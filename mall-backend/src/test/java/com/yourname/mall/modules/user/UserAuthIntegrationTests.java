package com.yourname.mall.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    void cleanup() throws IOException {
        jdbcTemplate.update("DELETE FROM ums_user");

        Path uploadRoot = Paths.get("target/test-uploads");
        if (Files.exists(uploadRoot)) {
            Files.walk(uploadRoot)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
        }
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

    @Test
    void infoShouldReturnProfileByToken() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, phone, nickname, avatar, status, is_deleted) VALUES (?, ?, ?, ?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456"),
            "13800138000",
            "小鱼同学",
            "/default-anime-avatar.svg"
        );

        String token = loginAndGetToken("alice", "123456");

        mockMvc.perform(get("/api/user/info")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("alice"))
            .andExpect(jsonPath("$.data.phone").value("13800138000"))
            .andExpect(jsonPath("$.data.nickname").value("小鱼同学"))
            .andExpect(jsonPath("$.data.avatar").value("/default-anime-avatar.svg"));
    }

    @Test
    void updateNicknameShouldSucceed() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        String token = loginAndGetToken("alice", "123456");

        mockMvc.perform(post("/api/user/nickname")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of("nickname", "新昵称"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.nickname").value("新昵称"));

        String nickname = jdbcTemplate.queryForObject(
            "SELECT nickname FROM ums_user WHERE username = ?",
            String.class,
            "alice"
        );
        assertThat(nickname).isEqualTo("新昵称");
    }

    @Test
    void bindPhoneShouldRequireMockSmsCode() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        String token = loginAndGetToken("alice", "123456");

        mockMvc.perform(post("/api/user/phone/bind")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "phone", "13800138000",
                    "verificationCode", "2222"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("验证码错误"));
    }

    @Test
    void bindPhoneShouldSucceedWhenCodeIs1111() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        String token = loginAndGetToken("alice", "123456");

        mockMvc.perform(post("/api/user/phone/bind")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(Map.of(
                    "phone", "13800138000",
                    "verificationCode", "1111"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.phone").value("13800138000"));
    }

    @Test
    void uploadAvatarShouldSucceed() throws Exception {
        jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            "alice",
            passwordEncoder.encode("123456")
        );
        String token = loginAndGetToken("alice", "123456");

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            "image/png",
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );

        String avatarUrl = objectMapper.readTree(
            mockMvc.perform(multipart("/api/user/avatar/upload")
                    .file(file)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.avatar", not(blankOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString()
        ).path("data").path("avatar").asText();

        assertThat(avatarUrl).startsWith("/api/user/avatar/");

        String savedAvatar = jdbcTemplate.queryForObject(
            "SELECT avatar FROM ums_user WHERE username = ?",
            String.class,
            "alice"
        );
        assertThat(savedAvatar).isEqualTo(avatarUrl);

        mockMvc.perform(get(avatarUrl))
            .andExpect(status().isOk());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "username", username,
            "password", password
        ));
        String loginResponse = mockMvc.perform(post("/api/user/login")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token", not(blankOrNullString())))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(loginResponse).path("data").path("token").asText();
    }
}
