package com.orderflow.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductWithValidData() {
        Product product = new Product(
                "SKU-001",
                "Teclado Mecânico",
                new BigDecimal("250.00")
        );

        assertEquals("SKU-001", product.getSku());
        assertEquals("Teclado Mecânico", product.getName());
        assertEquals(
                new BigDecimal("250.00"),
                product.getPrice()
        );
        assertTrue(product.isActive());
    }

    @Test
    void shouldRejectProductWithNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "SKU-002",
                        "Mouse",
                        new BigDecimal("-50.00")
                )
        );
    }

    @Test
    void shouldRejectProductWithEmptySku() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "",
                        "Monitor",
                        new BigDecimal("800.00")
                )
        );
    }

    @Test
    void shouldNormalizeSku() {
        Product product = new Product(
                " sku-003 ",
                "Webcam",
                new BigDecimal("200.00")
        );

        assertEquals("SKU-003", product.getSku());
    }

    @Test
    void shouldRejectProductWithEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        "SKU-004",
                        "",
                        new BigDecimal("100.00")
                )
        );
    }
    @Test
    void shouldDeactivateProduct() {
        Product product = new Product(
                "SKU-005",
                "Teclado Mecânico",
                new BigDecimal("250.00")
        );

        product.deactivate();

        assertFalse(product.isActive());
    }}
