package com.yourname.mall.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {

    @NotNull(message = "skuId不能为空")
    private Long skuId;

    @NotNull(message = "quantity不能为空")
    @Min(value = 1, message = "quantity必须大于0")
    private Integer quantity;
}

