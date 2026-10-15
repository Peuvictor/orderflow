package com.orderflow.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String sku) {
        super("Product with SKU " + sku + " was not found");
    }
}