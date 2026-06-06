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
 * @since 2026-05-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("coupon_record")
public class CouponRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券id
     */
    private Long couponId;

    /**
     * 领取时间
     */
    private Date createTime;

    /**
     * 使用状态: NEW/USED/EXPIRED
     */
    private String useState;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称（冗余，减少关联查询）
     */
    private String userName;

    /**
     * 优惠券标题（冗余）
     */
    private String couponTitle;

    /**
     * 有效开始时间（冗余）
     */
    private Date startTime;

    /**
     * 有效结束时间（冗余）
     */
    private Date endTime;

    /**
     * 使用的订单id
     */
    private Long orderId;

    /**
     * 抵扣价格（冗余）
     */
    private BigDecimal price;

    /**
     * 满减门槛（冗余）
     */
    private BigDecimal conditionPrice;


}
