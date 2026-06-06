package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {

    @JsonProperty("cart_items")
    private List<CartItemVO> cartItems;

    @JsonProperty("total_num")
    private Integer totalNum;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("real_pay_amount")
    private BigDecimal realPayAmount;

    public Integer getTotalNum() {
        if (this.cartItems != null) {
            return cartItems.stream().mapToInt(CartItemVO::getBuyNum).sum();
        }
        return 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (this.cartItems != null) {
            for (CartItemVO item : cartItems) {
                amount = amount.add(item.getTotalAmount());
            }
        }
        return amount;
    }

    public BigDecimal getRealPayAmount() {
        // 无优惠时与totalAmount一致，后续减去优惠券金额
        return getTotalAmount();
    }

    // getter / setter ...
}