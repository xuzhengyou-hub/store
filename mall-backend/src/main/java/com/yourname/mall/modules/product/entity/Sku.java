package com.yourname.mall.modules.product.entity;

import lombok.Data;

@Data
public class Sku {

    private Long id;
    private Long spuId;
    private Integer stock;
    private Integer lockStock;
    private Long priceCent;
}
