package com.hxy.model;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zyd
 * @since 2026-05-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_order")
public class ProductOrderDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单唯一标识（展示给用户和支付系统）
     */
    private String outTradeNo;

    /**
     * NEW 未支付, PAY 已支付, CANCEL 超时取消
     */
    private String state;

    /**
     * 订单生成时间
     */
    private Date createTime;

    /**
     * 订单总金额（未减优惠）
     */
    private BigDecimal totalAmount;

    /**
     * 实际支付金额（减去优惠后）
     */
    private BigDecimal payAmount;

    /**
     * 支付类型，如 ALIPAY/WECHAT/BANK
     */
    private String payType;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String headImg;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 0未删除，1已删除（逻辑删除）
     */
    private Integer del;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 订单类型 DAILY普通单/PROMOTION促销单
     */
    private String orderType;

    /**
     * 收货地址（JSON存储，快照）
     */
    private String receiverAddress;


}
