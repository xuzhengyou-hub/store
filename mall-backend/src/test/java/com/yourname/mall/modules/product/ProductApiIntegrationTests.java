package com.yourname.mall.modules.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupData() {
        jdbcTemplate.update("DELETE FROM pms_sku");
        jdbcTemplate.update("DELETE FROM pms_spu");

        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.update(
                "INSERT INTO pms_spu (id, name, description, main_image, detail_html, publish_status) VALUES (?, ?, ?, ?, ?, 1)",
                i,
                "商品" + i,
                "这是商品" + i + "的简要描述",
                "https://placehold.co/420x420?text=SPU" + i,
                "<p>商品" + i + "详情图文</p>"
            );
            jdbcTemplate.update(
                "INSERT INTO pms_sku (id, spu_id, sku_name, sku_code, price, stock, lock_stock, image) VALUES (?, ?, ?, ?, ?, ?, 0, ?)",
                i,
                i,
                "SKU商品" + i,
                "SKU-" + i,
                1000 + i,
                100 + i,
                "https://placehold.co/420x420?text=SKU" + i
            );
        }
    }

    @Test
    void homeShouldReturnTop8Products() throws Exception {
        mockMvc.perform(get("/api/product/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.products", hasSize(8)))
            .andExpect(jsonPath("$.data.products[0].id").value(1))
            .andExpect(jsonPath("$.data.products[0].name").value("商品1"));
    }

    @Test
    void detailShouldReturnProductDetail() throws Exception {
        mockMvc.perform(get("/api/product/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(3))
            .andExpect(jsonPath("$.data.name").value("商品3"))
            .andExpect(jsonPath("$.data.stock").value(103))
            .andExpect(jsonPath("$.data.gallery", hasSize(1)));
    }
}