package com.orderflow;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndRetrieveProduct() throws Exception {

        // Cadastrar produto
        mockMvc.perform(
                        post("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "sku": "SKU-INT-001",
                                    "name": "Teclado Mecânico",
                                    "price": 250.00
                                }
                                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-INT-001"))
                .andExpect(jsonPath("$.active").value(true));

        // Consultar o mesmo produto
        mockMvc.perform(
                        get("/products/SKU-INT-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-INT-001"))
                .andExpect(jsonPath("$.name").value("Teclado Mecânico"))
                .andExpect(jsonPath("$.price").value(250.0));
    }
}