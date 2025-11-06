# 🚀 HOW TO RUN IWOS - Complete Guide

This guide will help you build and run the complete IWOS (Intelligent Warehouse Operations System) from scratch.

---

## 📋 Prerequisites

### Required Software
- **Docker** (version 20.10+)
- **Docker Compose** (version 2.0+)
- **Git** (for cloning/pulling updates)

### Recommended (Optional)
- **Java 17** (for local development)
- **Maven 3.8+** (for local builds)
- **Postman** or **curl** (for API testing)

### System Requirements
- **RAM**: Minimum 8GB (16GB recommended)
- **CPU**: 4+ cores
- **Disk Space**: 20GB free space
- **OS**: Linux, macOS, or Windows with WSL2

---

## 🎯 Quick Start (3 Commands)

```bash
# 1. Navigate to project directory
cd /home/user/CAPSTONE_PROJECT

# 2. Run the build and start script
./build-and-run.sh

# 3. Wait for services to start (2-3 minutes first time)
# Once you see "✅ IWOS IS RUNNING!", you're ready!
```

That's it! The script will:
- ✅ Check prerequisites
- ✅ Build all 4 microservices
- ✅ Start PostgreSQL, Redis, Kafka
- ✅ Start all microservices
- ✅ Display all endpoints and commands

---

## 📖 Step-by-Step Manual Setup

If you prefer manual control or the script fails, follow these steps:

### Step 1: Clean Up (Optional)
```bash
# Stop any running containers
docker-compose down -v
```

### Step 2: Build Services
```bash
# Build all microservices
docker-compose build
```

This builds:
- Auth Service (JWT authentication)
- Inventory Service (Stock management)
- Order Service (CQRS + Event Sourcing)
- Warehouse Service (Geospatial allocation)

**Note**: First build takes 5-10 minutes.

### Step 3: Start Infrastructure
```bash
# Start databases and message broker
docker-compose up -d postgres redis zookeeper kafka kafka-ui
```

Wait for health checks (30-60 seconds):
```bash
# Check PostgreSQL
docker exec iwos-postgres pg_isready -U iwos_user

# Check Redis
docker exec iwos-redis redis-cli -a iwos_redis_password ping

# Check Kafka
docker exec iwos-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Step 4: Start Microservices
```bash
# Start all microservices
docker-compose up -d auth-service inventory-service warehouse-service order-service
```

Wait for services (1-2 minutes):
```bash
# Check health of all services
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8082/actuator/health  # Inventory
curl http://localhost:8083/actuator/health  # Order
curl http://localhost:8084/actuator/health  # Warehouse
```

---

## ✅ Verification - Is Everything Running?

### Quick Health Check Script
```bash
#!/bin/bash

echo "🔍 Checking IWOS Services..."

services=(
  "Auth:8081"
  "Inventory:8082"
  "Order:8083"
  "Warehouse:8084"
)

for service in "${services[@]}"; do
  name="${service%%:*}"
  port="${service##*:}"

  if curl -s "http://localhost:$port/actuator/health" | grep -q "UP"; then
    echo "✅ $name Service - Running"
  else
    echo "❌ $name Service - Not responding"
  fi
done
```

### Check Container Status
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Expected output:
```
NAMES                       STATUS          PORTS
iwos-order-service         Up 2 minutes    0.0.0.0:8083->8083/tcp
iwos-warehouse-service     Up 2 minutes    0.0.0.0:8084->8084/tcp
iwos-inventory-service     Up 2 minutes    0.0.0.0:8082->8082/tcp
iwos-auth-service          Up 2 minutes    0.0.0.0:8081->8081/tcp
iwos-kafka                 Up 3 minutes    0.0.0.0:9092-9093->9092-9093/tcp
iwos-postgres              Up 3 minutes    0.0.0.0:5432->5432/tcp
iwos-redis                 Up 3 minutes    0.0.0.0:6379->6379/tcp
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f order-service

# Last 100 lines
docker-compose logs --tail=100 order-service
```

---

## 🧪 Testing the System

### Test 1: Register a User

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@iwos.com",
    "name": "Admin User"
  }'
```

