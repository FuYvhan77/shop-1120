package com.hxy.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RepayOrderRequest {
    @JsonProperty("out_trade_no")
    private String outTradeNo;
    @JsonProperty("pay_type")
    private String payType;
    @JsonProperty("client_type")
    private String clientType;
}