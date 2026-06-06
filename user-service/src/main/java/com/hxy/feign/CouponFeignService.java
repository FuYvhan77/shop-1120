package com.hxy.feign;

import com.hxy.request.NewUserCouponRequest;
import com.hxy.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service")
public interface CouponFeignService {

    /**
     * 新用户注册发放优惠券
     */
    @PostMapping("/api/coupon_record/v1/new_user_coupon")
    JsonData addNewUserCoupon(@RequestBody NewUserCouponRequest request);
}
