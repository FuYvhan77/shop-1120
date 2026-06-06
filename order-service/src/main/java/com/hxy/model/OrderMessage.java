package com.hxy.model;

import lombok.Data;

@Data
public class OrderMessage {
    private Long messageId;
    private String outTradeNo;   // 需要关单的订单号
}