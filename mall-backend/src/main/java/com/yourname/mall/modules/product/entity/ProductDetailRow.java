package com.yourname.mall.modules.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductDetailRow {

    private Long skuId;
    private String name;
    private String description;
    private String detailHtml;
    private String image;
    private BigDecimal price;
    private Integer stock;
}