package com.yourname.mall.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemQuantityRequest {

    @NotNull(message = "cartItemId不能为空")
    private Long cartItemId;

    @NotNull(message = "quantity不能为空")
    @Min(value = 1, message = "quantity必须大于0")
    private Integer quantity;
}

