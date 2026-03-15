package com.yourname.mall.modules.product.service;

import com.yourname.mall.modules.product.dto.ProductDetailResponse;
import com.yourname.mall.modules.product.dto.ProductHomeResponse;

public interface ProductService {

    ProductHomeResponse getHomeProducts(int limit);

    ProductDetailResponse getProductDetail(Long skuId);
}