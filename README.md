# Microservice Architecture with Circuit Breaker & Service Discovery

> Group project for **Microservice Based Software Design and Development** course.  
> Built with Java 17, Spring Boot 4, Spring Cloud 2025, Netflix Eureka, Resilience4j, and Docker.

---

## Overview

A fully containerized distributed system composed of three independent microservices. The project demonstrates real-world fault tolerance patterns — specifically the **Circuit Breaker** pattern — to prevent cascading failures in a microservice architecture.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Bridge Network                 │
│                                                         │
│   ┌──────────────┐         ┌──────────────────────┐    │
│   │ Eureka Server│◄────────│   Order Service      │    │
│   │   :8761      │         │   :8082              │    │
│   └──────┬───────┘         │  (Circuit Breaker)   │    │
│          │                 └──────────┬───────────┘    │
│          │  register               calls               │
│          ▼                          ▼                   │
│   ┌──────────────┐         ┌──────────────────────┐    │
│   │   Registry   │────────►│   Product Service    │    │
│   │  (address    │ lookup  │   :8081              │    │
│   │   book)      │         └──────────────────────┘    │
│   └──────────────┘                                     │
└─────────────────────────────────────────────────────────┘
```

### Services

| Service | Port | Role |
|---|---|---|
| `eureka-server` | 8761 | Service registry & discovery |
| `product-service` | 8081 | Exposes product catalog |
| `order-service` | 8082 | Calls product-service; Circuit Breaker enabled |

---

## Tech Stack

- **Java 17**
- **Spring Boot 4** / **Spring Cloud 2025**
- **Netflix Eureka** — Service discovery
- **Resilience4j** — Circuit Breaker & TimeLimiter
- **Docker** & **Docker Compose** — Containerization & orchestration
- **Gradle** — Build tool

---

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### Run the entire system

```bash
docker compose up --build
```

That's it. All three services will start in the correct order.

| URL | Description |
|---|---|
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8081 | Product Service UI |
| http://localhost:8082 | Order Service UI |

### Stop everything

```bash
docker compose down
```

---

## Failure Scenarios

### Scenario 1 — Product Service Goes Down

Simulates a service crash and observes Circuit Breaker behavior.

```bash
# Bring the system down
docker compose stop product-service

# Make a request to order-service → Circuit Breaker opens, fallback activates
# http://localhost:8082/orders  →  "Üzgünüz, Ürün Servisinde bir arıza var (Circuit Breaker devrede!)"

# Restore service
docker compose start product-service
# Circuit Breaker transitions back to Closed → normal traffic resumes
```

**What happens internally:**
1. Order Service calls Product Service → fails
2. Resilience4j tracks failures in a sliding window of 10 calls
3. When failure rate exceeds **50%**, circuit opens
4. All subsequent calls are immediately redirected to the `fallbackMethod` — no threads blocked
5. After **10 seconds**, circuit enters Half-Open state and probes for recovery

### Scenario 2 — Eureka Server Goes Down + Product Service Recreated

Demonstrates client-side caching resilience and its limits.

```bash
# Step 1: Stop Eureka
docker compose stop eureka-server
# Services continue communicating via locally cached addresses → no immediate failure

# Step 2: Completely remove and recreate Product Service (new IP assigned)
docker compose rm -fsv product-service
docker compose up -d --no-deps product-service
# Order Service cannot discover the new address → communication breaks
```

**Key insight:** Eureka clients cache the registry locally. A temporary Eureka outage does not immediately break service-to-service communication. However, if a service is recreated while Eureka is offline, its new IP cannot be propagated — this is the hard limit of client-side caching and demonstrates why service discovery is a critical component.

---

## Circuit Breaker Configuration

Configured in `order-service/src/main/resources/application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50         # opens at 50% failure rate
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
  timelimiter:
    instances:
      productService:
        timeoutDuration: 1s              # calls exceeding 1s are treated as failures
```

| State | Behavior |
|---|---|
| **Closed** | Requests flow normally; failures are monitored |
| **Open** | All calls go directly to fallback; no real calls made |
| **Half-Open** | Limited probe requests allowed; closes or reopens based on result |

---

## Project Structure

```
microservice-project/
├── docker-compose.yml
├── eureka-server/
│   ├── Dockerfile
│   └── src/main/
│       ├── java/.../EurekaServerApplication.java
│       └── resources/application.yml
├── product-service/
│   ├── Dockerfile
│   └── src/main/
│       ├── java/.../ProductController.java
│       └── resources/application.yml
└── order-service/
    ├── Dockerfile
    └── src/main/
        ├── java/.../OrderController.java
        └── resources/application.yml
```
