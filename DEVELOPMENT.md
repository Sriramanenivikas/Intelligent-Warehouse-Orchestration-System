# IWOS Development Guide

## 🚀 Quick Start

### Option 1: Start Everything Automatically
```bash
./start-all.sh
```
This will start:
- PostgreSQL, Redis, Kafka (via Docker)
- All 4 backend microservices
- React frontend

Wait 30-60 seconds, then access:
- **Frontend**: http://localhost:3000
- **Login**: admin@iwos.com / Admin@123

### Option 2: Start Infrastructure Only (For Development)
```bash
./quick-start.sh
```
Then manually start services you're working on:
```bash
# Terminal 1 - Auth Service
cd backend/auth-service
mvn spring-boot:run

# Terminal 2 - Inventory Service
cd backend/inventory-service
mvn spring-boot:run

# Terminal 3 - Order Service
cd backend/order-service
mvn spring-boot:run

# Terminal 4 - Frontend
cd frontend/iwos-dashboard
npm install
npm run dev
```

### Stop Everything
```bash
./stop-all.sh
```

---

## 📋 Prerequisites

### Required
- **Java 17+** - `java -version`
- **Maven 3.8+** - `mvn -version`
- **Node.js 18+** - `node -version`
- **Docker & Docker Compose** - `docker --version`

### Optional
- **PostgreSQL Client** (for direct DB access)
- **Redis CLI** (for cache debugging)
- **Postman** (for API testing)

---

## 🏗️ Project Structure

```
CAPSTONE_PROJECT/
├── backend/
│   ├── auth-service/          # Port 8081 - Authentication
│   ├── inventory-service/     # Port 8082 - Inventory Management
│   ├── order-service/         # Port 8083 - Order Processing
│   ├── warehouse-service/     # Port 8084 - Warehouse Management
│   └── pom.xml               # Parent POM
│
├── frontend/
│   └── iwos-dashboard/        # Port 3000 - React Dashboard
│
├── infrastructure/
│   ├── postgres/init/         # Database init scripts
│   ├── kafka/                 # Kafka configurations
│   └── scripts/               # Utility scripts
│
├── docs/                      # Additional documentation
├── logs/                      # Application logs
├── docker-compose.yml         # Infrastructure definition
├── start-all.sh              # Start everything
├── stop-all.sh               # Stop everything
└── quick-start.sh            # Start infrastructure only
```

---

## 🔧 Development Workflow

### 1. Database Changes

**Direct Database Access:**
```bash
docker-compose exec postgres psql -U iwos_user -d iwos_db

# Common commands:
\dt                          # List all tables
\d users                     # Describe users table
SELECT * FROM users;         # Query data
```

**Add New Tables:**
Edit `infrastructure/postgres/init/01-init-database.sql` and restart PostgreSQL:
```bash
docker-compose restart postgres
```

### 2. Backend Development

**Build All Services:**
```bash
cd backend
mvn clean install
```

**Build Single Service:**
```bash
cd backend/auth-service
mvn clean install
```

**Run Tests:**
```bash
mvn test
```

**Generate Code Coverage:**
```bash
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

**Add New Entity/Service:**
Use the code generator:
```bash
python3 infrastructure/scripts/generate-microservices.py
```

### 3. Frontend Development

**Install Dependencies:**
```bash
cd frontend/iwos-dashboard
npm install
```

**Start Dev Server (Hot Reload):**
```bash
npm run dev
```

**Build for Production:**
```bash
npm run build
```

**Lint Code:**
```bash
npm run lint
```

### 4. API Testing

**Using Swagger UI:**
- Auth Service: http://localhost:8081/api/v1/swagger-ui.html
- Inventory: http://localhost:8082/api/v1/swagger-ui.html
- Orders: http://localhost:8083/api/v1/swagger-ui.html
- Warehouse: http://localhost:8084/api/v1/swagger-ui.html

**Using cURL:**
```bash
# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@iwos.com","password":"Admin@123"}'

# Get Orders (with token)
curl -X GET http://localhost:8083/api/v1/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -ti:8081

# Kill process
kill -9 $(lsof -ti:8081)
```

### Docker Issues
```bash
# Reset Docker containers
docker-compose down -v
docker-compose up -d

# View logs
docker-compose logs -f postgres
docker-compose logs -f kafka
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check credentials in application.yml
# Default: iwos_user / iwos_password / iwos_db
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker-compose ps kafka

