package com.yourname.mall.modules.product.mapper;

import com.yourname.mall.modules.product.entity.ProductDetailRow;
import com.yourname.mall.modules.product.entity.ProductHomeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductMapper {

    private static final RowMapper<ProductHomeRow> HOME_ROW_MAPPER = (rs, rowNum) -> new ProductHomeRow(
        rs.getLong("sku_id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("image"),
        rs.getBigDecimal("price")
    );

    private static final RowMapper<ProductDetailRow> DETAIL_ROW_MAPPER = (rs, rowNum) -> new ProductDetailRow(
        rs.getLong("sku_id"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("detail_html"),
        rs.getString("image"),
        rs.getBigDecimal("price"),
        rs.getInt("stock")
    );

    private final JdbcTemplate jdbcTemplate;

    public List<ProductHomeRow> findHomeProducts(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return jdbcTemplate.query(
            "SELECT s.id AS sku_id, p.name, p.description, COALESCE(s.image, p.main_image) AS image, s.price " +
                "FROM pms_sku s INNER JOIN pms_spu p ON s.spu_id = p.id " +
                "ORDER BY s.id ASC LIMIT ?",
            HOME_ROW_MAPPER,
            safeLimit
        );
    }

    public ProductDetailRow findProductDetail(Long skuId) {
        List<ProductDetailRow> rows = jdbcTemplate.query(
            "SELECT s.id AS sku_id, p.name, p.description, p.detail_html, COALESCE(s.image, p.main_image) AS image, s.price, s.stock " +
                "FROM pms_sku s INNER JOIN pms_spu p ON s.spu_id = p.id " +
                "WHERE s.id = ? LIMIT 1",
            DETAIL_ROW_MAPPER,
            skuId
        );
        return rows.isEmpty() ? null : rows.get(0);
    }
}