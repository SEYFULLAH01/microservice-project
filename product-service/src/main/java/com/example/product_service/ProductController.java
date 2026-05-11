package com.example.product_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @GetMapping
    public List<String> getProducts() {
        logger.info("Ürün listesi isteği alındı. Ürünler gönderiliyor...");
        // Normal, çalışan sağlıklı sistem.
        return Arrays.asList("Laptop", "Akıllı Telefon", "Tablet", "Kulaklık");
    }
}
