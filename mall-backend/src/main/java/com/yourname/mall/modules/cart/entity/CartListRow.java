package com.yourname.mall.modules.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartListRow {

    private Long cartItemId;
    private Long spuId;
    private Long skuId;
    private String name;
    private String description;
    private String image;
    private Integer quantity;
    private Integer stock;
    private BigDecimal addedPrice;
    private BigDecimal currentPrice;
}

