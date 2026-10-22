package com.orderflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "SKU cannot be empty")
        String sku,

        @NotBlank(message = "Product name cannot be empty")
        String name,

        @NotNull(message = "Product price cannot be null")
        @DecimalMin(
                value = "0.00",
                inclusive = false,
                message = "Product price must be greater than zero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Product price must have at most two decimal places"
        )
        BigDecimal price

) {
}