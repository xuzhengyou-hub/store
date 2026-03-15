package com.yourname.mall.modules.order.entity;

import lombok.Data;

@Data
public class Order {

    private Long id;
    private String orderSn;
    private Integer status;
    private Long payAmountCent;
}
