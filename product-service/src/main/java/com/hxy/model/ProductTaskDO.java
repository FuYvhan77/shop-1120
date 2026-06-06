package com.hxy.model;

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
 * @since 2026-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_task")
public class ProductTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 购买数量（锁定量）
     */
    private Integer buyNum;

    /**
     * 商品标题
     */
    private String productName;

    /**
     * 锁定状态: LOCK/FINISH/CANCEL
     */
    private String lockState;

    /**
     * 关联的订单号
     */
    private String outTradeNo;

    private Date createTime;


}
