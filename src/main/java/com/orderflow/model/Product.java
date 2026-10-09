package com.orderflow.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Locale;

@Getter
public class Product {

    private String sku;
    private String name;
    private BigDecimal price;
    private boolean active;
    public void deactivate() {
        this.active = false;
    }

    public Product(String sku, String name, BigDecimal price) {

        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException(
                    "SKU cannot be empty"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty"
            );
        }

        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        this.sku = sku.trim().toUpperCase(Locale.ROOT);
        this.name = name.trim();
        this.price = price;
        this.active = true;
    }

}