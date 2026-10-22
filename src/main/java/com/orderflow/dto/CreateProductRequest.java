package com.orderflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "SKU cannot be empty")
        String sku,

        @NotBlank(message = "Product name cannot be empty")
        String name,

        @NotNull(message = "Product price cannot be null")
        BigDecimal price
) {
}