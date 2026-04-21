package com.example.product_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @GetMapping
    public List<String> getProducts() {
        // Normal, çalışan sağlıklı sistem.
        return Arrays.asList("Laptop", "Akıllı Telefon", "Tablet", "Kulaklık");
    }
}
