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
 * @since 2026-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product")
public class ProductDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 封面图
     */
    private String coverImg;

    /**
     * 详情（富文本或JSON）
     */
    private String detail;

    /**
     * 原价（划线价）
     */
    private BigDecimal oldAmount;

    /**
     * 现价（实际售价）
     */
    private BigDecimal amount;

    /**
     * 实际库存
     */
    private Integer stock;

    private Date createTime;

    /**
     * 锁定库存（已下单未支付）
     */
    private Integer lockStock;


}