**Expected Response**:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "8f3e2...",
  "expiresIn": 1800,
  "user": {
    "username": "admin",
    "email": "admin@iwos.com",
    "name": "Admin User"
  }
}
```

### Test 2: Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Save the access token** from the response for subsequent requests.

### Test 3: Create a Warehouse

```bash
# Replace YOUR_ACCESS_TOKEN with the token from login
curl -X POST http://localhost:8084/api/v1/warehouses \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -d '{
    "name": "Bangalore Main Warehouse",
    "code": "BLR-001",
    "address": "Electronic City, Bangalore",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560100",
    "latitude": 12.8456,
    "longitude": 77.6603,
    "maxCapacity": 10000,
    "priority": 1,
    "isActive": true
  }'
```

### Test 4: Add Inventory

```bash
curl -X POST http://localhost:8082/api/v1/inventory \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -d '{
    "warehouseId": "WH-...",
    "sku": "SKU-001",
    "quantityOnHand": 1000,
    "reorderPoint": 100
  }'
```

### Test 5: Create an Order (End-to-End Test)

```bash
curl -X POST http://localhost:8083/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN' \
  -d '{
    "customerId": "CUST-123",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+919876543210",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 5,
        "unitPrice": 99.99
      }
    ],
    "deliveryAddress": {
      "line1": "123 Main Street",
      "line2": "Apartment 4B",
      "city": "Bangalore",
      "state": "Karnataka",
      "pincode": "560001",
      "latitude": 12.9716,
      "longitude": 77.5946
    },
    "deliveryType": "EXPRESS",
    "paymentMethod": "ONLINE"
  }'
```

**What happens when you create an order?**
1. ✅ Order Service receives request
2. ✅ Calls Warehouse Service to find nearest warehouse (Haversine algorithm)
3. ✅ Saves order to PostgreSQL (write model)
4. ✅ Publishes `ORDER_CREATED` event to Kafka
5. ✅ Event Store records the event
6. ✅ Inventory Service consumes event and reserves stock
7. ✅ Returns order with warehouse assignment and ETA

**Expected Response**:
```json
{
  "orderId": "ORD-1234567890",
  "customerId": "CUST-123",
  "status": "PENDING",
  "warehouseId": "WH-...",
  "warehouseName": "Bangalore Main Warehouse",
  "totalAmount": 499.95,
  "distanceKm": 5.2,
  "estimatedDeliveryMinutes": 30,
  "items": [...]
}
```

### Test 6: Check Order Status

```bash
curl http://localhost:8083/api/v1/orders/ORD-1234567890 \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### Test 7: View Kafka Events

Open Kafka UI in your browser:
```
http://localhost:8090
```

Navigate to:
- **Topics** → `order.events` → **Messages**
- **Topics** → `inventory.events` → **Messages**
- **Topics** → `warehouse.events` → **Messages**

You'll see all events flowing through the system in real-time!

---

## 🐛 Troubleshooting

### Problem: Services won't start

**Solution 1**: Check Docker resources
```bash
# Increase Docker memory to 8GB
# Docker Desktop → Settings → Resources → Memory → 8GB
```

**Solution 2**: Clean rebuild
```bash
docker-compose down -v
docker system prune -af
./build-and-run.sh
```

### Problem: Port already in use

```bash
# Find what's using the port
lsof -i :8081  # or 8082, 8083, 8084, 5432, etc.

# Kill the process or stop the container
docker stop <container-id>
```

### Problem: Database connection errors

```bash
# Check PostgreSQL is running
docker logs iwos-postgres

# Restart PostgreSQL
docker-compose restart postgres

# Wait for health check
docker exec iwos-postgres pg_isready -U iwos_user
```

### Problem: Kafka connection errors

```bash
# Check Kafka logs
docker logs iwos-kafka

# Restart Kafka and dependent services
docker-compose restart zookeeper kafka
sleep 30
docker-compose restart auth-service inventory-service order-service warehouse-service
```

### Problem: Services build fail

```bash
# Clean Maven cache and rebuild
docker-compose build --no-cache auth-service
```

### Problem: "Out of Memory" errors

