#!/bin/bash

# IWOS - Complete System Startup Script
# This script starts all services in the correct order

set -e

echo "=========================================="
echo "🚀 Starting IWOS System"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Start Infrastructure
echo -e "${YELLOW}📦 Step 1: Starting Infrastructure (PostgreSQL, Redis, Kafka)${NC}"
docker-compose up -d postgres redis zookeeper kafka
echo -e "${GREEN}✅ Infrastructure started${NC}"
echo ""

# Wait for databases to be ready
echo -e "${YELLOW}⏳ Waiting for databases to be ready...${NC}"
sleep 10

# Check if PostgreSQL is ready
until docker-compose exec -T postgres pg_isready -U iwos_user -d iwos_db > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done
echo -e "${GREEN}✅ PostgreSQL is ready${NC}"

# Check if Kafka is ready
until docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
  echo "Waiting for Kafka..."
  sleep 2
done
echo -e "${GREEN}✅ Kafka is ready${NC}"
echo ""

# Step 2: Build Backend Services
echo -e "${YELLOW}🔨 Step 2: Building Backend Services${NC}"
cd backend
mvn clean install -DskipTests
echo -e "${GREEN}✅ Backend services built${NC}"
cd ..
echo ""

# Step 3: Start Backend Services
echo -e "${YELLOW}🚀 Step 3: Starting Backend Services${NC}"
echo "Starting services in background..."

# Start each service in background
cd backend/auth-service
nohup mvn spring-boot:run > ../../logs/auth-service.log 2>&1 &
echo $! > ../../logs/auth-service.pid
echo -e "${GREEN}✅ Auth Service starting (port 8081)${NC}"
cd ../..

cd backend/inventory-service
nohup mvn spring-boot:run > ../../logs/inventory-service.log 2>&1 &
echo $! > ../../logs/inventory-service.pid
echo -e "${GREEN}✅ Inventory Service starting (port 8082)${NC}"
cd ../..

cd backend/order-service
nohup mvn spring-boot:run > ../../logs/order-service.log 2>&1 &
echo $! > ../../logs/order-service.pid
echo -e "${GREEN}✅ Order Service starting (port 8083)${NC}"
cd ../..

cd backend/warehouse-service
nohup mvn spring-boot:run > ../../logs/warehouse-service.log 2>&1 &
echo $! > ../../logs/warehouse-service.pid
echo -e "${GREEN}✅ Warehouse Service starting (port 8084)${NC}"
cd ../..

echo ""
echo -e "${YELLOW}⏳ Waiting for services to start (30 seconds)...${NC}"
sleep 30
echo ""

# Step 4: Start Frontend
echo -e "${YELLOW}🎨 Step 4: Starting Frontend${NC}"
cd frontend/iwos-dashboard

if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies..."
    npm install
fi

nohup npm run dev > ../../logs/frontend.log 2>&1 &
echo $! > ../../logs/frontend.pid
echo -e "${GREEN}✅ Frontend starting (port 3000)${NC}"
cd ../..
echo ""

# Summary
echo "=========================================="
echo -e "${GREEN}✅ IWOS System Started Successfully!${NC}"
echo "=========================================="
echo ""
echo "🌐 Access Points:"
echo "   Frontend Dashboard:  http://localhost:3000"
echo "   Auth Service:        http://localhost:8081/api/v1"
echo "   Inventory Service:   http://localhost:8082/api/v1"
echo "   Order Service:       http://localhost:8083/api/v1"
echo "   Warehouse Service:   http://localhost:8084/api/v1"
echo "   Kafka UI:            http://localhost:8090"
echo ""
echo "📋 Logs:"
echo "   tail -f logs/*.log"
echo ""
echo "🛑 To stop all services:"
echo "   ./stop-all.sh"
echo ""
echo "=========================================="
