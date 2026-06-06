package com.hxy.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ConfirmOrderRequest {
    @JsonProperty("coupon_record_id")
    private Long couponRecordId;           // 选用的优惠券记录ID，-1表示不使用

    @JsonProperty("product_ids")
    private List<Long> productIdList;      // 要购买的商品ID列表

    @JsonProperty("pay_type")
    private String payType;                // 支付渠道 ALIPAY/WECHAT

    @JsonProperty("client_type")
    private String clientType;             // 客户端类型 APP/PC/H5

    @JsonProperty("address_id")
    private long addressId;                // 收货地址ID

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;        // 前端计算的商品总价（后端需验价）

    @JsonProperty("real_pay_amount")
    private BigDecimal realPayAmount;      // 前端计算的实付金额（总价-优惠）

    @JsonProperty("token")
    private String token;                  // 防重令牌
}