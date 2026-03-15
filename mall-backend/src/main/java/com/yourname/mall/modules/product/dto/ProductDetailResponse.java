package com.yourname.mall.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String shortDesc;
    private String detailHtml;
    private String image;
    private List<String> gallery;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
}