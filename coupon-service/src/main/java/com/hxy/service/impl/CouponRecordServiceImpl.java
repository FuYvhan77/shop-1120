package com.hxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxy.config.RabbitMQConfig;
import com.hxy.enums.CouponCategoryEnum;
import com.hxy.enums.CouponStateEnum;
import com.hxy.enums.CouponTaskStateEnum;
import com.hxy.exception.BizException;
import com.hxy.feign.ProductOrderFeignSerivce;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.mapper.CouponMapper;
import com.hxy.mapper.CouponTaskMapper;
import com.hxy.model.*;
import com.hxy.mapper.CouponRecordMapper;
import com.hxy.request.LockCouponRecordRequest;
import com.hxy.request.NewUserCouponRequest;
import com.hxy.service.CouponRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hxy.service.CouponService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.JsonData;
import com.hxy.vo.CouponRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hxy.enums.ProductOrderStateEnum;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
@Service
@Slf4j
public class CouponRecordServiceImpl extends ServiceImpl<CouponRecordMapper, CouponRecordDO> implements CouponRecordService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRecordMapper couponRecordMapper;

    @Autowired
    private CouponTaskMapper couponTaskMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private ProductOrderFeignSerivce productOrderFeignSerivce;

    @Override
    public JsonData pageList(int page, int size) {
        //1,定义条件构造器
        LambdaQueryWrapper<CouponRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponRecordDO::getUserId, LoginInterceptor.threadLocal.get().getId());
        queryWrapper.orderByDesc(CouponRecordDO::getCreateTime);
        //2,查询数据
        Page<CouponRecordDO> pageParam = new Page<>(page, size);
        Page<CouponRecordDO> page1 = this.page(pageParam, queryWrapper);
        //3,转换
        List<CouponRecordVO> collect = page1.getRecords().stream().map(item -> {
            CouponRecordVO couponRecordVO = new CouponRecordVO();
            BeanUtils.copyProperties(item, couponRecordVO);
            return couponRecordVO;
        }).collect(Collectors.toList());
        //4,返回
        Map<String, Object> map = new HashMap<>();
        map.put("total", page1.getTotal());
        map.put("rows",collect);
        return JsonData.buildSuccess(map);
    }

    @Override
    public JsonData selById(Long id) {
        LambdaQueryWrapper<CouponRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponRecordDO::getId, id);
        queryWrapper.eq(CouponRecordDO::getUserId, LoginInterceptor.threadLocal.get().getId());
        CouponRecordDO couponRecordDO = this.getOne(queryWrapper);
        CouponRecordVO couponRecordVO = new CouponRecordVO();
        BeanUtils.copyProperties(couponRecordDO, couponRecordVO);
        return JsonData.buildSuccess(couponRecordVO);
    }

    @Override
    public JsonData newUserCoupon(NewUserCouponRequest request) {
        //发放优惠卷接口
        //1,查询有没有优惠卷
        LambdaQueryWrapper<CouponDO>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CouponDO::getCategory, CouponCategoryEnum.NEW_USER.name());
        List<CouponDO> couponDOS = couponMapper.selectList(queryWrapper);

        if (couponDOS.size() <= 0) {
            return JsonData.buildError("没有新人优惠券了");
        }


        //2,生成优惠券领取记录
        LoginUser loginUser=new LoginUser();
        loginUser.setId(request.getUserId());
        loginUser.setName(request.getName());
        LoginInterceptor.threadLocal.set(loginUser);
        for (CouponDO couponDO : couponDOS) {
            couponService.addPromotionCoupon(couponDO.getId());
        }


        return JsonData.buildSuccess("领取成功");
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public JsonData lockCouponRecords(LockCouponRecordRequest recordRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderOutTradeNo = recordRequest.getOrderOutTradeNo();
        List<Long> lockCouponRecordIds = recordRequest.getLockCouponRecordIds();

        //1,锁定优化
        // 第一步：批量更新优惠券记录状态为USED
        int updateRows = couponRecordMapper.lockUseStateBatch(
                loginUser.getId(),
                CouponStateEnum.USED.name(),
                lockCouponRecordIds);

        // 第二步：为每张被锁定的券创建锁定任务记录
        List<CouponTaskDO> couponTaskList = lockCouponRecordIds.stream().map(couponRecordId -> {
            CouponTaskDO taskDO = new CouponTaskDO();
            taskDO.setCreateTime(new Date());
            taskDO.setOutTradeNo(orderOutTradeNo);
            taskDO.setCouponRecordId(couponRecordId);
            taskDO.setLockState(CouponTaskStateEnum.LOCK.name());
            return taskDO;
        }).collect(Collectors.toList());

        int insertRows = couponTaskMapper.insertBatch(couponTaskList);

        log.info("优惠券记录锁定 updateRows={}", updateRows);
        log.info("新增优惠券task insertRows={}", insertRows);


        //TODO  向mq发送延迟消息
        // 校验：更新的记录数、插入的任务数必须与请求的ID数量完全一致
        if (lockCouponRecordIds.size() == insertRows && insertRows == updateRows) {
            // 第三步：发送延迟消息，用于后续超时释放
            for (CouponTaskDO task : couponTaskList) {
                CouponRecordMessage message = new CouponRecordMessage();
                message.setOutTradeNo(orderOutTradeNo);
                message.setTaskId(task.getId());
                rabbitTemplate.convertAndSend(
                        rabbitMQConfig.getEventExchange(),
                        rabbitMQConfig.getCouponReleaseDelayRoutingKey(),
                        message);
                log.info("优惠券锁定消息发送成功: {}", message);
            }
            return JsonData.buildSuccess();
        } else {
            throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean releaseCouponRecord(CouponRecordMessage recordMessage) {
        // 1. 查询锁定任务是否存在
        CouponTaskDO taskDO = couponTaskMapper.selectOne(
                new QueryWrapper<CouponTaskDO>().eq("id", recordMessage.getTaskId()));

        if (taskDO == null) {
            log.warn("工作单不存在，消息: {}", recordMessage);
            return true;  // 任务不存在，直接确认消费
        }

        // 2. 只有LOCK状态才需要处理
        if (!CouponTaskStateEnum.LOCK.name().equalsIgnoreCase(taskDO.getLockState())) {
            log.warn("工作单状态不是LOCK, state={}, 消息={}", taskDO.getLockState(), recordMessage);
            return true;  // 幂等：已处理过，直接确认
        }

        // 3. 查询订单状态
        JsonData jsonData = productOrderFeignSerivce.queryProductOrderState(recordMessage.getOutTradeNo());

        if (jsonData.getCode() == 0) {
            String state = jsonData.getData().toString();

            if (ProductOrderStateEnum.NEW.name().equalsIgnoreCase(state)) {
                // 订单仍为未支付，暂不释放，重新投递
                log.warn("订单状态是NEW，重新投递: {}", recordMessage);
                return false;
            }

            if (ProductOrderStateEnum.PAY.name().equalsIgnoreCase(state)) {
                // 已支付，只需将task标记为FINISH
                taskDO.setLockState(CouponTaskStateEnum.FINISH.name());
                couponTaskMapper.update(taskDO,
                        new QueryWrapper<CouponTaskDO>().eq("id", recordMessage.getTaskId()));
                log.info("订单已支付，修改锁定工作单为FINISH: {}", recordMessage);
                return true;
            }
        }

        // 4. 订单不存在或已取消 → 执行释放
        taskDO.setLockState(CouponTaskStateEnum.CANCEL.name());
        couponTaskMapper.update(taskDO,
                new QueryWrapper<CouponTaskDO>().eq("id", recordMessage.getTaskId()));

        // 恢复优惠券记录为可用
        LambdaUpdateWrapper<CouponRecordDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CouponRecordDO::getId, taskDO.getCouponRecordId());
        updateWrapper.set(CouponRecordDO::getUseState, CouponStateEnum.NEW.name());
        couponRecordMapper.update(null, updateWrapper);

        log.warn("订单不存在或已取消，释放优惠券，task=CANCEL, message={}", recordMessage);
        return true;
    }

}
