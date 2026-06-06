package com.hxy.service;

import com.hxy.model.CouponDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.utils.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
public interface CouponService extends IService<CouponDO> {

    JsonData pageCouponActivity(int page, int size);

    JsonData addPromotionCoupon(Long couponId);
}
