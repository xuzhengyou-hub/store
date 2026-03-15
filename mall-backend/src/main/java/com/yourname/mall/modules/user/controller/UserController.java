package com.yourname.mall.modules.user.controller;

import com.yourname.mall.common.Result;
import com.yourname.mall.modules.user.dto.LoginRequest;
import com.yourname.mall.modules.user.dto.LoginResponse;
import com.yourname.mall.modules.user.dto.RegisterRequest;
import com.yourname.mall.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    public Result<Map<String, String>> info() {
        return Result.success(Map.of("username", "demo-user"));
    }
}
