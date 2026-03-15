package com.yourname.mall.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductHomeResponse {

    private List<ProductHomeItemResponse> products;
}