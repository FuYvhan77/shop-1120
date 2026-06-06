package com.hxy.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "优惠券锁定对象", description = "优惠券锁定对象")
public class LockCouponRecordRequest {
    @ApiModelProperty(value = "优惠券记录id列表", example = "[1,2,3]")
    private List<Long> lockCouponRecordIds;

    @ApiModelProperty(value = "订单号", example = "3234fw234rfd232")
    private String orderOutTradeNo;
}