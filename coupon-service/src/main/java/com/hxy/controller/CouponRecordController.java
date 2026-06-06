package com.hxy.controller;


import com.hxy.request.LockCouponRecordRequest;
import com.hxy.request.NewUserCouponRequest;
import com.hxy.service.CouponRecordService;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
@Api(tags = "优惠券领取记录")
@RestController
@RequestMapping("/api/coupon_record/v1")
public class CouponRecordController {

    @Autowired
    private CouponRecordService couponRecordService;


    @ApiOperation("分页查询优惠券领取记录")
    @GetMapping("page")
    public JsonData pageCouponRecord(@ApiParam("当前页") @RequestParam(defaultValue = "1") int page,
                                     @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size) {


        return couponRecordService.pageList(page, size);
    }



    @ApiOperation("查询优惠券领取记录详情")
    @GetMapping("/detail/{record_id}")
    public JsonData detail(@ApiParam("优惠券领取记录id") @PathVariable("record_id") Long id) {

        return couponRecordService.selById(id);
    }



    //TODO  新人注册优惠卷领  暂时user微服务没有调用
    @ApiOperation("领取新人注册优惠卷")
    @PostMapping("/new_user_coupon")
    public JsonData newUserCoupon(@ApiParam("用户对象") @RequestBody NewUserCouponRequest request) {
        return couponRecordService.newUserCoupon(request);
    }



    @ApiOperation("rpc-锁定优惠券记录")
    @PostMapping("lock_records")
    public JsonData lockCouponRecords(@RequestBody LockCouponRecordRequest recordRequest) {
        return couponRecordService.lockCouponRecords(recordRequest);
    }
}

