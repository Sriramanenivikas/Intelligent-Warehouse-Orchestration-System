# 🏭 IWOS — Indian Warehouse & Order System

Production-grade microservices platform for warehouse management, order processing, and last-mile delivery.

## Architecture

- **19 Microservices** across Core, Supply Chain, Delivery, and Intelligence domains
- **CQRS + Event Sourcing** for Order Service with Temporal Sagas
- **Event-Driven** architecture via Apache Kafka
- **Service Mesh** with Istio for mTLS and traffic management

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.4, Spring Cloud 2024.0.1 |
| Databases | PostgreSQL 16, MongoDB 7, DynamoDB, Redis 7 |
| Messaging | Apache Kafka (Confluent) |
| Workflow | Temporal.io |
| Search | OpenSearch 2.x |
| AI/ML | AWS SageMaker, DeepAR+, XGBoost |
| Observability | Prometheus, Grafana, Jaeger |
| Container | Docker, Kubernetes (EKS) |
| Service Mesh | Istio |
| CI/CD | GitHub Actions, ArgoCD |

## Quick Start

```bash
# 1. Start infrastructure
docker compose up -d

# 2. Build all modules
mvn clean install -DskipTests

# 3. Run a specific service
mvn spring-boot:run -pl iwos-order-service
```

## Port Assignments

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Order Service | 8082 |
| Cart Service | 8083 |
| Payment Service | 8084 |
| Inventory Service | 8085 |
| WMS Service | 8086 |
| Pick Pack Service | 8087 |
| Dispatch Service | 8088 |
| Route Optimizer | 8089 |
| Tracking Service | 8090 |
| Stock Predictor | 8091 |
| Recommendation | 8092 |
| Pricing Service | 8093 |
| Fraud Detection | 8094 |
| Notification | 8095 |
| Config Server | 8888 |
| Discovery Server | 8761 |
