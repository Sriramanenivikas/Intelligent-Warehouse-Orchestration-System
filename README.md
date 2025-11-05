# 🏗️ IWOS - Intelligent Warehouse Operations System

## 📋 Project Overview

**IWOS** is an industry-grade, event-driven warehouse management system built with modern microservices architecture.

### Phase 1 Features (Current)
- ✅ Order Processing System
- ✅ Real-time Inventory Management
- ✅ Warehouse & Zone Management
- ✅ User Authentication & Authorization
- ✅ Event-Driven Architecture (Kafka)
- ✅ React.js Dashboard
- ✅ REST APIs

### Phase 2 Features (Future)
- ⏳ ML-based Demand Forecasting
- ⏳ Route Optimization
- ⏳ Cloud Deployment (AWS/GCP)
- ⏳ Advanced Analytics

---

## 🏛️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   React.js Frontend                      │
│              (Order Dashboard & Inventory UI)            │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTPS
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  API Gateway (Spring Cloud)              │
│           (Routing, Auth, Rate Limiting)                 │
└────────┬────────────┬──────────┬──────────┬─────────────┘
         │            │          │          │
         ▼            ▼          ▼          ▼
    ┌────────┐  ┌─────────┐ ┌──────┐  ┌──────────┐
    │  Auth  │  │Inventory│ │Order │  │Warehouse │
    │Service │  │Service  │ │Svc   │  │Service   │
    └────┬───┘  └────┬────┘ └──┬───┘  └────┬─────┘
         │           │         │           │
         └───────────┴─────────┴───────────┘
                     │
                     ▼
            ┌─────────────────┐
            │   Kafka Events  │
            │  (Event Bus)    │
            └─────────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
    ┌──────────┐ ┌──────┐  ┌──────────┐
    │PostgreSQL│ │Redis │  │Elasticsearch│
    │  (Data)  │ │(Cache)│ │  (Search)   │
    └──────────┘ └──────┘  └──────────┘
```

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.2+
- **Language**: Java 17
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Spring Cloud Eureka
- **Event Bus**: Apache Kafka
- **Databases**:
  - PostgreSQL (Transactional data)
  - Redis (Caching & Sessions)
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: Swagger/OpenAPI

### Frontend
- **Framework**: React.js 18+
- **State Management**: Redux Toolkit
- **UI Library**: Material-UI (MUI)
- **HTTP Client**: Axios
- **Routing**: React Router v6

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Monitoring**: Prometheus + Grafana (Phase 2)
- **Logging**: ELK Stack (Phase 2)

---

## 📂 Project Structure

```
CAPSTONE_PROJECT/
├── backend/
│   ├── api-gateway/              # API Gateway Service
│   ├── auth-service/             # Authentication & Authorization
│   ├── inventory-service/        # Inventory Management
│   ├── order-service/            # Order Processing
│   └── warehouse-service/        # Warehouse Management
│
├── frontend/
│   └── iwos-dashboard/           # React.js Dashboard
│
├── infrastructure/
│   ├── postgres/                 # Database init scripts
│   ├── kafka/                    # Kafka configurations
│   └── scripts/                  # Utility scripts
│
├── docs/                         # Additional documentation
└── docker-compose.yml            # Local development setup
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+

### 1. Start Infrastructure (Databases + Kafka)
```bash
docker-compose up -d postgres redis kafka
```

### 2. Build & Run Backend Services
```bash
cd backend/auth-service
mvn spring-boot:run

# In separate terminals:
cd backend/inventory-service && mvn spring-boot:run
cd backend/order-service && mvn spring-boot:run
cd backend/warehouse-service && mvn spring-boot:run
cd backend/api-gateway && mvn spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend/iwos-dashboard
npm install
npm start
```

### 4. Access the Application
- **Frontend Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 📊 Core Features

### 1. Order Processing Workflow
```
Order Created → Inventory Reserved → Order Confirmed →
Processing → Picking → Packing → Ready to Ship → Shipped → Delivered
```

### 2. Inventory Management
- Real-time stock tracking
- Multi-warehouse support
- Automatic reorder alerts
- Stock reservation system
- Audit trail for all transactions

### 3. Warehouse Management
- Zone-based organization
- Capacity monitoring
- Worker assignment (manual for Phase 1)

### 4. Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Roles: ADMIN, MANAGER, OPERATOR, VIEWER

---

## 🔌 API Endpoints

