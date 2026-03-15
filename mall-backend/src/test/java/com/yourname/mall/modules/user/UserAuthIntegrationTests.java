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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM ums_user");
    }

    @Test
    void registerSuccess() throws Exception {
        Map<String, Object> request = Map.of(
            "username", "alice",
            "password", "123456"
        );

        mockMvc.perform(post("/api/user/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("注册成功"));

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM ums_user WHERE username = ?",
            Integer.class,
            "alice"
        );
        String encoded = jdbcTemplate.queryForObject(
            "SELECT password FROM ums_user WHERE username = ?",
            String.class,
            "alice"
        );

        assertThat(count).isEqualTo(1);
        assertThat(encoded).isNotEqualTo("123456");
        assertThat(passwordEncoder.matches("123456", encoded)).isTrue();
    }

    @Test
    void registerDuplicateUsernameShouldFail() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        Map<String, Object> request = Map.of(
            "username", "alice",
            "password", "123456"
        );

        mockMvc.perform(post("/api/user/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("用户名已被注册"));
    }

    @Test
    void loginSuccess() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        Map<String, Object> request = Map.of(
            "username", "alice",
            "password", "123456"
        );

        mockMvc.perform(post("/api/user/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loginWrongPasswordShouldFail() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        Map<String, Object> request = Map.of(
            "username", "alice",
            "password", "wrong-password"
        );

        mockMvc.perform(post("/api/user/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
}
