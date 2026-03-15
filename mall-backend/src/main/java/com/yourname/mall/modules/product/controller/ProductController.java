package com.yourname.mall.modules.product.controller;

import com.yourname.mall.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        return Result.success(Map.of("banners", List.of(), "recommendations", List.of()));
    }
}
