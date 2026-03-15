package com.yourname.mall.modules.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemEntity {

    private Long id;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer quantity;
    private BigDecimal price;
}
