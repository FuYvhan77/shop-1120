package com.hxy.feign;

import com.hxy.request.LockProductRequest;
import com.hxy.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductFeignService {
    @PostMapping("/api/cart/v1/confirm_order_cart_items")
    JsonData confirmOrderCartItem(@RequestBody List<Long> productIdList);

    /**
     * 锁定商品购物项库存
     * @param lockProductRequest
     * @return
     */
    @PostMapping("/api/product/v1/lock_products")
    JsonData lockProductStock(@RequestBody LockProductRequest lockProductRequest);
}
