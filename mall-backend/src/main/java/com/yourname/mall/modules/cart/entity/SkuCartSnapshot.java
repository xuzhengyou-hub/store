package com.yourname.mall.modules.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SkuCartSnapshot {

    private Long spuId;
    private Long skuId;
    private String image;
    private BigDecimal price;
    private Integer stock;
}
