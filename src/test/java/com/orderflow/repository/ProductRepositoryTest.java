package com.orderflow.repository;

import com.orderflow.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProductRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProductBySku() {

        Product product = new Product(
                "SKU-001",
                "Teclado Mecânico",
                new BigDecimal("250.00")
        );

        productRepository.saveAndFlush(product);

        var result = productRepository.findBySku("SKU-001");

        assertTrue(result.isPresent());
        assertEquals("Teclado Mecânico", result.get().getName());
    }
    @Test
    void shouldRejectDuplicateSku() {

        Product firstProduct = new Product(
                "SKU-001",
                "Teclado Mecânico",
                new BigDecimal("250.00")
        );

        Product secondProduct = new Product(
                "SKU-001",
                "Mouse Gamer",
                new BigDecimal("150.00")
        );

        productRepository.saveAndFlush(firstProduct);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(secondProduct)
        );
    }
}