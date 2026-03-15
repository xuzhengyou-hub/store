package com.yourname.mall.modules.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartPageResponse {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private List<CartItemResponse> list;
}
