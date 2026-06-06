package com.hxy.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxy.config.RabbitMQConfig;
import com.hxy.enums.CouponStateEnum;
import com.hxy.enums.ProductOrderPayTypeEnum;
import com.hxy.enums.ProductOrderStateEnum;
import com.hxy.enums.ProductOrderTypeEnum;
import com.hxy.exception.BizException;
import com.hxy.feign.CouponFeignService;
import com.hxy.feign.ProductFeignService;
import com.hxy.feign.UserFeignService;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.mapper.ProductOrderItemMapper;
import com.hxy.model.LoginUser;
import com.hxy.model.OrderMessage;
import com.hxy.model.ProductOrderDO;
import com.hxy.mapper.ProductOrderMapper;
import com.hxy.model.ProductOrderItemDO;
import com.hxy.pay.PayFactory;
import com.hxy.pay.PayInterface;
import com.hxy.request.*;
import com.hxy.service.ProductOrderItemService;
import com.hxy.service.ProductOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.CommonUtil;
import com.hxy.utils.JsonData;
import com.hxy.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-21
 */
@Service
@Slf4j
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderService {

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private UserFeignService userFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private CouponFeignService couponFeignSerivce;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductOrderItemService productOrderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderItemMapper productOrderItemMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //下单接口
    @Override
    public String confirmOrder(ConfirmOrderRequest request) {
        //0,验证防重令牌
        // 在confirmOrder方法最前面
        String orderToken = request.getToken();
        if (StringUtils.isBlank(orderToken)) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_NOT_EXIST);
        }

