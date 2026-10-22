package com.orderflow.controller;

import com.orderflow.model.Product;
import com.orderflow.service.ProductService;
import com.orderflow.service.DuplicateSkuException;
import com.orderflow.service.ProductNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProductAndReturn201() throws Exception {

        // Arrange
        Product product = new Product(
                "SKU-001",
                "Teclado Mecânico",
                new BigDecimal("250.00")
        );

        when(productService.createProduct(
                eq("SKU-001"),
                eq("Teclado Mecânico"),
                eq(new BigDecimal("250.00"))
        )).thenReturn(product);

        // Act + Assert
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "sku": "SKU-001",
                                    "name": "Teclado Mecânico",
                                    "price": 250.00
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Teclado Mecânico"))
                .andExpect(jsonPath("$.price").value(250.0))
                .andExpect(jsonPath("$.active").value(true));
    }
    @Test
    void shouldReturn409WhenSkuAlreadyExists() throws Exception {

        // Arrange
        when(productService.createProduct(
                eq("SKU-001"),
                eq("Teclado Mecânico"),
                eq(new BigDecimal("250.00"))
        )).thenThrow(
                new DuplicateSkuException("SKU-001")
        );

        // Act + Assert
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "SKU-001",
                                "name": "Teclado Mecânico",
                                "price": 250.00
                            }
                            """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Product with SKU SKU-001 already exists"));
    }
    @Test
    void shouldReturn400WhenSkuIsBlank() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "",
                                "name": "Teclado Mecânico",
                                "price": 250.00
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "SKU-008",
                                "name": "",
                                "price": 250.00
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
    @Test
    void shouldReturn400WhenPriceIsNull() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "SKU-009",
                                "name": "Mouse Gamer",
                                "price": null
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
    @Test
    void shouldReturn400WhenPriceIsZero() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "SKU-010",
                                "name": "Mouse Gamer",
                                "price": 0
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
    @Test
    void shouldReturn400WhenPriceHasTooManyDecimals() throws Exception {

        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "SKU-011",
                                "name": "Mouse Gamer",
                                "price": 150.999
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }
    @Test
    void shouldReturn200WhenProductExists() throws Exception {

        // Arrange
        Product product = new Product(
                "SKU-007",
                "Monitor Gamer",
                new BigDecimal("1200.00")
        );

        when(productService.getProductBySku("SKU-007"))
                .thenReturn(product);

        // Act + Assert
        mockMvc.perform(
                        get("/products/SKU-007")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-007"))
                .andExpect(jsonPath("$.name").value("Monitor Gamer"))
                .andExpect(jsonPath("$.price").value(1200.0))
                .andExpect(jsonPath("$.active").value(true));

        verify(productService).getProductBySku("SKU-007");
    }
    @Test
    void shouldReturn404WhenProductDoesNotExist() throws Exception {

        // Arrange
        when(productService.getProductBySku("SKU-999"))
                .thenThrow(new ProductNotFoundException("SKU-999"));

        // Act + Assert
        mockMvc.perform(
                        get("/products/SKU-999")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Product with SKU SKU-999 was not found"));

        verify(productService).getProductBySku("SKU-999");
    }
    @Test
    void shouldReturnStandardErrorResponseWhenSkuIsBlank() throws Exception {

        // Act + Assert
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "sku": "",
                                "name": "Teclado Mecânico",
                                "price": 250.00
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("SKU cannot be empty"));

        verifyNoInteractions(productService);
    }
}