```bash
# Increase JVM heap for services
# Edit docker-compose.yml and add:
environment:
  JAVA_OPTS: "-Xmx1024m -Xms512m"
```

---

## 📊 Monitoring & Observability

### Actuator Endpoints (All Services)

```bash
# Health check
curl http://localhost:8083/actuator/health

# Detailed health
curl http://localhost:8083/actuator/health/liveness
curl http://localhost:8083/actuator/health/readiness

# Metrics (Prometheus format)
curl http://localhost:8083/actuator/prometheus

# Info
curl http://localhost:8083/actuator/info
```

### Database Access

**PostgreSQL** (using psql):
```bash
docker exec -it iwos-postgres psql -U iwos_user -d iwos_db

# List tables
\dt

# Query orders
SELECT * FROM orders LIMIT 10;

# Query event store
SELECT * FROM event_store ORDER BY occurred_at DESC LIMIT 10;

# Exit
\q
```

**Redis** (using redis-cli):
```bash
docker exec -it iwos-redis redis-cli -a iwos_redis_password

# List all keys
KEYS *

# Get a value
GET some_key

# Exit
exit
```

### Kafka Topics

```bash
# List topics
docker exec iwos-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume messages from a topic
docker exec iwos-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order.events \
  --from-beginning

# Produce a test message
docker exec -it iwos-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic test-topic
```

---

## 🔒 Security Notes

### Default Credentials (CHANGE IN PRODUCTION!)

- **PostgreSQL**: `iwos_user` / `iwos_password`
- **Redis**: `iwos_redis_password`
- **JWT Secret**: Set via `JWT_SECRET` environment variable

### To Change Credentials:

1. Edit `docker-compose.yml`:
   ```yaml
   environment:
     POSTGRES_PASSWORD: your-secure-password
     JWT_SECRET: your-very-long-secret-key-here
   ```

2. Update application.yml files in each service

3. Rebuild and restart:
   ```bash
   docker-compose down -v
   ./build-and-run.sh
   ```

---

## 🛑 Stopping the System

### Graceful Shutdown
```bash
docker-compose down
```

### Stop and Remove All Data (Clean Slate)
```bash
docker-compose down -v
```

### Stop Individual Service
```bash
docker-compose stop order-service
```

### Restart Individual Service
```bash
docker-compose restart order-service
```

---

## 📚 Additional Resources

- **API Documentation**: http://localhost:8083/swagger-ui.html (replace port for each service)
- **Architecture**: See `FINAL_SYSTEM_ARCHITECTURE.md`
- **System Design**: See `SYSTEM_DESIGN.md`
- **Kong Gateway**: See `docs/KONG_API_GATEWAY.md`
- **Load Testing**: See `LOAD_TESTING_GUIDE.md`

---

## ✅ Success Checklist

After starting the system, verify:

- [ ] All 4 microservices are running (8081-8084)
- [ ] PostgreSQL is accessible (5432)
- [ ] Redis is running (6379)
- [ ] Kafka is running (9092/9093)
- [ ] Kafka UI is accessible (8090)
- [ ] You can register a user
- [ ] You can login and get JWT token
- [ ] You can create a warehouse
- [ ] You can create an order
- [ ] Events appear in Kafka UI
- [ ] Inventory is reserved automatically

---

## 🎉 You're Ready!

Your IWOS system is now running with:
- ✅ **4 Microservices** (Auth, Inventory, Order, Warehouse)
- ✅ **Event-Driven Architecture** (Kafka)
- ✅ **CQRS Pattern** (Command Query Responsibility Segregation)
- ✅ **Event Sourcing** (Append-only event log)
- ✅ **Geospatial Allocation** (Haversine formula)
- ✅ **JWT Authentication** (Stateless security)

**Next Steps**:
1. Run the tests from this guide
2. Check the Swagger UI for each service
3. Monitor events in Kafka UI
4. Run the load testing (see `LOAD_TESTING_GUIDE.md`)
5. Deploy to production (see deployment guide)

**Need Help?** Check the logs: `docker-compose logs -f`

---

**Built for IWOS - Industry-Grade Warehouse Operations System**
