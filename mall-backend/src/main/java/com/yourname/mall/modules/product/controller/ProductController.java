package com.yourname.mall.modules.product.controller;

import com.yourname.mall.common.Result;
import com.yourname.mall.modules.product.dto.ProductDetailResponse;
import com.yourname.mall.modules.product.dto.ProductHomeResponse;
import com.yourname.mall.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/home")
    public Result<ProductHomeResponse> home(@RequestParam(name = "limit", defaultValue = "8") int limit) {
        return Result.success(productService.getHomeProducts(limit));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailResponse> detail(@PathVariable("id") Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @GetMapping("/detail/{id}")
    public Result<ProductDetailResponse> detailAlias(@PathVariable("id") Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}