### Auth Service (Port 8081)
```
POST   /api/v1/auth/register       # Register new user
POST   /api/v1/auth/login          # Login
POST   /api/v1/auth/refresh        # Refresh JWT
GET    /api/v1/auth/me             # Get current user
```

### Inventory Service (Port 8082)
```
GET    /api/v1/inventory/skus           # List all SKUs
POST   /api/v1/inventory/skus           # Create SKU
GET    /api/v1/inventory/stock          # Get stock levels
POST   /api/v1/inventory/stock/adjust   # Adjust stock
POST   /api/v1/inventory/stock/reserve  # Reserve for order
```

### Order Service (Port 8083)
```
POST   /api/v1/orders              # Create order
GET    /api/v1/orders/{id}         # Get order details
GET    /api/v1/orders              # List orders
PUT    /api/v1/orders/{id}/status  # Update status
DELETE /api/v1/orders/{id}         # Cancel order
```

### Warehouse Service (Port 8084)
```
GET    /api/v1/warehouses          # List warehouses
POST   /api/v1/warehouses          # Create warehouse
GET    /api/v1/warehouses/{id}/zones  # List zones
POST   /api/v1/warehouses/{id}/zones  # Create zone
```

---

## 🗄️ Database Schema

### Users & Auth
- `users` - User accounts
- `roles` - Roles (ADMIN, MANAGER, etc.)
- `user_roles` - User-role mapping
- `permissions` - Fine-grained permissions
- `refresh_tokens` - JWT refresh tokens

### Inventory
- `skus` - Product SKUs
- `inventory` - Stock levels per warehouse
- `inventory_transactions` - Audit trail

### Orders
- `orders` - Order headers
- `order_items` - Order line items
- `order_status_history` - Status changes

### Warehouses
- `warehouses` - Warehouse locations
- `zones` - Storage zones within warehouses

---

## 🎯 Design Principles

### SOLID Principles Applied
- **S**ingle Responsibility: Each service has one clear purpose
- **O**pen/Closed: Services extensible via events, closed to modification
- **L**iskov Substitution: Interfaces used for all external dependencies
- **I**nterface Segregation: Fine-grained service interfaces
- **D**ependency Inversion: Services depend on abstractions, not concrete implementations

### Event-Driven Architecture
- Services communicate via Kafka events
- Loose coupling between services
- Async processing for non-critical operations
- Event replay capability for debugging

---

## 🧪 Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate coverage report
mvn jacoco:report
```

Target: **80%+ code coverage**

---

## 📝 Development Guidelines

### Git Workflow
1. Create feature branch: `git checkout -b feature/your-feature`
2. Commit with clear messages
3. Push to remote: `git push -u origin feature/your-feature`
4. Create Pull Request

### Code Style
- Follow Google Java Style Guide
- Use Prettier for React code formatting
- Run linters before committing

### Commit Message Format
```
type(scope): description

[optional body]
[optional footer]
```
Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

---

## 🔐 Security

- JWT tokens with 30-minute expiration
- Refresh tokens stored in database (7-day expiration)
- Passwords hashed with BCrypt (cost factor: 12)
- CORS configured for frontend origin
- Input validation on all endpoints
- SQL injection prevention via JPA/Hibernate

---

## 📈 Performance Targets

| Metric | Target |
|--------|--------|
| API Response Time (p95) | < 200ms |
| Order Creation | < 2 seconds |
| Inventory Check | < 50ms (with Redis cache) |
| Concurrent Orders | 1,000+ |
| Database Query Time | < 50ms |

---

## 🐛 Troubleshooting

### Kafka Connection Issues
```bash
# Check Kafka is running
docker-compose ps kafka

# View Kafka logs
docker-compose logs -f kafka
```

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Connect to database
docker-compose exec postgres psql -U iwos_user -d iwos_db
```

### Port Already in Use
```bash
# Kill process on port (e.g., 8080)
lsof -ti:8080 | xargs kill -9
```

---

## 🗺️ Roadmap

### Phase 1 (Current) ✅
- Core order processing
- Inventory management
- Basic UI

### Phase 2 (Next)
- ML-based demand forecasting
- Route optimization for picking
- Advanced analytics dashboard
- Mobile app

### Phase 3 (Future)
- IoT device integration
- Robot fleet management
- Real-time video monitoring
- Cloud deployment (AWS/GCP)

---

## 📄 License

This project is developed as a capstone project for educational purposes.

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- React.js Community
- Apache Kafka Documentation
- Industry best practices from Amazon, Walmart, and Alibaba warehouse systems

---

**Built with ❤️ for the future of intelligent warehousing**