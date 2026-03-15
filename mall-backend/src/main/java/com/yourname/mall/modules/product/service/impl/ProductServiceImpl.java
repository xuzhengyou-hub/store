package com.yourname.mall.modules.product.service.impl;

import com.yourname.mall.exception.BusinessException;
import com.yourname.mall.modules.product.dto.ProductDetailResponse;
import com.yourname.mall.modules.product.dto.ProductHomeItemResponse;
import com.yourname.mall.modules.product.dto.ProductHomeResponse;
import com.yourname.mall.modules.product.entity.ProductDetailRow;
import com.yourname.mall.modules.product.mapper.ProductMapper;
import com.yourname.mall.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductHomeResponse getHomeProducts(int limit) {
        List<ProductHomeItemResponse> products = productMapper.findHomeProducts(limit).stream()
            .map(row -> new ProductHomeItemResponse(
                row.getSkuId(),
                row.getName(),
                row.getDescription(),
                row.getImage(),
                row.getPrice(),
                buildOriginalPrice(row.getPrice())
            ))
            .toList();

        return new ProductHomeResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long skuId) {
        ProductDetailRow row = productMapper.findProductDetail(skuId);
        if (row == null) {
            throw new BusinessException("商品不存在");
        }

        String image = row.getImage() == null ? "https://placehold.co/600x600/e8e8e8/777777?text=Product" : row.getImage();

        return new ProductDetailResponse(
            row.getSkuId(),
            row.getName(),
            row.getDescription(),
            row.getDetailHtml(),
            image,
            List.of(image),
            row.getPrice(),
            buildOriginalPrice(row.getPrice()),
            row.getStock(),
            Math.max(88, row.getSkuId().intValue() * 3)
        );
    }

    private BigDecimal buildOriginalPrice(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal("1.23")).setScale(2, RoundingMode.HALF_UP);
    }
}