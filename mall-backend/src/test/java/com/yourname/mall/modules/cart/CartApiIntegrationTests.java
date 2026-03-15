package com.yourname.mall.modules.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM oms_cart_item");
        jdbcTemplate.update("DELETE FROM pms_sku");
        jdbcTemplate.update("DELETE FROM pms_spu");
        jdbcTemplate.update("DELETE FROM ums_user");

        jdbcTemplate.update(
            "INSERT INTO ums_user (id, username, password, status, is_deleted) VALUES (?, ?, ?, 1, 0)",
            1L, "alice", passwordEncoder.encode("123456")
        );

        for (long i = 1; i <= 6; i++) {
            jdbcTemplate.update(
                "INSERT INTO pms_spu (id, name, description, main_image, detail_html, publish_status) VALUES (?, ?, ?, ?, ?, 1)",
                i,
                "商品" + i,
                "描述" + i,
                "https://placehold.co/420x420?text=SPU" + i,
                "<p>详情" + i + "</p>"
            );
            jdbcTemplate.update(
                "INSERT INTO pms_sku (id, spu_id, sku_name, sku_code, price, stock, lock_stock, image) VALUES (?, ?, ?, ?, ?, ?, 0, ?)",
                i,
                i,
                "SKU商品" + i,
                "SKU-" + i,
                BigDecimal.valueOf(100 + i),
                50,
                "https://placehold.co/420x420?text=SKU" + i
            );
        }
    }

    @Test
    void addAndListShouldSucceed() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("skuId", 1, "quantity", 2))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/cart/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list", hasSize(1)))
            .andExpect(jsonPath("$.data.list[0].skuId").value(1))
            .andExpect(jsonPath("$.data.list[0].quantity").value(2));
    }

    @Test
    void addSameSkuShouldMergeQuantity() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("skuId", 1, "quantity", 2))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("skuId", 1, "quantity", 3))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart/list")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].quantity").value(5));
    }

    @Test
    void updateQuantityShouldSucceed() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("skuId", 1, "quantity", 1))))
            .andExpect(status().isOk());

        Long cartItemId = jdbcTemplate.queryForObject(
            "SELECT id FROM oms_cart_item WHERE user_id = 1 AND sku_id = 1",
            Long.class
        );

        mockMvc.perform(put("/api/cart/update")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("cartItemId", cartItemId, "quantity", 4))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/cart/list")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].quantity").value(4));
    }

    @Test
    void listShouldPaginateByFive() throws Exception {
        String token = loginAndGetToken();

        for (int i = 1; i <= 6; i++) {
            mockMvc.perform(post("/api/cart/add")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(Map.of("skuId", i, "quantity", 1))))
                .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/cart/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(6))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.list", hasSize(5)));

        mockMvc.perform(get("/api/cart/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "2")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list", hasSize(1)));
    }

    private String loginAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/api/user/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("username", "alice", "password", "123456"))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}

