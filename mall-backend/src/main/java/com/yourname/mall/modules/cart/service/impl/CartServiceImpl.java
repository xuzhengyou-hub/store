package com.yourname.mall.modules.cart.service.impl;

import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.cart.dto.CartItemResponse;
import com.yourname.mall.modules.cart.dto.CartPageResponse;
import com.yourname.mall.modules.cart.entity.CartItemEntity;
import com.yourname.mall.modules.cart.entity.SkuCartSnapshot;
import com.yourname.mall.modules.cart.mapper.CartMapper;
import com.yourname.mall.modules.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final CartMapper cartMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(Long userId, Long skuId, Integer quantity) {
        SkuCartSnapshot sku = requireSku(skuId);
        CartItemEntity existing = cartMapper.findCartItemByUserAndSku(userId, skuId);
        int nextQuantity = quantity;
        if (existing != null) {
            nextQuantity = existing.getQuantity() + quantity;
        }
        validateQuantityAgainstStock(nextQuantity, sku.getStock());

        if (existing == null) {
            cartMapper.insertCartItem(userId, sku.getSpuId(), skuId, nextQuantity, sku.getPrice());
            return;
        }
        cartMapper.updateCartItem(existing.getId(), nextQuantity, sku.getPrice());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItemEntity cartItem = cartMapper.findCartItemByUserAndId(userId, cartItemId);
        if (cartItem == null) {
            throw new BusinessException("璐墿杞﹀晢鍝佷笉瀛樺湪");
        }
        SkuCartSnapshot sku = requireSku(cartItem.getSkuId());
        validateQuantityAgainstStock(quantity, sku.getStock());
        cartMapper.updateCartQuantity(cartItemId, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public CartPageResponse listByPage(Long userId, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, 20);
        long total = cartMapper.countByUserId(userId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil(total * 1.0 / safeSize);
        int offset = (safePage - 1) * safeSize;

        List<CartItemResponse> list = cartMapper.queryPageByUserId(userId, safeSize, offset).stream()
            .map(row -> new CartItemResponse(
                row.getCartItemId(),
                row.getSpuId(),
                row.getSkuId(),
                row.getName(),
                row.getDescription(),
                row.getImage(),
                row.getQuantity(),
                row.getStock(),
                row.getAddedPrice(),
                row.getCurrentPrice(),
                row.getCurrentPrice().multiply(BigDecimal.valueOf(row.getQuantity()))
            ))
            .toList();
        return new CartPageResponse(safePage, safeSize, total, totalPages, list);
    }

    private SkuCartSnapshot requireSku(Long skuId) {
        SkuCartSnapshot sku = cartMapper.findSkuSnapshot(skuId);
        if (sku == null) {
            throw new BusinessException("鍟嗗搧涓嶅瓨鍦?);
        }
        return sku;
    }

    private void validateQuantityAgainstStock(Integer quantity, Integer stock) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException("璐拱鏁伴噺蹇呴』澶т簬0");
        }
        if (stock == null || stock <= 0) {
            throw new BusinessException("鍟嗗搧搴撳瓨涓嶈冻");
        }
        if (quantity > stock) {
            throw new BusinessException("璐拱鏁伴噺瓒呰繃搴撳瓨");
        }
    }
}