# List topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# View Kafka UI
open http://localhost:8090
```

### Maven Build Fails
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

### Frontend Build Fails
```bash
# Clear node_modules and reinstall
cd frontend/iwos-dashboard
rm -rf node_modules package-lock.json
npm install
```

---

## 📊 Monitoring & Debugging

### View Logs
```bash
# Backend logs
tail -f logs/auth-service.log
tail -f logs/order-service.log

# Frontend logs
tail -f logs/frontend.log

# Docker logs
docker-compose logs -f
```

### Health Checks
```bash
# Auth Service
curl http://localhost:8081/api/v1/actuator/health

# All Services
for port in 8081 8082 8083 8084; do
  echo "Port $port:"
  curl -s http://localhost:$port/api/v1/actuator/health | jq
done
```

### Database Queries
```sql
-- Check user count
SELECT COUNT(*) FROM users;

-- Check orders
SELECT id, order_number, status, total_amount FROM orders LIMIT 10;

-- Check inventory
SELECT s.sku_code, i.quantity_on_hand, i.quantity_reserved
FROM inventory i
JOIN skus s ON i.sku_id = s.id;
```

---

## 🧪 Testing

### Unit Tests
```bash
cd backend/auth-service
mvn test
```

### Integration Tests
```bash
mvn verify
```

### End-to-End Testing
1. Start all services: `./start-all.sh`
2. Open frontend: http://localhost:3000
3. Login with: admin@iwos.com / Admin@123
4. Test workflows:
   - Create order
   - View inventory
   - Update order status

---

## 🔒 Security Notes

### Default Credentials
- **Database**: iwos_user / iwos_password
- **Redis**: iwos_redis_password
- **Admin User**: admin@iwos.com / Admin@123

**⚠️ CHANGE THESE IN PRODUCTION!**

### JWT Configuration
Edit `backend/auth-service/src/main/resources/application.yml`:
```yaml
jwt:
  secret: your-256-bit-secret-key
  access-token-expiration: 1800000  # 30 minutes
  refresh-token-expiration: 604800000  # 7 days
```

---

## 📈 Performance Optimization

### Enable Redis Caching
Already configured in services. Verify:
```bash
# Connect to Redis
docker-compose exec redis redis-cli -a iwos_redis_password

# Check cached keys
KEYS *

# View cache hit/miss ratio
INFO stats
```

### Database Indexing
All critical indexes are created in `01-init-database.sql`.
To add more:
```sql
CREATE INDEX idx_custom ON table_name(column_name);
```

### Frontend Build Optimization
```bash
cd frontend/iwos-dashboard
npm run build
# Serves optimized, minified assets
```

---

## 🚢 Deployment

### Local Docker Build
```bash
# Build backend images
cd backend/auth-service
docker build -t iwos/auth-service:latest .

# Build frontend image
cd frontend/iwos-dashboard
docker build -t iwos/frontend:latest .
```

### Production Checklist
- [ ] Change all default passwords
- [ ] Generate new JWT secret (256+ bits)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS for production domains
- [ ] Set up log aggregation (ELK)
- [ ] Configure backups (PostgreSQL)
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Load test with JMeter/Gatling

---

## 🎓 Learning Resources

### Spring Boot
- [Official Docs](https://spring.io/projects/spring-boot)
- [Baeldung Spring Tutorials](https://www.baeldung.com/spring-boot)

### React
- [Official Docs](https://react.dev/)
- [React Router](https://reactrouter.com/)

### Apache Kafka
- [Kafka Quickstart](https://kafka.apache.org/quickstart)
- [Spring Kafka](https://spring.io/projects/spring-kafka)

### PostgreSQL
- [Official Docs](https://www.postgresql.org/docs/)
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)

---

## 🤝 Contributing

### Code Style
- **Backend**: Google Java Style Guide
- **Frontend**: Prettier + ESLint
- **Commits**: Conventional Commits format

### Adding Features
1. Create feature branch: `git checkout -b feature/your-feature`
2. Implement feature
3. Add tests (target: 80%+ coverage)
4. Update documentation
5. Create Pull Request

---

## 📞 Support

### Common Issues
- Check `TROUBLESHOOTING.md`
- Search GitHub Issues
- Review logs in `logs/` directory

### Report Bugs
1. Check existing issues
2. Provide logs and error messages
3. Include steps to reproduce

---

**Happy Coding! 🚀**
