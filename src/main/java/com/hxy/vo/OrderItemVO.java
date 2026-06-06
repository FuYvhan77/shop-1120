package com.hxy.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName OrderItemVO
 * @date 2026-05-29 13:55
 */

@Data
public class OrderItemVO {

    private Long id;

    /**
     * 关联的订单ID
     */
    private Long productOrderId;

    private String outTradeNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（快照）
     */
    private String productName;

    /**
     * 商品图片（快照）
     */
    private String productImg;

    /**
     * 购买数量
     */
    private Integer buyNum;

    private Date createTime;

    /**
     * 该商品项总价（单价*数量）
     */
    private BigDecimal totalAmount;

    /**
     * 商品单价
     */
    private BigDecimal amount;
}