// Lua脚本原子操作：比较并删除
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                "return redis.call('del',KEYS[1]) " +
                "else return 0 end";

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Arrays.asList("user:order:" + LoginInterceptor.threadLocal.get().getId()),
                orderToken);

        if (result == 0L) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_TOKEN_EQUAL_FAIL);
        }


        //1,生成订单号
        String outTradeNo = CommonUtil.getRandomString(32);
        //2,验证收货地址
        AddressVO addressVO = getUserAddress(request.getAddressId());
        //3,获取商品
        JsonData cartItemDate = productFeignService.confirmOrderCartItem(request.getProductIdList());
        List<CartItemVO> orderItemList = cartItemDate.getData(new TypeReference<List<CartItemVO>>() {
        });
        if (orderItemList == null) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }
        //4,计算订单金额
        this.checkPrice(orderItemList, request);

        //5,锁定优惠券
        this.lockCouponRecords(request, outTradeNo);

        //6,锁定库存
        this.lockProductStocks(orderItemList, outTradeNo);

        //7,生成订单
        ProductOrderDO productOrderDO = saveProductOrder(request, LoginInterceptor.threadLocal.get(), outTradeNo, addressVO);

        //8创建订单项
        saveProductOrderItems(outTradeNo, productOrderDO.getId(), orderItemList);

        //9,发送延迟消息
        // 发送延迟消息，用于超时未支付自动关单
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOutTradeNo(outTradeNo);
        rabbitTemplate.convertAndSend(
                rabbitMQConfig.getEventExchange(),
                rabbitMQConfig.getOrderCloseDelayRoutingKey(),
                orderMessage);
        log.info("订单关单延迟消息发送成功: {}", outTradeNo);


        //10,生成支付信息
        PayInfoVO payInfoVO = new PayInfoVO();
        payInfoVO.setOutTradeNo(outTradeNo);
        payInfoVO.setPayFee(request.getRealPayAmount());
        payInfoVO.setPayType(request.getPayType());
        payInfoVO.setClientType(request.getClientType());
        payInfoVO.setTitle("商品订单");
        payInfoVO.setDescription("商品订单");
        payInfoVO.setOrderPayTimeoutMills(15 * 60);
        PayInterface ali = PayFactory.getPay("ali");
        String pay = ali.pay(payInfoVO);

        //11,将支付页面存进redis中
        redisTemplate.opsForValue().set("order:pay:" + outTradeNo, pay, 14, TimeUnit.MINUTES);


        return pay;
    }

    private void saveProductOrderItems(String outTradeNo, Long orderId, List<CartItemVO> orderItemList) {
        List<ProductOrderItemDO> items = orderItemList.stream().map(obj -> {
            ProductOrderItemDO item = new ProductOrderItemDO();
            item.setProductOrderId(orderId);
            item.setOutTradeNo(outTradeNo);
            item.setProductId(obj.getProductId());
            item.setProductName(obj.getProductTitle());
            item.setProductImg(obj.getProductImg());
            item.setBuyNum(obj.getBuyNum());
            item.setAmount(obj.getAmount());
            item.setTotalAmount(obj.getTotalAmount());
            item.setCreateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        productOrderItemService.saveBatch(items);
    }

    private ProductOrderDO saveProductOrder(ConfirmOrderRequest orderRequest, LoginUser loginUser,
                                            String orderOutTradeNo, AddressVO addressVO) {
        ProductOrderDO order = new ProductOrderDO();
        order.setUserId(loginUser.getId().intValue());
        order.setHeadImg(loginUser.getHeadImg());
        order.setNickname(loginUser.getName());
        order.setOutTradeNo(orderOutTradeNo);
        order.setCreateTime(new Date());
        order.setDel(0);
        order.setOrderType(ProductOrderTypeEnum.DAILY.name());
        order.setPayAmount(orderRequest.getRealPayAmount());  // 实付金额
        order.setTotalAmount(orderRequest.getTotalAmount());  // 原总价
        order.setState(ProductOrderStateEnum.NEW.name());
        order.setPayType(orderRequest.getPayType());
        order.setReceiverAddress(JSON.toJSONString(addressVO));  // 地址快照
        productOrderMapper.insert(order);
        return order;
    }


    /**
     * 锁定商品库存
     *
     * @param orderItemList
     * @param orderOutTradeNo
     */
    private void lockProductStocks(List<CartItemVO> orderItemList, String orderOutTradeNo) {

        List<OrderItemRequest> itemRequestList = orderItemList.stream().map(obj -> {

            OrderItemRequest request = new OrderItemRequest();
            request.setBuyNum(obj.getBuyNum());
            request.setProductId(obj.getProductId());
            return request;
        }).collect(Collectors.toList());


        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemList(itemRequestList);

        JsonData jsonData = productFeignService.lockProductStock(lockProductRequest);
        if (jsonData.getCode() != 0) {
            log.error("锁定商品库存失败：{}", lockProductRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }

    /**
     * 锁定优惠券
     *
     * @param orderRequest
     * @param orderOutTradeNo
     */
    private void lockCouponRecords(ConfirmOrderRequest orderRequest, String orderOutTradeNo) {
        List<Long> lockCouponRecordIds = new ArrayList<>();
        if (orderRequest.getCouponRecordId() > 0) {
            lockCouponRecordIds.add(orderRequest.getCouponRecordId());

            LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
            lockCouponRecordRequest.setOrderOutTradeNo(orderOutTradeNo);
            lockCouponRecordRequest.setLockCouponRecordIds(lockCouponRecordIds);

            //发起锁定优惠券请求
            JsonData jsonData = couponFeignSerivce.lockCouponRecords(lockCouponRecordRequest);
            if (jsonData.getCode() != 0) {
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }

    }

    /**
     * 验证价格
     * 1）统计全部商品的价格
     * 2) 获取优惠券(判断是否满足优惠券的条件)，总价再减去优惠券的价格 就是 最终的价格
     *
     * @param orderItemList
     * @param orderRequest
     */
    private void checkPrice(List<CartItemVO> orderItemList, ConfirmOrderRequest orderRequest) {

        //统计商品总价格
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null) {
            for (CartItemVO orderItemVO : orderItemList) {
                BigDecimal itemRealPayAmount = orderItemVO.getTotalAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
        }

        //获取优惠券，判断是否可以使用
        CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());

        //计算购物车价格，是否满足优惠券满减条件
        if (couponRecordVO != null) {

            //计算是否满足满减
            if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
            }
            if (couponRecordVO.getPrice().compareTo(realPayAmount) > 0) {
                realPayAmount = BigDecimal.ZERO;
            } else {
                realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
            }

        }

        if (realPayAmount.compareTo(orderRequest.getRealPayAmount()) != 0) {
            log.error("订单验价失败：{}", orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    /**
     * 获取优惠券
     *
     * @param couponRecordId
     * @return
     */
    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {

        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }

        JsonData couponData = couponFeignSerivce.findUserCouponRecordById(couponRecordId);

        if (couponData.getCode() != 0) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }

        if (couponData.getCode() == 0) {

            CouponRecordVO couponRecordVO = couponData.getData(new TypeReference<CouponRecordVO>() {
            });

            if (!couponAvailable(couponRecordVO)) {
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            return couponRecordVO;
        }

        return null;
    }

    /**
     * 判断优惠券是否可用
     *
     * @param couponRecordVO
     * @return
     */
    private boolean couponAvailable(CouponRecordVO couponRecordVO) {

        if (couponRecordVO.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())) {
            long currentTimestamp = System.currentTimeMillis();
            long end = couponRecordVO.getEndTime().getTime();
            long start = couponRecordVO.getStartTime().getTime();
            if (currentTimestamp >= start && currentTimestamp <= end) {
                return true;
            }
        }
        return false;
    }

    //获取收货地址
    private AddressVO getUserAddress(Long id) {
        JsonData data = userFeignService.detail(id);
        if (data.getCode() != 0) {
            log.error("获取收获地址失败,msg:{}", data);
            throw new BizException(BizCodeEnum.ADDRESS_NO_EXITS);
        }
        AddressVO addressVO = data.getData(new TypeReference<AddressVO>() {
        });
        return addressVO;
    }


    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO order = productOrderMapper.selectOne(
                new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
        return order == null ? "" : order.getState();
    }


    //自动关单
    @Override
    public boolean closeProductOrder(OrderMessage orderMessage) {
        //1,查询订单
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductOrderDO::getOutTradeNo, orderMessage.getOutTradeNo());
        ProductOrderDO orderDO = this.getOne(queryWrapper);

        //2,判断
        if (orderDO == null) {
            log.warn("订单不存在，直接确认: {}", orderMessage);
            return true;  // 订单可能已被删除，消息无需重试
        }

        //3,判断订单状态
        if (ProductOrderStateEnum.PAY.name().equalsIgnoreCase(orderDO.getState()) || ProductOrderStateEnum.CANCEL.name().equalsIgnoreCase(orderDO.getState())) {
            log.info("订单已支付，直接确认: {}", orderMessage);
            return true;
        }

        //4,订单未支付  主动查单
        //查询支付的状态（钱到没到账）
        PayInterface ali = PayFactory.getPay("ali");
        String payResult = ali.queryPayState(orderMessage.getOutTradeNo());


        //5,判断支付结果
        LambdaUpdateWrapper<ProductOrderDO> updateWrapper = new LambdaUpdateWrapper<>();
        if (payResult != null) {
            log.info("订单支付成功: {}", orderMessage);
            updateWrapper.eq(ProductOrderDO::getOutTradeNo, orderMessage.getOutTradeNo());
            updateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
            this.update(updateWrapper);
            return true;
        } else {
            log.info("订单支付失败: {}", orderMessage);
            updateWrapper.eq(ProductOrderDO::getOutTradeNo, orderMessage.getOutTradeNo());
            updateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.CANCEL.name());
            this.update(updateWrapper);
            return true;
        }
    }

    @Override
    public JsonData handlerOrderCallbackMsg(ProductOrderPayTypeEnum productOrderPayTypeEnum, Map<String, String> paramsMap) {

        String outTradeNo = paramsMap.get("out_trade_no");
        String tradeStatus = paramsMap.get("trade_status");
        log.info("支付宝回调，订单号={}, 交易状态={}", outTradeNo, tradeStatus);

        // 只有 TRADE_SUCCESS 和 TRADE_FINISHED 代表支付成功
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 使用乐观锁更新订单状态：只有状态为NEW才能改为PAY
            LambdaUpdateWrapper<ProductOrderDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProductOrderDO::getOutTradeNo, outTradeNo);
            updateWrapper.set(ProductOrderDO::getState, ProductOrderStateEnum.PAY.name());
            boolean rows = this.update(null, updateWrapper);

            if (rows) {
                log.info("订单 {} 支付成功，状态已更新", outTradeNo);
                // TODO: 可在此处发送消息，触发库存真实扣减、积分发放等后续流程
            } else {
                log.warn("订单 {} 状态更新失败，可能已处理或状态异常", outTradeNo);
            }
            return JsonData.buildSuccess();
        } else {
            log.info("订单 {} 交易状态为 {}，暂不处理", outTradeNo, tradeStatus);
        }

        return JsonData.buildSuccess();
    }

    @Override
    public Object page1(int page, int size, String state) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);

        QueryWrapper<ProductOrderDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginUser.getId());
        if (StringUtils.isNotBlank(state)) {
            wrapper.eq("state", state);
        }
        wrapper.orderByDesc("create_time");

        IPage<ProductOrderDO> pageResult = productOrderMapper.selectPage(pageInfo, wrapper);

        // 转换为VO并填充订单项
        List<ProductOrderVO> voList = pageResult.getRecords().stream().map(order -> {
            // 查询该订单的所有订单项
            List<ProductOrderItemDO> items = productOrderItemMapper.selectList(
                    new QueryWrapper<ProductOrderItemDO>()
                            .eq("product_order_id", order.getId()));

            List<CartItemVO> itemVOList = items.stream().map(item -> {
                CartItemVO itemVO = new CartItemVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            }).collect(Collectors.toList());

            ProductOrderVO orderVO = new ProductOrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderItemList(itemVOList);
            return orderVO;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total_record", pageResult.getTotal());
        result.put("total_page", pageResult.getPages());
        result.put("current_data", voList);
        return result;
    }

    @Override
    public String repay(RepayOrderRequest repayRequest) {
        String string = redisTemplate.opsForValue().get("order:pay:" + repayRequest.getOutTradeNo());
        if (string == null || string.length() == 0) {
            return null;
        } else {
            return string;
        }
    }
}
