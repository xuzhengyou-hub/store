package com.yourname.mall.modules.order.entity;

import lombok.Data;

@Data
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long skuId;
    private Integer quantity;
    private Long priceCent;
}
