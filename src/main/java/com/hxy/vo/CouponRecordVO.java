package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponRecordVO {
    private Long id;
    @JsonProperty("coupon_id")
    private Long couponId;
    @JsonProperty("use_state")
    private String useState;     // NEW / USED / EXPIRED
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("coupon_title")
    private String couponTitle;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    @JsonProperty("start_time")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")
    @JsonProperty("end_time")
    private Date endTime;
    @JsonProperty("order_id")
    private Long orderId;
    private BigDecimal price;           // 抵扣金额
    @JsonProperty("condition_price")
    private BigDecimal conditionPrice;  // 满减门槛
}