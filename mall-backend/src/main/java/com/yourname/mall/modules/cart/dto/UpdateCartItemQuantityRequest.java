package com.yourname.mall.modules.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemQuantityRequest {

    @NotNull(message = "cartItemId涓嶈兘涓虹┖")
    private Long cartItemId;

    @NotNull(message = "quantity涓嶈兘涓虹┖")
    @Min(value = 1, message = "quantity蹇呴』澶т簬0")
    private Integer quantity;
}
