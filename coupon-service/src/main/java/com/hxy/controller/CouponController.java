package com.hxy.controller;


import com.hxy.model.CouponDO;
import com.hxy.request.LockCouponRecordRequest;
import com.hxy.service.CouponService;
import com.hxy.utils.JsonData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zyd
 * @since 2026-05-19
 */
@Api(tags = "优惠券")
@RestController
@RequestMapping("/api/coupon/v1")
public class CouponController {


    @Autowired
    private CouponService couponService;


    @ApiOperation("优惠券列表")
    @GetMapping("/list")
    public JsonData list(@ApiParam("当前页") @RequestParam(defaultValue = "1") int page,
                         @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size) {
        return couponService.pageCouponActivity(page, size);
    }


    @ApiOperation("领取优惠卷")
    @GetMapping("/add/promotion/{coupon_id}")
    public JsonData addPromotionCoupon(@ApiParam("优惠券id") @PathVariable("coupon_id") Long couponId) {
        return couponService.addPromotionCoupon(couponId);
    }


    @ApiOperation("测试缓存查询优惠券")
    @GetMapping("/get_coupon")
    @Cacheable(key = "abc", value = "coupon_list")//coupon_list:coupon_list :
    public JsonData getCoupon() {
        return JsonData.buildSuccess(couponService.list());
    }

    @ApiOperation("测试缓存查询优惠券2")
    @GetMapping("/get_coupon2")
    @Cacheable(value = "coupon", key = "#id")//coupon_list:coupon_list :  1:vslue
    public JsonData getCoupon2(@RequestParam("id") Long id) {
        return JsonData.buildSuccess(couponService.getById(id));
    }

    @ApiOperation("修改")
    @PostMapping("/updata")
    @CachePut(value = "coupon", key = "#entity.id")
    public JsonData add(@RequestBody CouponDO entity) {boolean save = couponService.updateById(entity);
        if (save) {
            return JsonData.buildSuccess(entity);
        }
        return JsonData.buildError("修改失败");
    }


    @ApiOperation("测试缓存删除优惠券")
    @PostMapping("/updata_coupon")
    @CacheEvict(value = "coupon", key = "#id")
    public JsonData del(@RequestParam("id") Long id){
        couponService.removeById( id);
        return JsonData.buildSuccess();
    }

}

