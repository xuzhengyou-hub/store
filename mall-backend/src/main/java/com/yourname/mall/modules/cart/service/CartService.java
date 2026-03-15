package com.yourname.mall.modules.cart.service;

import com.yourname.mall.modules.cart.dto.CartPageResponse;

public interface CartService {

    void addItem(Long userId, Long skuId, Integer quantity);

    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    CartPageResponse listByPage(Long userId, Integer page, Integer size);
}
