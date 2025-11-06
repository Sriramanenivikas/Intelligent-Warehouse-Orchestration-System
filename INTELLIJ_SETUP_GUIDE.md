# 🚀 IntelliJ IDEA Setup Guide for IWOS

Complete guide to open, configure, and run the IWOS project in IntelliJ IDEA.

---

## 📋 Prerequisites

- **IntelliJ IDEA** (Community or Ultimate Edition)
- **Java 17** installed (verify: `java --version`)
- **Maven** (IntelliJ has built-in Maven)
- **Docker Desktop** running (for PostgreSQL, Redis, Kafka)
- **Lombok Plugin** (should be pre-installed in modern IntelliJ)

---

## 🔧 Step 1: Open Project in IntelliJ IDEA

### Option A: Import Existing Project

1. Open IntelliJ IDEA
2. Click **Open** (not "New Project")
3. Navigate to: `/home/user/CAPSTONE_PROJECT`
4. Select the root `backend/pom.xml` file
5. Click **Open as Project**
6. IntelliJ will detect it's a Maven multi-module project

### Option B: From Command Line

```bash
cd /home/user/CAPSTONE_PROJECT/backend
idea .
```

---

## ⚙️ Step 2: Configure IntelliJ

### 2.1 Enable Annotation Processing (For Lombok)

1. Go to: **File** → **Settings** (Windows/Linux) or **IntelliJ IDEA** → **Preferences** (Mac)
2. Navigate to: **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. Check ✅ **Enable annotation processing**
4. Click **Apply**

### 2.2 Set Java SDK

1. Go to: **File** → **Project Structure** (Ctrl+Alt+Shift+S)
2. Under **Project Settings** → **Project**
3. Set **SDK** to **Java 17**
4. Set **Language level** to **17 - Sealed types, always-strict floating-point semantics**
5. Click **OK**

### 2.3 Configure Maven

1. Go to: **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
2. Set **Maven home path** (use bundled Maven or specify path)
3. Check ✅ **Use settings file** (default is fine)
4. Under **Importing**:
   - Check ✅ **Import Maven projects automatically**
   - Check ✅ **Automatically download Sources**
   - Check ✅ **Automatically download Documentation**
5. Click **Apply**

---

## 📦 Step 3: Build the Project

### 3.1 Maven Reimport

1. Open Maven tool window: **View** → **Tool Windows** → **Maven**
2. Click the **Reload All Maven Projects** button (🔄 icon)
3. Wait for IntelliJ to download all dependencies (first time takes 2-3 minutes)

### 3.2 Build All Modules

In Maven tool window:
1. Expand **iwos-parent**
2. Expand **Lifecycle**
3. Double-click **clean** (removes old builds)
4. Double-click **install** (builds all modules)

**Or use terminal**:
```bash
cd /home/user/CAPSTONE_PROJECT/backend
mvn clean install -DskipTests
```

