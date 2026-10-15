package com.orderflow.service;

import com.orderflow.model.Product;
import com.orderflow.repository.ProductRepository;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.util.Optional;


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
    @Test
    void shouldCreateProductWhenSkuIsAvailable() {

        // Arrange: preparar o repositório simulado
        ProductRepository repository = mock(ProductRepository.class);

        ProductService service = new ProductService(repository);

        when(repository.existsBySku("SKU-006"))
                .thenReturn(false);

        when(repository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: executar o cadastro
        Product createdProduct = service.createProduct(
                " sku-006 ",
                "Mouse Gamer",
                new BigDecimal("150.00")
        );

        // Assert: verificar o produto criado
        assertNotNull(createdProduct);
        assertEquals("SKU-006", createdProduct.getSku());
        assertEquals("Mouse Gamer", createdProduct.getName());
        assertEquals(
                new BigDecimal("150.00"),
                createdProduct.getPrice()
        );
        assertTrue(createdProduct.isActive());

        // Verificar as interações com o Repository
        verify(repository).existsBySku("SKU-006");
        verify(repository).save(any(Product.class));
    }
    @Test
    void shouldFindProductBySku() {

        // Arrange
        ProductRepository repository = mock(ProductRepository.class);
        ProductService service = new ProductService(repository);

        Product product = new Product(
                "SKU-007",
                "Monitor Gamer",
                new BigDecimal("1200.00")
        );

        when(repository.findBySku("SKU-007"))
                .thenReturn(Optional.of(product));

        // Act
        Product result = service.getProductBySku(" sku-007 ");

        // Assert
        assertSame(product, result);

        verify(repository).findBySku("SKU-007");
    }
    @Test
    void shouldThrowExceptionWhenProductIsNotFound() {

        // Arrange
        ProductRepository repository = mock(ProductRepository.class);
        ProductService service = new ProductService(repository);

        when(repository.findBySku("SKU-999"))
                .thenReturn(Optional.empty());

        // Act + Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> service.getProductBySku(" sku-999 ")
        );

        assertTrue(exception.getMessage().contains("SKU-999"));

        verify(repository).findBySku("SKU-999");
    }
    @Test
    void shouldRejectBlankSkuWhenSearching() {

        ProductRepository repository = mock(ProductRepository.class);
        ProductService service = new ProductService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getProductBySku("   ")
        );

        verifyNoInteractions(repository);
    }
}