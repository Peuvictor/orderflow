package com.orderflow.service;

import com.orderflow.model.Product;
import com.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Test
    void shouldRejectProductWithDuplicateSku() {

        // Arrange: preparar o cenário
        ProductRepository repository = mock(ProductRepository.class);

        ProductService service = new ProductService(repository);

        when(repository.existsBySku("SKU-001"))
                .thenReturn(true);

        // Act + Assert: verificar o comportamento esperado
        DuplicateSkuException exception = assertThrows(
                DuplicateSkuException.class,
                () -> service.createProduct(
                        " sku-001 ",
                        "Teclado Mecânico",
                        new BigDecimal("250.00")
                )
        );

        assertTrue(exception.getMessage().contains("SKU-001"));

        // Verificar que o produto não foi salvo
        verify(repository, never()).save(any(Product.class));
    }
}