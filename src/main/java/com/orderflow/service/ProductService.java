package com.orderflow.service;

import com.orderflow.model.Product;
import com.orderflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Product createProduct(
            String sku,
            String name,
            BigDecimal price
    ) {

        Product product = new Product(sku, name, price);

        if (repository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException(product.getSku());
        }

        return repository.save(product);
    }
    public Product getProductBySku(String sku) {

        String normalizedSku = sku.trim()
                .toUpperCase(Locale.ROOT);

        return repository.findBySku(normalizedSku)
                .orElseThrow();
    }
}