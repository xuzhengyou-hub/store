package com.yourname.mall.modules.user.controller;

import com.yourname.mall.common.Result;
import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.user.dto.AddUserAddressRequest;
import com.yourname.mall.modules.user.dto.DeleteBatchAddressRequest;
import com.yourname.mall.modules.user.dto.UpdateUserAddressRequest;
import com.yourname.mall.modules.user.dto.UserAddressPageResponse;
import com.yourname.mall.modules.user.service.JwtTokenService;
import com.yourname.mall.modules.user.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/add")
    public Result<Void> add(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody AddUserAddressRequest request
    ) {
        Long userId = resolveUserId(authorization);
        userAddressService.addAddress(userId, request);
        return Result.success("新增地址成功", null);
    }

    @PutMapping("/update")
    public Result<Void> update(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody UpdateUserAddressRequest request
    ) {
        Long userId = resolveUserId(authorization);
        userAddressService.updateAddress(userId, request);
        return Result.success("更新地址成功", null);
    }

    @GetMapping("/list")
    public Result<UserAddressPageResponse> list(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        Long userId = resolveUserId(authorization);
        return Result.success(userAddressService.listAddress(userId, page, size));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteOne(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @PathVariable("id") Long id
    ) {
        Long userId = resolveUserId(authorization);
        userAddressService.deleteOne(userId, id);
        return Result.success("删除地址成功", null);
    }

    @DeleteMapping("/delete_batch")
    public Result<Void> deleteBatch(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody DeleteBatchAddressRequest request
    ) {
        Long userId = resolveUserId(authorization);
        userAddressService.deleteBatch(userId, request.getIds());
        return Result.success("批量删除地址成功", null);
    }

    @DeleteMapping("/delete_all")
    public Result<Void> deleteAll(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = resolveUserId(authorization);
        userAddressService.deleteAll(userId);
        return Result.success("清空地址成功", null);
    }

    private Long resolveUserId(String authorization) {
        String token = extractBearerToken(authorization);
        return jwtTokenService.parseUserId(token);
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException("未登录或令牌缺失");
        }
        final String prefix = "Bearer ";
        if (!authorization.startsWith(prefix) || authorization.length() <= prefix.length()) {
            throw new BusinessException("令牌格式不正确");
        }
        return authorization.substring(prefix.length()).trim();
    }
}

