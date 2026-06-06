package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("buy_num")
    private Integer buyNum;

    @JsonProperty("product_title")
    private String productTitle;

    @JsonProperty("product_img")
    private String productImg;

    private BigDecimal amount;  // 商品单价

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;  // 该项小计 = amount * buyNum

    // getter / setter ...

    /**
     * 总价格 = 单价 × 数量
     */
    public BigDecimal getTotalAmount() {
        return this.amount.multiply(new BigDecimal(this.buyNum));
    }
}