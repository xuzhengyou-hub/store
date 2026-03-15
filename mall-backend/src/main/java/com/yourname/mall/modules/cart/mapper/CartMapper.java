package com.yourname.mall.modules.cart.mapper;

import com.yourname.mall.modules.cart.entity.CartItemEntity;
import com.yourname.mall.modules.cart.entity.CartListRow;
import com.yourname.mall.modules.cart.entity.SkuCartSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CartMapper {

    private static final RowMapper<SkuCartSnapshot> SKU_ROW_MAPPER = (rs, rowNum) -> new SkuCartSnapshot(
        rs.getLong("spu_id"),
        rs.getLong("sku_id"),
        rs.getString("image"),
        rs.getBigDecimal("price"),
        rs.getInt("stock")
    );

    private static final RowMapper<CartItemEntity> CART_ITEM_ROW_MAPPER = (rs, rowNum) -> new CartItemEntity(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getLong("spu_id"),
        rs.getLong("sku_id"),
        rs.getInt("quantity"),
        rs.getBigDecimal("price")
    );

    private static final RowMapper<CartListRow> CART_LIST_ROW_MAPPER = (rs, rowNum) -> new CartListRow(
        rs.getLong("cart_item_id"),
        rs.getLong("spu_id"),
        rs.getLong("sku_id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("image"),
        rs.getInt("quantity"),
        rs.getInt("stock"),
        rs.getBigDecimal("added_price"),
        rs.getBigDecimal("current_price")
    );

    private final JdbcTemplate jdbcTemplate;

    public SkuCartSnapshot findSkuSnapshot(Long skuId) {
        List<SkuCartSnapshot> rows = jdbcTemplate.query(
            "SELECT s.spu_id, s.id AS sku_id, COALESCE(s.image, p.main_image) AS image, s.price, s.stock " +
                "FROM pms_sku s INNER JOIN pms_spu p ON s.spu_id = p.id WHERE s.id = ? LIMIT 1",
            SKU_ROW_MAPPER,
            skuId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public CartItemEntity findCartItemByUserAndSku(Long userId, Long skuId) {
        List<CartItemEntity> rows = jdbcTemplate.query(
            "SELECT id, user_id, spu_id, sku_id, quantity, price FROM oms_cart_item " +
                "WHERE user_id = ? AND sku_id = ? LIMIT 1",
            CART_ITEM_ROW_MAPPER,
            userId,
            skuId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public CartItemEntity findCartItemByUserAndId(Long userId, Long cartItemId) {
        List<CartItemEntity> rows = jdbcTemplate.query(
            "SELECT id, user_id, spu_id, sku_id, quantity, price FROM oms_cart_item " +
                "WHERE user_id = ? AND id = ? LIMIT 1",
            CART_ITEM_ROW_MAPPER,
            userId,
            cartItemId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int insertCartItem(Long userId, Long spuId, Long skuId, Integer quantity, java.math.BigDecimal price) {
        return jdbcTemplate.update(
            "INSERT INTO oms_cart_item (user_id, spu_id, sku_id, quantity, price) VALUES (?, ?, ?, ?, ?)",
            userId,
            spuId,
            skuId,
            quantity,
            price
        );
    }

    public int updateCartItem(Long id, Integer quantity, java.math.BigDecimal price) {
        return jdbcTemplate.update(
            "UPDATE oms_cart_item SET quantity = ?, price = ? WHERE id = ?",
            quantity,
            price,
            id
        );
    }

    public int updateCartQuantity(Long id, Integer quantity) {
        return jdbcTemplate.update(
            "UPDATE oms_cart_item SET quantity = ? WHERE id = ?",
            quantity,
            id
        );
    }

    public Long countByUserId(Long userId) {
        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM oms_cart_item WHERE user_id = ?",
            Long.class,
            userId
        );
        return total == null ? 0L : total;
    }

    public List<CartListRow> queryPageByUserId(Long userId, int limit, int offset) {
        return jdbcTemplate.query(
            "SELECT c.id AS cart_item_id, c.spu_id, c.sku_id, p.name, p.description, " +
                "COALESCE(s.image, p.main_image) AS image, c.quantity, s.stock, c.price AS added_price, s.price AS current_price " +
                "FROM oms_cart_item c " +
                "INNER JOIN pms_sku s ON c.sku_id = s.id " +
                "INNER JOIN pms_spu p ON c.spu_id = p.id " +
                "WHERE c.user_id = ? ORDER BY c.update_time DESC, c.id DESC LIMIT ? OFFSET ?",
            CART_LIST_ROW_MAPPER,
            userId,
            limit,
            offset
        );
    }
}
