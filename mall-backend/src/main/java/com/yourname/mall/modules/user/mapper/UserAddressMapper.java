package com.yourname.mall.modules.user.mapper;

import com.yourname.mall.modules.user.entity.UserAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.StringJoiner;

@Repository
@RequiredArgsConstructor
public class UserAddressMapper {

    private static final RowMapper<UserAddress> ADDRESS_ROW_MAPPER = (rs, rowNum) -> new UserAddress(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getString("receiver_name"),
        rs.getString("receiver_phone"),
        rs.getString("province"),
        rs.getString("city"),
        rs.getString("detail_address"),
        rs.getInt("is_default")
    );

    private final JdbcTemplate jdbcTemplate;

    public int insertAddress(
        Long userId,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String detailAddress,
        Integer isDefault
    ) {
        return jdbcTemplate.update(
            "INSERT INTO ums_user_address (user_id, receiver_name, receiver_phone, province, city, detail_address, is_default) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            userId, receiverName, receiverPhone, province, city, detailAddress, isDefault
        );
    }

    public int clearDefaultByUserId(Long userId) {
        return jdbcTemplate.update(
            "UPDATE ums_user_address SET is_default = 0 WHERE user_id = ?",
            userId
        );
    }

    public UserAddress findByUserIdAndId(Long userId, Long id) {
        List<UserAddress> rows = jdbcTemplate.query(
            "SELECT id, user_id, receiver_name, receiver_phone, province, city, detail_address, is_default " +
                "FROM ums_user_address WHERE user_id = ? AND id = ? LIMIT 1",
            ADDRESS_ROW_MAPPER,
            userId,
            id
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int updateAddress(
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String detailAddress,
        Integer isDefault
    ) {
        return jdbcTemplate.update(
            "UPDATE ums_user_address SET receiver_name = ?, receiver_phone = ?, province = ?, city = ?, detail_address = ?, is_default = ? " +
                "WHERE id = ?",
            receiverName, receiverPhone, province, city, detailAddress, isDefault, id
        );
    }

    public Long countByUserId(Long userId) {
        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM ums_user_address WHERE user_id = ?",
            Long.class,
            userId
        );
        return total == null ? 0L : total;
    }

    public List<UserAddress> queryPageByUserId(Long userId, int limit, int offset) {
        return jdbcTemplate.query(
            "SELECT id, user_id, receiver_name, receiver_phone, province, city, detail_address, is_default " +
                "FROM ums_user_address WHERE user_id = ? ORDER BY is_default DESC, id DESC LIMIT ? OFFSET ?",
            ADDRESS_ROW_MAPPER,
            userId,
            limit,
            offset
        );
    }

    public int deleteByUserIdAndId(Long userId, Long id) {
        return jdbcTemplate.update(
            "DELETE FROM ums_user_address WHERE user_id = ? AND id = ?",
            userId,
            id
        );
    }

    public int deleteByUserIdAndIds(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        StringJoiner placeholders = new StringJoiner(",", "(", ")");
        for (int i = 0; i < ids.size(); i++) {
            placeholders.add("?");
        }
        Object[] args = new Object[ids.size() + 1];
        args[0] = userId;
        for (int i = 0; i < ids.size(); i++) {
            args[i + 1] = ids.get(i);
        }
        return jdbcTemplate.update(
            "DELETE FROM ums_user_address WHERE user_id = ? AND id IN " + placeholders,
            args
        );
    }

    public int deleteAllByUserId(Long userId) {
        return jdbcTemplate.update(
            "DELETE FROM ums_user_address WHERE user_id = ?",
            userId
        );
    }
}

