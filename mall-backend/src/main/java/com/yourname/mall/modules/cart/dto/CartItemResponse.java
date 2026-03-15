package com.yourname.mall.modules.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long spuId;
    private Long skuId;
    private String name;
    private String description;
    private String image;
    private Integer quantity;
    private Integer stock;
    private BigDecimal addedPrice;
    private BigDecimal currentPrice;
    private BigDecimal subtotalAmount;
}

