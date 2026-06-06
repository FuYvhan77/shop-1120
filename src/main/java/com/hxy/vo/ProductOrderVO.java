package com.hxy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ProductOrderVO {
    private Long id;
    private String outTradeNo;
    private String state;           // NEW / PAY / CANCEL
    private Date createTime;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private String payType;
    private String nickname;
    private String headImg;
    private Long userId;
    private String orderType;
    private String receiverAddress; // JSON格式收货地址快照
    private List<CartItemVO> orderItemList;  // 订单项列表
}