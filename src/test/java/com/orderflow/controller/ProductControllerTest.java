package com.orderflow.controller;

import com.orderflow.model.Product;
import com.orderflow.service.ProductService;
import com.orderflow.service.DuplicateSkuException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}