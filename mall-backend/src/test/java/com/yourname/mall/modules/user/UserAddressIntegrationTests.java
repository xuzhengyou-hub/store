package com.yourname.mall.modules.user;

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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAddressIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupData() {
        jdbcTemplate.update("DELETE FROM ums_user_address");
        jdbcTemplate.update("DELETE FROM ums_user");
        jdbcTemplate.update(
            "INSERT INTO ums_user (id, username, password, status, is_deleted) VALUES (?, ?, ?, 1, 0)",
            1L,
            "alice",
            passwordEncoder.encode("123456")
        );
    }

    @Test
    void addAndListAddressShouldSucceed() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(post("/api/user/address/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "receiverName", "张三",
                    "receiverPhone", "13800138000",
                    "province", "浙江省",
                    "city", "杭州市",
                    "detailAddress", "滨江区海创园",
                    "isDefault", 1
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/user/address/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.list[0].receiverName").value("张三"))
            .andExpect(jsonPath("$.data.list[0].isDefault").value(1));
    }

    @Test
    void updateAddressShouldSucceed() throws Exception {
        String token = loginAndGetToken();
        createAddress(token, "李四", "13800138001", "广东省", "深圳市", "南山区科技园", 0);

        Long addressId = jdbcTemplate.queryForObject(
            "SELECT id FROM ums_user_address WHERE user_id = 1 LIMIT 1",
            Long.class
        );

        mockMvc.perform(put("/api/user/address/update")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "id", addressId,
                    "receiverName", "李四-新",
                    "receiverPhone", "13800138009",
                    "province", "广东省",
                    "city", "广州市",
                    "detailAddress", "天河区珠江新城",
                    "isDefault", 1
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/user/address/list")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].receiverName").value("李四-新"))
            .andExpect(jsonPath("$.data.list[0].receiverPhone").value("13800138009"))
            .andExpect(jsonPath("$.data.list[0].isDefault").value(1));
    }

    @Test
    void listShouldPaginateByFive() throws Exception {
        String token = loginAndGetToken();
        for (int i = 1; i <= 6; i++) {
            createAddress(token, "用户" + i, "1380013800" + i, "省" + i, "市" + i, "地址" + i, 0);
        }

        mockMvc.perform(get("/api/user/address/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(6))
            .andExpect(jsonPath("$.data.totalPages").value(2))
            .andExpect(jsonPath("$.data.list", hasSize(5)));

        mockMvc.perform(get("/api/user/address/list")
                .header("Authorization", "Bearer " + token)
                .param("page", "2")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list", hasSize(1)));
    }

    @Test
    void deleteOneBatchAndAllShouldSucceed() throws Exception {
        String token = loginAndGetToken();
        createAddress(token, "A", "13800138001", "省A", "市A", "地址A", 0);
        createAddress(token, "B", "13800138002", "省B", "市B", "地址B", 0);
        createAddress(token, "C", "13800138003", "省C", "市C", "地址C", 0);

        List<Long> ids = jdbcTemplate.queryForList(
            "SELECT id FROM ums_user_address WHERE user_id = 1 ORDER BY id ASC",
            Long.class
        );

        mockMvc.perform(delete("/api/user/address/delete/{id}", ids.get(0))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/user/address/delete_batch")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("ids", List.of(ids.get(1))))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/user/address/delete_all")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM ums_user_address WHERE user_id = 1",
            Integer.class
        );
        assertThat(count).isEqualTo(0);
    }

    private void createAddress(
        String token,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String detailAddress,
        int isDefault
    ) throws Exception {
        mockMvc.perform(post("/api/user/address/add")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "receiverName", receiverName,
                    "receiverPhone", receiverPhone,
                    "province", province,
                    "city", city,
                    "detailAddress", detailAddress,
                    "isDefault", isDefault
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/api/user/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "alice",
                    "password", "123456"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
