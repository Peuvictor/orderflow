package com.orderflow.controller;

import com.orderflow.dto.CreateProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.model.Product;
import com.orderflow.service.ProductService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {

        Product product = productService.createProduct(
                request.sku(),
                request.name(),
                request.price()
        );

        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}