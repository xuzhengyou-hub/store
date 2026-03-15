package com.yourname.mall.modules.user.mapper;

import com.yourname.mall.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserMapper {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setNickname(rs.getString("nickname"));
        user.setAvatar(rs.getString("avatar"));
        user.setStatus(rs.getInt("status"));
        return user;
    };

    private final JdbcTemplate jdbcTemplate;

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM ums_user WHERE username = ? AND is_deleted = 0",
            Integer.class,
            username
        );
        return count != null && count > 0;
    }

    public User findByUsername(String username) {
        List<User> users = jdbcTemplate.query(
            "SELECT id, username, password, phone, nickname, avatar, status " +
                "FROM ums_user WHERE username = ? AND is_deleted = 0 LIMIT 1",
            USER_ROW_MAPPER,
            username
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public int insertUser(String username, String encodedPassword) {
        return jdbcTemplate.update(
            "INSERT INTO ums_user (username, password, status, is_deleted) VALUES (?, ?, 1, 0)",
            username,
            encodedPassword
        );
    }
}
