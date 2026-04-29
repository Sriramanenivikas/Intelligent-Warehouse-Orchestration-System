# IWOS Testing Guide

## Overview

This guide provides instructions for running unit tests, integration tests, and end-to-end tests for the Intelligent Warehouse Orchestration System (IWOS).

## Prerequisites

- Java 21+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 16 (via Docker)
- Kafka 7.7.0 (via Docker)
- Redis 7 (via Docker)

## Quick Start

### 1. Start Infrastructure

```bash
# Start core infrastructure (DB, Cache, Message Queue)
docker-compose up -d postgres redis zookeeper kafka schema-registry

# Verify services are running
docker ps
```

### 2. Build All Services

```bash
./scripts/local-dev-build.sh
```

**Options:**
- `--skip-tests`: Build without running tests (faster)
- `--clean`: Force clean build

### 3. Run E2E Tests

```bash
./scripts/e2e-test.sh [--verbose] [--cleanup]
```

**Options:**
- `--verbose`: Show detailed debug output
- `--cleanup`: Clean up test data after completion

## Testing Levels

### Level 1: Unit Tests

Test individual service components in isolation.

**Run unit tests:**
```bash
cd services/commerce/order-intake-service
mvn test
```

**Run all unit tests:**
```bash
mvn test
```

### Level 2: Integration Tests

Test services with real databases and Kafka topics.

**Run integration tests for a service:**
```bash
cd services/commerce/order-intake-service
mvn verify
```

**Key integration tests:**
- `OrderIntakeControllerIntegrationTest` - Order API contract testing
- Database transaction handling
- Kafka event publishing/consumption

### Level 3: End-to-End Tests

Test complete user workflows across multiple services.

**Run E2E tests:**
```bash
./scripts/e2e-test.sh
```

**E2E test flow:**
1. Create order via Order Intake API
2. Verify order is persisted
3. Check inventory reservation
4. Validate order status progression
5. Test idempotency
6. List orders with pagination

## Test Infrastructure

### Shared Test Utilities

All tests can use utilities from `platform/shared-java/service-testkit`:

```java
import com.iwos.testkit.TestDataBuilder;
import com.iwos.testkit.ApiTestClient;
import com.iwos.testkit.SharedTestConfiguration;

// Create test data fluently
var testData = new TestDataBuilder()
    .withCustomerId("cust-123")
    .withQuantity(5)
    .withPrice(100.0)
    .buildOrderId();

// Use test API client
var client = new ApiTestClient()
    .withBaseUrl("http://localhost:8080")
    .withTimeout(5000);
```

### Test Configuration

Services use `@SpringBootTest` with embedded database:

```java
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:55432/iwos_test",
    "spring.kafka.bootstrap-servers=localhost:9092"
})
class OrderIntakeControllerIntegrationTest {
    // Tests here
}
```

## API Contract Testing

### OpenAPI Specifications

All service APIs are documented in `contracts/openapi/`:

- `order-intake-api.yml` - Order acceptance API
- `inventory-ledger-api.yml` - Stock management API
- `payment-api.yml` - Payment processing API

**View specs:**
```bash
# Using Swagger UI (if running locally)
http://localhost:8081/swagger-ui.html  # Order Intake

# Validate OpenAPI
mvn openapi-generator:validate
```

### Contract-Driven Development

When adding new APIs:

1. **Define OpenAPI spec first** (`contracts/openapi/`)
2. **Generate Java code** (if using generator)
3. **Write tests against spec**
4. **Implement service**

## Test Data Management

### Seeding Test Data

```bash
./scripts/seed-data.sh
```

**Inserts:**
- Sample customers
- Test products (SKUs)
- Inventory levels
- Test orders

### Clearing Test Data

```bash
psql -h localhost -p 55432 -U iwos -d iwos_test -c "
  TRUNCATE TABLE orders CASCADE;
  TRUNCATE TABLE order_items CASCADE;
  TRUNCATE TABLE inventory CASCADE;
"
```

## Continuous Integration

Tests are automatically run on:
- Pull request to `develop` or `prod`
- GitHub Actions workflow

**View CI results:** GitHub Actions tab

## Performance Testing

### Load Test Scenarios

```bash
# Load test with JMeter (when available)
mvn jmeter:gui

# Or use k6 for load testing
k6 run tests/performance/order-flow-load.js
```

### Performance Baselines

Expected performance (on local machine):
- Order creation: < 500ms
- Order retrieval: < 100ms
- List orders (100 items): < 1s
- Inventory reservation: < 300ms

## Troubleshooting

### Tests Fail: Connection Refused

**Problem:** Tests can't connect to PostgreSQL or Kafka

**Solution:**
```bash
# Verify containers are running
docker ps | grep -E "postgres|kafka|redis"

# If not running, start them
docker-compose up -d

# Check logs
docker logs iwos-postgres
docker logs iwos-kafka
```

### Tests Fail: Flyway Migration Error

**Problem:** Database migrations are failing

**Solution:**
```bash
# Reset test database
docker-compose exec postgres psql -U iwos -d iwos_test -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Re-run tests (migrations will re-apply)
mvn test
```

### Tests Timeout

**Problem:** Tests taking too long or hanging

**Solution:**
- Increase timeout in test configuration:
  ```java
  @TestPropertySource(properties = {
      "spring.test.database.replace=any",
      "spring.test.mockmvc.print=true",
      "server.servlet.context-path=/api"
  })
  ```

- Check service logs:
  ```bash
  docker logs iwos-postgres
  docker logs iwos-kafka
  ```

## Best Practices

### Writing Tests

1. **Use meaningful names:**
   ```java
   @Test
   @DisplayName("Should accept valid order and return 202 ACCEPTED")
   void testCreateOrderSuccess() {
       // Test body
   }
   ```

2. **Follow AAA pattern (Arrange-Act-Assert):**
   ```java
   // Arrange
   String payload = generateTestData();
   
   // Act
   var response = mockMvc.perform(post("/api/v1/orders")
       .content(payload));
   
   // Assert
   response.andExpect(status().isAccepted());
   ```

3. **Test both happy and sad paths:**
   ```java
   @Test
   void testValidOrder() { /* */ }
   
   @Test
   void testInvalidOrder() { /* */ }
   
   @Test
   void testDuplicateOrder() { /* */ }
   ```

4. **Use test fixtures:**
   ```java
   var builder = new TestDataBuilder()
       .withCustomerId("cust-123");
   ```

### Test Organization

```
services/
├── commerce/
│   └── order-intake-service/
│       ├── src/main/java/...
│       └── src/test/java/
│           └── com/iwos/orderintake/
│               ├── OrderIntakeControllerIntegrationTest.java
│               ├── OrderServiceTest.java
│               └── OrderRepositoryTest.java
```

## Useful Commands

```bash
# Run all tests
mvn test

# Run tests for specific service
cd services/commerce/order-intake-service && mvn test

# Run specific test class
mvn test -Dtest=OrderIntakeControllerIntegrationTest

# Run specific test method
mvn test -Dtest=OrderIntakeControllerIntegrationTest#testCreateOrderSuccess

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run tests in parallel
mvn test -DparallelTestCount=4

# Skip tests during build
mvn clean install -DskipTests
```

## Further Reading

- [API Contracts](../contracts/README.md)
- [Service Architecture](../docs/architecture/service-catalog.md)
- [Development Guide](../CONTRIBUTING.md)

---

**Last Updated:** 2026-04-21  
**Maintainer:** IWOS Team