Expected output:
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for iwos-parent 1.0.0-SNAPSHOT:
[INFO]
[INFO] iwos-parent ........................................ SUCCESS [  0.523 s]
[INFO] IWOS - Auth Service ................................ SUCCESS [  5.234 s]
[INFO] IWOS - Inventory Service ........................... SUCCESS [  4.123 s]
[INFO] IWOS - Order Service ............................... SUCCESS [  5.789 s]
[INFO] IWOS - Warehouse Service ........................... SUCCESS [  4.567 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 🐳 Step 4: Start Infrastructure (PostgreSQL, Redis, Kafka)

Before running the microservices, start the infrastructure:

### Option A: Using Docker Compose (Recommended)

```bash
cd /home/user/CAPSTONE_PROJECT

# Start only infrastructure (not microservices)
docker-compose up -d postgres redis zookeeper kafka kafka-ui

# Verify they're running
docker ps

# Check health
docker exec iwos-postgres pg_isready -U iwos_user
docker exec iwos-redis redis-cli -a iwos_redis_password ping
```

### Option B: Individual Services

```bash
# PostgreSQL
docker run -d --name iwos-postgres \
  -e POSTGRES_DB=iwos_db \
  -e POSTGRES_USER=iwos_user \
  -e POSTGRES_PASSWORD=iwos_password \
  -p 5432:5432 \
  postgres:15-alpine

# Redis
docker run -d --name iwos-redis \
  -p 6379:6379 \
  redis:7-alpine redis-server --requirepass iwos_redis_password

# For Kafka, use docker-compose (it's complex)
```

---

## ▶️ Step 5: Run Services in IntelliJ

### Method 1: Using Run Configurations (Recommended)

I've created run configurations for you. To use them:

1. Click the **Run** dropdown in the toolbar
2. Select one of:
   - **AuthServiceApplication**
   - **InventoryServiceApplication**
   - **OrderServiceApplication**
   - **WarehouseServiceApplication**
3. Click the green play button ▶️

### Method 2: Manually Run Each Service

#### Auth Service (Port 8081)

1. Navigate to: `auth-service/src/main/java/com/iwos/AuthServiceApplication.java`
2. Right-click on the file → **Run 'AuthServiceApplication'**
3. Or click the green play button (▶️) in the gutter next to the class

**Check it's running**:
```bash
curl http://localhost:8081/actuator/health
```

#### Inventory Service (Port 8082)

1. Navigate to: `inventory-service/src/main/java/com/iwos/InventoryServiceApplication.java`
2. Right-click → **Run 'InventoryServiceApplication'**

**Check it's running**:
```bash
curl http://localhost:8082/actuator/health
```

#### Order Service (Port 8083)

1. Navigate to: `order-service/src/main/java/com/iwos/OrderServiceApplication.java`
2. Right-click → **Run 'OrderServiceApplication'**

**Check it's running**:
```bash
curl http://localhost:8083/actuator/health
```

#### Warehouse Service (Port 8084)

1. Navigate to: `warehouse-service/src/main/java/com/iwos/WarehouseServiceApplication.java`
2. Right-click → **Run 'WarehouseServiceApplication'**

**Check it's running**:
```bash
curl http://localhost:8084/actuator/health
```

### Method 3: Run All Services with Compound Configuration

1. Click **Run** → **Edit Configurations**
2. Click **+** → **Compound**
3. Name it: **All IWOS Services**
4. Add all 4 services:
   - AuthServiceApplication
   - InventoryServiceApplication
   - OrderServiceApplication
   - WarehouseServiceApplication
5. Click **OK**
6. Run **All IWOS Services** to start everything at once!

---

## 🧪 Step 6: Test the System

### Test 1: Register a User

Open IntelliJ HTTP Client or use terminal:

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

**Save the `accessToken` from the response!**

### Test 2: Create an Order

```bash
# Replace YOUR_TOKEN with the accessToken from above
curl -X POST http://localhost:8083/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer YOUR_TOKEN' \
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

---

## 📊 Step 7: Monitor Logs in IntelliJ

### View Service Logs

1. After running a service, the **Run** tool window opens at the bottom
2. You'll see tabs for each service
3. Click on a tab to view its logs
4. Look for:
   - ✅ `Started [ServiceName]Application in X seconds`
   - 📦 `Creating order for customer...`
   - 🔍 `Finding optimal warehouse...`
   - ✅ `Order created successfully`

### Search Logs

1. In the Run tool window, click the **Filter** icon (funnel)
2. Enter keywords like: `ERROR`, `Exception`, `ORDER`, etc.
3. Enable **Scroll to the end** to auto-scroll with new logs

---

## 🐛 Troubleshooting

### Problem: "Cannot resolve symbol 'lombok'"

**Solution**:
1. Enable annotation processing (see Step 2.1)
2. Invalidate caches: **File** → **Invalidate Caches** → **Invalidate and Restart**

### Problem: "Port 8081 already in use"

**Solution**:
```bash
# Find what's using the port
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or change the port in application.yml
```

### Problem: "Connection refused to PostgreSQL"

**Solution**:
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Start it if not running
docker-compose up -d postgres

# Check logs
docker logs iwos-postgres
```

### Problem: Maven dependencies not downloading

**Solution**:
1. **File** → **Invalidate Caches** → **Invalidate and Restart**
2. Delete `~/.m2/repository` folder
3. Reload Maven projects
4. Run: `mvn clean install -U` (forces updates)

### Problem: Lombok annotations not working

**Solution**:
1. Install Lombok Plugin:
   - **File** → **Settings** → **Plugins**
   - Search for "Lombok"
   - Install and restart IntelliJ
2. Enable annotation processing (Step 2.1)

---

## 🔥 Hot Reload / Auto-Restart

IntelliJ supports auto-restart when code changes:

1. **File** → **Settings** → **Build, Execution, Deployment** → **Compiler**
2. Check ✅ **Build project automatically**
3. **File** → **Settings** → **Advanced Settings**
4. Check ✅ **Allow auto-make to start even if developed application is currently running**
5. Services will auto-restart when you save files (Ctrl+S)

---

## 🎯 IntelliJ Pro Tips

### 1. Database Tool (Ultimate Edition Only)

1. **View** → **Tool Windows** → **Database**
2. Click **+** → **Data Source** → **PostgreSQL**
3. Enter connection details:
   - Host: `localhost`
   - Port: `5432`
   - Database: `iwos_db`
   - User: `iwos_user`
   - Password: `iwos_password`
4. Click **Test Connection** → **OK**
5. Now you can browse tables, run queries directly in IntelliJ!

### 2. HTTP Client (Testing APIs)

1. **Tools** → **HTTP Client** → **Create Request in HTTP Client**
2. Create a file `test-apis.http` with:

```http
### Register User
POST http://localhost:8081/api/v1/auth/register
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "email": "admin@iwos.com",
  "name": "Admin User"
}

### Login
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

### Create Order
POST http://localhost:8083/api/v1/orders
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "customerId": "CUST-123",
  "customerName": "John Doe",
  ...
}
```

3. Click the green play button next to each request to execute it!

### 3. Maven Projects Tool Window

- **View** → **Tool Windows** → **Maven**
- See all modules, dependencies, lifecycle phases
- Run Maven commands with double-click
- Right-click project → **Generate Sources and Update Folders**

### 4. Services Tool Window (Ultimate Edition)

- **View** → **Tool Windows** → **Services**
- Organize all running services in one place
- Start/stop multiple services easily
- View logs side-by-side

---

## ✅ Verification Checklist

After setup, verify:

- [x] IntelliJ opens the project without errors
- [x] Maven dependencies are downloaded
- [x] No red underlines in code (all imports resolved)
- [x] PostgreSQL, Redis, Kafka are running in Docker
- [x] All 4 services start successfully
- [x] Health endpoints return `{"status":"UP"}`
- [x] Can register user and get JWT token
- [x] Can create order end-to-end

---

## 🎉 You're Ready!

Your IntelliJ IDEA is now fully configured for IWOS development!

**Next Steps**:
1. Set breakpoints and debug requests
2. Use IntelliJ's built-in HTTP client
3. Connect to PostgreSQL database tool
4. Explore the code with Ctrl+Click navigation

**Need Help?** Check IntelliJ's excellent documentation or ask!

---

**Happy Coding! 🚀**
