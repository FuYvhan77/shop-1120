package com.hxy.service;

import com.hxy.model.CouponRecordDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hxy.model.CouponRecordMessage;
import com.hxy.request.LockCouponRecordRequest;
import com.hxy.request.NewUserCouponRequest;
import com.hxy.utils.JsonData;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
public interface CouponRecordService extends IService<CouponRecordDO> {

    JsonData pageList(int page, int size);

    JsonData selById(Long id);

    JsonData newUserCoupon(NewUserCouponRequest request);

    JsonData lockCouponRecords(LockCouponRecordRequest recordRequest);

    boolean releaseCouponRecord(CouponRecordMessage recordMessage);
}
