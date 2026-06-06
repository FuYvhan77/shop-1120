package com.hxy.model;

import lombok.Data;

@Data
public class CouponRecordMessage {
    private Long messageId;      // 消息唯一标识（可选，用于日志追踪）
    private String outTradeNo;   // 关联的订单号
    private Long taskId;         // 锁定任务ID
}