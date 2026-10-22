package com.orderflow.dto;

import com.orderflow.model.Product;
import java.math.BigDecimal;

public record ProductResponse(
        String sku,
        String name,
        BigDecimal price,
        boolean active
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.isActive()
        );
    }
}