package com.yourname.mall.modules.order.controller;

import com.yourname.mall.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestBody Map<String, Object> payload) {
        return Result.success(Map.of("orderSn", "TODO_ORDER_SN"));
    }
}
