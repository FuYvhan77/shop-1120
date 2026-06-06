package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CouponVO {
    private Long id;
    private String category;
    @JsonProperty("coupon_img")
    private String couponImg;
    @JsonProperty("coupon_title")
    private String couponTitle;
    private BigDecimal price;
    @JsonProperty("user_limit")
    private Integer userLimit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("start_time")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("end_time")
    private Date endTime;
    @JsonProperty("publish_count")
    private Integer publishCount;
    private Integer stock;
    @JsonProperty("condition_price")
    private BigDecimal conditionPrice;
}