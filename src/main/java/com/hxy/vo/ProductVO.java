package com.hxy.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVO {
    private Long id;
    private String title;
    @JsonProperty("cover_img")
    private String coverImg;
    private String detail;
    @JsonProperty("old_price")
    private BigDecimal oldPrice;
    private BigDecimal price;
    private Integer stock;   // 注意：返回给前台的应该是可售库存
}