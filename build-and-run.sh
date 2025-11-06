#!/bin/bash

# ============================================
# IWOS - BUILD AND RUN SCRIPT
# ============================================
# Complete startup script for IWOS system
# Builds all microservices and starts the complete stack
# ============================================

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo "============================================"
echo -e "${GREEN}🚀 IWOS - INTELLIGENT WAREHOUSE OPERATIONS SYSTEM${NC}"
echo "============================================"
echo ""

# ============================================
# PRE-FLIGHT CHECKS
# ============================================
echo -e "${YELLOW}📋 Step 1: Pre-flight Checks${NC}"
echo ""

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker installed${NC}"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose installed${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker is running${NC}"

# Check Java/Maven for local builds
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✅ Maven installed ($(mvn --version | head -1))${NC}"
else
    echo -e "${YELLOW}⚠️  Maven not installed locally (will use Docker for builds)${NC}"
fi

echo ""

# ============================================
# CLEANUP OLD CONTAINERS
# ============================================
echo -e "${YELLOW}🧹 Step 2: Cleaning Up Old Containers${NC}"
echo ""

if [ "$(docker ps -aq -f name=iwos)" ]; then
    echo "Stopping existing IWOS containers..."
    docker-compose down -v 2>/dev/null || true
    echo -e "${GREEN}✅ Cleanup complete${NC}"
else
    echo -e "${CYAN}No existing containers to clean up${NC}"
fi

echo ""

# ============================================
# BUILD MICROSERVICES
# ============================================
echo -e "${YELLOW}🔨 Step 3: Building Microservices${NC}"
echo ""

echo "This will take a few minutes on first run..."
echo ""

# Build all services
echo -e "${CYAN}Building Auth Service...${NC}"
docker-compose build auth-service

echo -e "${CYAN}Building Inventory Service...${NC}"
docker-compose build inventory-service

echo -e "${CYAN}Building Order Service...${NC}"
docker-compose build order-service

echo -e "${CYAN}Building Warehouse Service...${NC}"
docker-compose build warehouse-service

echo ""
echo -e "${GREEN}✅ All services built successfully${NC}"
echo ""

# ============================================
# START INFRASTRUCTURE
# ============================================
echo -e "${YELLOW}🐘 Step 4: Starting Infrastructure (PostgreSQL, Redis, Kafka)${NC}"
echo ""

# Start infrastructure services first
docker-compose up -d postgres redis zookeeper kafka

# Wait for infrastructure to be healthy
echo "Waiting for infrastructure to be ready..."
echo -n "  PostgreSQL: "
until docker exec iwos-postgres pg_isready -U iwos_user > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

echo -n "  Redis: "
until docker exec iwos-redis redis-cli -a iwos_redis_password ping > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

echo -n "  Kafka: "
until docker exec iwos-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
    echo -n "."
    sleep 3
done
echo -e " ${GREEN}Ready${NC}"

echo ""
echo -e "${GREEN}✅ Infrastructure is running${NC}"
echo ""

# ============================================
# START MICROSERVICES
# ============================================
echo -e "${YELLOW}🎯 Step 5: Starting Microservices${NC}"
echo ""

# Start microservices
docker-compose up -d auth-service inventory-service warehouse-service order-service kafka-ui

# Wait for services to be healthy
echo "Waiting for microservices to be ready (this may take 1-2 minutes)..."
sleep 10

echo -n "  Auth Service (8081): "
for i in {1..30}; do
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo -n "  Inventory Service (8082): "
for i in {1..30}; do
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo -n "  Order Service (8083): "
for i in {1..30}; do
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo -n "  Warehouse Service (8084): "
for i in {1..30}; do
    if curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}Ready${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

echo ""

# ============================================
# SYSTEM STATUS
# ============================================
echo "============================================"
echo -e "${GREEN}✅ IWOS IS RUNNING!${NC}"
echo "============================================"
echo ""
echo -e "${BLUE}📊 SERVICE ENDPOINTS:${NC}"
echo ""
echo "  🔐 Auth Service:        http://localhost:8081"
echo "     Health Check:       http://localhost:8081/actuator/health"
echo "     API Docs:           http://localhost:8081/swagger-ui.html"
echo ""
echo "  📦 Inventory Service:   http://localhost:8082"
echo "     Health Check:       http://localhost:8082/actuator/health"
echo "     API Docs:           http://localhost:8082/swagger-ui.html"
echo ""
echo "  🛒 Order Service:       http://localhost:8083"
echo "     Health Check:       http://localhost:8083/actuator/health"
echo "     API Docs:           http://localhost:8083/swagger-ui.html"
echo ""
echo "  🏭 Warehouse Service:   http://localhost:8084"
echo "     Health Check:       http://localhost:8084/actuator/health"
echo "     API Docs:           http://localhost:8084/swagger-ui.html"
echo ""
echo -e "${BLUE}🗄️ INFRASTRUCTURE:${NC}"
echo ""
echo "  PostgreSQL:            localhost:5432"
echo "    Database:            iwos_db"
echo "    User:                iwos_user"
echo "    Password:            iwos_password"
echo ""
echo "  Redis:                 localhost:6379"
echo "    Password:            iwos_redis_password"
echo ""
echo "  Kafka:                 localhost:9093 (external)"
echo "    Kafka UI:            http://localhost:8090"
echo ""
echo -e "${BLUE}🎯 QUICK TESTS:${NC}"
echo ""
echo "  # Health Check All Services"
echo "  curl http://localhost:8081/actuator/health  # Auth"
echo "  curl http://localhost:8082/actuator/health  # Inventory"
echo "  curl http://localhost:8083/actuator/health  # Order"
echo "  curl http://localhost:8084/actuator/health  # Warehouse"
echo ""
echo "  # Register User"
echo "  curl -X POST http://localhost:8081/api/v1/auth/register \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"username\":\"admin\",\"password\":\"admin123\",\"email\":\"admin@iwos.com\",\"name\":\"Admin User\"}'"
echo ""
echo "  # Login"
echo "  curl -X POST http://localhost:8081/api/v1/auth/login \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"username\":\"admin\",\"password\":\"admin123\"}'"
echo ""
echo -e "${BLUE}📝 LOGS:${NC}"
echo ""
echo "  # View all logs"
echo "  docker-compose logs -f"
echo ""
echo "  # View specific service logs"
echo "  docker-compose logs -f order-service"
echo "  docker-compose logs -f inventory-service"
echo "  docker-compose logs -f warehouse-service"
echo "  docker-compose logs -f auth-service"
echo ""
echo -e "${BLUE}🛑 STOP SYSTEM:${NC}"
echo ""
echo "  docker-compose down         # Stop all services"
echo "  docker-compose down -v      # Stop and remove volumes (clean slate)"
echo ""
echo "============================================"
echo -e "${YELLOW}💡 TIP: Check the README.md for API examples and testing${NC}"
echo "============================================"
