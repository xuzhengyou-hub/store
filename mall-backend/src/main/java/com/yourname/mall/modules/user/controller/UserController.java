package com.yourname.mall.modules.user.controller;

import com.yourname.mall.common.Result;
import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.user.dto.BindPhoneRequest;
import com.yourname.mall.modules.user.dto.LoginRequest;
import com.yourname.mall.modules.user.dto.LoginResponse;
import com.yourname.mall.modules.user.dto.RegisterRequest;
import com.yourname.mall.modules.user.dto.UpdateNicknameRequest;
import com.yourname.mall.modules.user.dto.UserInfoResponse;
import com.yourname.mall.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return Result.success(new LoginResponse(token));
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> info(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractBearerToken(authorization);
        return Result.success(userService.getUserInfoByToken(token));
    }

    @PostMapping("/nickname")
    public Result<UserInfoResponse> updateNickname(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody UpdateNicknameRequest request
    ) {
        String token = extractBearerToken(authorization);
        return Result.success(userService.updateNicknameByToken(token, request.getNickname()));
    }

    @PostMapping("/phone/bind")
    public Result<UserInfoResponse> bindPhone(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody BindPhoneRequest request
    ) {
        String token = extractBearerToken(authorization);
        return Result.success(userService.bindPhoneByToken(token, request.getPhone(), request.getVerificationCode()));
    }

    @PostMapping("/avatar/upload")
    public Result<UserInfoResponse> uploadAvatar(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestParam("file") MultipartFile file
    ) {
        String token = extractBearerToken(authorization);
        return Result.success(userService.uploadAvatarByToken(token, file));
    }

    @GetMapping("/avatar/{fileName}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String fileName) {
        Resource resource = userService.loadAvatar(fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
            .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource);
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
