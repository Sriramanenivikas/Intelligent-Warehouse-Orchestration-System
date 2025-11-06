#!/bin/bash

# ============================================
# IWOS LOAD TESTING EXECUTION SCRIPT
# ============================================
# Complete end-to-end load testing with:
# - 10,000 orders per second
# - Real-time monitoring dashboards
# - Event flow visualization
# - Database metrics
# ============================================

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ORDERS_PER_SEC="${ORDERS_PER_SEC:-10000}"
TEST_DURATION_MIN="${TEST_DURATION_MIN:-5}"
JWT_TOKEN="${JWT_TOKEN:-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJsb2FkdGVzdCIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c}"

echo "============================================"
echo -e "${GREEN}🚀 IWOS LOAD TESTING SYSTEM${NC}"
echo "============================================"
echo ""
echo -e "${YELLOW}Configuration:${NC}"
echo "  • Target Rate: $ORDERS_PER_SEC orders/second"
echo "  • Test Duration: $TEST_DURATION_MIN minutes"
echo "  • Total Orders: ~$((ORDERS_PER_SEC * TEST_DURATION_MIN * 60)) orders"
echo ""

# ============================================
# STEP 1: Pre-flight Checks
# ============================================
echo -e "${YELLOW}📋 Step 1: Pre-flight Checks${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker is running${NC}"

# Check if main IWOS network exists
if ! docker network inspect iwos-network > /dev/null 2>&1; then
    echo -e "${YELLOW}Creating iwos-network...${NC}"
    docker network create iwos-network
fi
echo -e "${GREEN}✅ Network ready${NC}"

echo ""

# ============================================
# STEP 2: Start Monitoring Stack
# ============================================
echo -e "${YELLOW}📊 Step 2: Starting Monitoring Stack${NC}"
echo "  • Grafana"
echo "  • Prometheus"
echo "  • Jaeger"
echo "  • Elasticsearch + Kibana"
echo ""

docker-compose -f docker-compose.loadtest.yml up -d

# Wait for services to be ready
echo -e "${YELLOW}⏳ Waiting for monitoring services to be ready...${NC}"

# Wait for Prometheus
echo -n "  • Prometheus: "
until curl -s http://localhost:9090/-/ready > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

# Wait for Grafana
echo -n "  • Grafana: "
until curl -s http://localhost:3001/api/health > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

# Wait for Jaeger
echo -n "  • Jaeger: "
until curl -s http://localhost:16686 > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

# Wait for Pincode DB
echo -n "  • Pincode Database: "
until docker exec iwos-pincode-db pg_isready -U pincode_user > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e " ${GREEN}Ready${NC}"

echo ""

# ============================================
# STEP 3: Verify IWOS Services
# ============================================
echo -e "${YELLOW}🔍 Step 3: Verifying IWOS Services${NC}"

# Check Kong
echo -n "  • Kong API Gateway: "
if curl -s http://localhost:8000 > /dev/null 2>&1; then
    echo -e "${GREEN}Running${NC}"
else
    echo -e "${RED}Not running! Please start IWOS services first.${NC}"
    exit 1
fi

# Check Order Service
echo -n "  • Order Service: "
if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}Running${NC}"
else
    echo -e "${RED}Not running! Please start IWOS services first.${NC}"
    exit 1
fi

# Check Inventory Service
echo -n "  • Inventory Service: "
if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}Running${NC}"
else
    echo -e "${RED}Not running! Please start IWOS services first.${NC}"
    exit 1
fi

# Check Warehouse Service
echo -n "  • Warehouse Service: "
if curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}Running${NC}"
else
    echo -e "${RED}Not running! Please start IWOS services first.${NC}"
    exit 1
fi

echo ""

# ============================================
# STEP 4: Start Order Generator
# ============================================
echo -e "${YELLOW}🎯 Step 4: Starting Order Generator${NC}"
echo "  Configuring for $ORDERS_PER_SEC orders/second..."

# Start order generator via API
curl -s -X POST "http://localhost:9001/api/loadtest/start?rate=$ORDERS_PER_SEC" > /dev/null 2>&1

echo -e "${GREEN}✅ Order generator started${NC}"
echo ""

