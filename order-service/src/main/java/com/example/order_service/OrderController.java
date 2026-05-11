package com.example.order_service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "productService")
    public CompletableFuture<String> placeOrder() {
        logger.info("Sipariş isteği geldi. Ürün servisine (PRODUCT-SERVICE) bağlanılıyor...");
        return CompletableFuture.supplyAsync(() -> {
            List<?> productList = restTemplate.getForObject("http://PRODUCT-SERVICE/products", List.class);
            logger.info("Ürün servisinden cevap başarıyla alındı. Sipariş tamamlanıyor.");
            return "Sipariş başarılı! Ürünler: " + productList.toString();
        });
    }

    public CompletableFuture<String> fallbackMethod(Exception e) {
        logger.error("DİKKAT: Ürün servisine ulaşılamadı! Devre Kesici (Circuit Breaker) çalıştı. Hata: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> 
            "Üzgünüz, Ürün Servisinde bir arıza var (Circuit Breaker devrede!) Sistem çökmedi."
        );
    }
}
