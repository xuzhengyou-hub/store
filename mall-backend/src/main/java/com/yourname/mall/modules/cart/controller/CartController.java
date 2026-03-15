package com.yourname.mall.modules.cart.controller;

import com.yourname.mall.common.Result;
import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.cart.dto.AddCartItemRequest;
import com.yourname.mall.modules.cart.dto.CartPageResponse;
import com.yourname.mall.modules.cart.dto.UpdateCartItemQuantityRequest;
import com.yourname.mall.modules.cart.service.CartService;
import com.yourname.mall.modules.user.service.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/add")
    public Result<Void> add(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody AddCartItemRequest request
    ) {
        Long userId = resolveUserId(authorization);
        cartService.addItem(userId, request.getSkuId(), request.getQuantity());
        return Result.success("鍔犲叆璐墿杞︽垚鍔?, null);
    }

    @PutMapping("/update")
    public Result<Void> update(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        Long userId = resolveUserId(authorization);
        cartService.updateQuantity(userId, request.getCartItemId(), request.getQuantity());
        return Result.success("璐墿杞︽暟閲忔洿鏂版垚鍔?, null);
    }

    @GetMapping("/list")
    public Result<CartPageResponse> list(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        Long userId = resolveUserId(authorization);
        return Result.success(cartService.listByPage(userId, page, size));
    }

    private Long resolveUserId(String authorization) {
        String token = extractBearerToken(authorization);
        return jwtTokenService.parseUserId(token);
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException("鏈櫥褰曟垨浠ょ墝缂哄け");
        }
        final String prefix = "Bearer ";
        if (!authorization.startsWith(prefix) || authorization.length() <= prefix.length()) {
            throw new BusinessException("浠ょ墝鏍煎紡涓嶆纭?);
        }
        return authorization.substring(prefix.length()).trim();
    }
}
