package com.hxy.service.impl;

import com.hxy.exception.BizException;
import com.hxy.interceptor.LoginInterceptor;
import com.hxy.model.LoginUser;
import com.hxy.model.ProductDO;
import com.hxy.request.CartItemRequest;
import com.hxy.service.CartService;
import com.hxy.service.ProductService;
import com.hxy.utils.BizCodeEnum;
import com.hxy.utils.JsonData;
import com.hxy.vo.CartItemVO;
import com.hxy.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addToCart(CartItemRequest cartItemRequest) {
        // 1. 判读他有没有购物车
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        // 2,他要购买的商品是否重复的？
        Object o = myCartOps.get(cartItemRequest.getProductId()+"");
        if (o==null){
            //不存在
            CartItemVO cartItemVO=new CartItemVO();
            cartItemVO.setProductId(cartItemRequest.getProductId());
            cartItemVO.setBuyNum(cartItemRequest.getBuyNum());
            ProductDO byId = productService.getById(cartItemRequest.getProductId());
            if (byId==null){
                throw new BizException(BizCodeEnum.PRODUCT_NOT_EXIST);
            }
            cartItemVO.setProductTitle(byId.getTitle());
            cartItemVO.setProductImg(byId.getCoverImg());
            cartItemVO.setAmount(byId.getAmount());

            myCartOps.put(cartItemVO.getProductId()+"",JSON.toJSONString(cartItemVO));

        }else {
            // 存在
            CartItemVO cartItemVO = JSON.parseObject((String) o, CartItemVO.class);
            cartItemVO.setBuyNum(cartItemVO.getBuyNum()+cartItemRequest.getBuyNum());
            myCartOps.put(cartItemVO.getProductId()+"",JSON.toJSONString(cartItemVO));
        }



    }

    @Override
    public void clear() {
        String cartKey = getCartKey();
        redisTemplate.delete(cartKey);
    }



    // 抽取通用方法——获取操作对象
    private BoundHashOperations<String, Object, Object> getMyCartOps() {
        String cartKey = getCartKey();
        return redisTemplate.boundHashOps(cartKey);
    }


    public static final String CART_KEY = "cart:%s";

    // 生成购物车 Redis Key
    private String getCartKey() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        return String.format(CART_KEY, loginUser.getId());//catr:id
    }



    @Override
    public CartVO getMyCart() {
        // 构建购物项列表，参数 true 表示需要获取最新价格
        List<CartItemVO> cartItemVOList = buildCartItem(true);

        CartVO cartVO = new CartVO();
        cartVO.setCartItems(cartItemVOList);
        return cartVO;
    }

    /**
     * 构建购物项列表
     * @param latestPrice 是否需要获取最新价格和商品信息
     */
    private List<CartItemVO> buildCartItem(boolean latestPrice) {
        BoundHashOperations<String, Object, Object> myCartOps = getMyCartOps();
        List<Object> values = myCartOps.values();

        List<CartItemVO> list = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        // 反序列化所有购物项
        for (Object value : values) {
            CartItemVO item = JSON.parseObject((String) value, CartItemVO.class);
            list.add(item);
            ids.add(item.getProductId());
        }

        // 如果需要最新价格，批量查询商品服务
        if (latestPrice && !ids.isEmpty()) {
            setProductLatestPrice(list, ids);
        }
        return list;
    }

    /**
     * 批量查询商品最新信息，并更新购物项中的标题、图片、单价
     */
    private void setProductLatestPrice(List<CartItemVO> list, List<Long> ids) {
        List<ProductDO> productDOS = productService.listByIds(ids);
        Map<Long, ProductDO> productMap = productDOS.stream()
                .collect(Collectors.toMap(ProductDO::getId, Function.identity()));

        list.forEach(item -> {
            ProductDO product = productMap.get(item.getProductId());
            if (product != null) {
                item.setProductTitle(product.getTitle());
                item.setProductImg(product.getCoverImg());
                item.setAmount(product.getAmount());   // 以数据库最新价格为准
            }
        });
    }


    @Override
    public void deleteItem(long productId) {
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        myCart.delete(productId+"");
    }


    @Override
    public void changeItemNum(CartItemRequest cartItemRequest) {
        BoundHashOperations<String, Object, Object> myCart = getMyCartOps();
        Object cacheObj = myCart.get(cartItemRequest.getProductId());

        if (cacheObj == null) {
            throw new BizException(BizCodeEnum.CART_FAIL);
        }

        CartItemVO item = JSON.parseObject((String) cacheObj, CartItemVO.class);
        int newNum = cartItemRequest.getBuyNum();
        if (newNum <= 0) {
            // 数量 <= 0 时，自动删除该购物项
            myCart.delete(cartItemRequest.getProductId());
        } else {
            item.setBuyNum(newNum);
            myCart.put(cartItemRequest.getProductId(), JSON.toJSONString(item));
        }
    }


    public List<CartItemVO> confirmOrderCartItem(List<Long> productIdList) {
        // 获取用户购物车全部数据，并刷新最新价格
        List<CartItemVO> allItems = buildCartItem(true);
        // 过滤出本次要购买的商品，并从购物车中删除
        List<CartItemVO> result = allItems.stream().filter(item -> {
            if(productIdList.contains(item.getProductId())){
                this.deleteItem(item.getProductId());   // 从Redis Hash中删除
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return result;
    }
}