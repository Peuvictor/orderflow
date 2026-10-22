package com.orderflow.dto;

import java.math.BigDecimal;

public record CreateProductRequest(
        String sku,
        String name,
        BigDecimal price
) {
}