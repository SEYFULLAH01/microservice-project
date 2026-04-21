package com.example.order_service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "productService")
    public CompletableFuture<String> placeOrder() {
        return CompletableFuture.supplyAsync(() -> {
            List<?> productList = restTemplate.getForObject("http://PRODUCT-SERVICE/products", List.class);
            return "Sipariş başarılı! Ürünler: " + productList.toString();
        });
    }

    public CompletableFuture<String> fallbackMethod(Exception e) {
        return CompletableFuture.supplyAsync(() -> 
            "Üzgünüz, Ürün Servisinde bir arıza var (Circuit Breaker devrede!) Sistem çökmedi."
        );
    }
}