# ============================================
# STEP 5: Show Dashboard URLs
# ============================================
echo "============================================"
echo -e "${GREEN}✅ LOAD TEST RUNNING!${NC}"
echo "============================================"
echo ""
echo -e "${BLUE}📊 DASHBOARDS & MONITORING:${NC}"
echo ""
echo "  🎨 Grafana (Main Dashboard)"
echo "     http://localhost:3001"
echo "     Username: admin"
echo "     Password: admin"
echo ""
echo "  📈 Prometheus (Metrics)"
echo "     http://localhost:9090"
echo ""
echo "  🔍 Jaeger (Distributed Tracing)"
echo "     http://localhost:16686"
echo ""
echo "  📋 Elasticsearch/Kibana (Logs)"
echo "     http://localhost:5601"
echo ""
echo "  🐘 PostgreSQL Admin (Write DB)"
echo "     http://localhost:5050"
echo "     Email: admin@iwos.com"
echo "     Password: admin"
echo ""
echo "  🍃 MongoDB Express (Read DB - CQRS)"
echo "     http://localhost:8091"
echo ""
echo "  📮 Redis Commander (Cache)"
echo "     http://localhost:8092"
echo ""
echo "  🦍 Kong Admin (Konga)"
echo "     http://localhost:1337"
echo ""
echo "  🎛️  Kafka UI"
echo "     http://localhost:8090"
echo ""
echo "============================================"
echo -e "${YELLOW}📊 REAL-TIME METRICS:${NC}"
echo "============================================"
echo ""

# Function to show metrics
show_metrics() {
    while true; do
        clear
        echo "============================================"
        echo -e "${GREEN}📊 LIVE LOAD TEST METRICS${NC}"
        echo "============================================"
        echo ""

        # Get metrics from order generator
        METRICS=$(curl -s http://localhost:9001/api/loadtest/status)

        if [ $? -eq 0 ]; then
            ENABLED=$(echo "$METRICS" | grep -o '"enabled":[^,]*' | cut -d':' -f2)
            TARGET=$(echo "$METRICS" | grep -o '"targetRate":[^,]*' | cut -d':' -f2)
            GENERATED=$(echo "$METRICS" | grep -o '"ordersGenerated":[^,]*' | cut -d':' -f2)
            SUCCESS=$(echo "$METRICS" | grep -o '"ordersSuccess":[^,]*' | cut -d':' -f2)
            FAILURE=$(echo "$METRICS" | grep -o '"ordersFailure":[^,]*' | cut -d':' -f2)
            SUCCESS_RATE=$(echo "$METRICS" | grep -o '"successRate":[^,}]*' | cut -d':' -f2)

            echo -e "${YELLOW}Target Rate:${NC} $TARGET orders/sec"
            echo -e "${YELLOW}Orders Generated:${NC} $GENERATED"
            echo -e "${GREEN}Orders Success:${NC} $SUCCESS"
            echo -e "${RED}Orders Failed:${NC} $FAILURE"
            echo -e "${BLUE}Success Rate:${NC} $SUCCESS_RATE%"
            echo ""

            # Show current rate (from Prometheus)
            CURRENT_RATE=$(curl -s 'http://localhost:9090/api/v1/query?query=rate(orders_generated_total[1m])' | \
                grep -o '"value":\[[^]]*\]' | grep -o '[0-9.]*' | tail -1)

            if [ ! -z "$CURRENT_RATE" ]; then
                echo -e "${GREEN}Current Rate:${NC} $(printf "%.0f" $CURRENT_RATE) orders/sec"
            fi
        fi

        echo ""
        echo "============================================"
        echo -e "${YELLOW}Press Ctrl+C to stop monitoring (test continues)${NC}"
        echo "============================================"

        sleep 5
    done
}

# Show metrics in background (optional)
if [ "$1" == "--monitor" ]; then
    show_metrics
else
    echo ""
    echo -e "${YELLOW}💡 TIP: Run with --monitor flag to see live metrics${NC}"
    echo "   Example: ./run-loadtest.sh --monitor"
    echo ""
    echo -e "${GREEN}Load test is running in background!${NC}"
    echo -e "${YELLOW}To stop: ./stop-loadtest.sh${NC}"
    echo ""
fi
