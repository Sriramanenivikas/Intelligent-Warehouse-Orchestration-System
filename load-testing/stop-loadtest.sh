#!/bin/bash

# ============================================
# STOP LOAD TEST
# ============================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "============================================"
echo -e "${YELLOW}🛑 Stopping Load Test${NC}"
echo "============================================"
echo ""

# Stop order generator
echo -n "Stopping order generator... "
curl -s -X POST http://localhost:9001/api/loadtest/stop > /dev/null 2>&1
echo -e "${GREEN}Done${NC}"

# Get final metrics
echo ""
echo -e "${YELLOW}📊 Final Metrics:${NC}"
METRICS=$(curl -s http://localhost:9001/api/loadtest/status)

if [ $? -eq 0 ]; then
    GENERATED=$(echo "$METRICS" | grep -o '"ordersGenerated":[^,]*' | cut -d':' -f2)
    SUCCESS=$(echo "$METRICS" | grep -o '"ordersSuccess":[^,]*' | cut -d':' -f2)
    FAILURE=$(echo "$METRICS" | grep -o '"ordersFailure":[^,]*' | cut -d':' -f2)
    SUCCESS_RATE=$(echo "$METRICS" | grep -o '"successRate":[^,}]*' | cut -d':' -f2)

    echo ""
    echo "  Total Orders Generated: $GENERATED"
    echo "  Successful Orders: $SUCCESS"
    echo "  Failed Orders: $FAILURE"
    echo "  Success Rate: $SUCCESS_RATE%"
fi

echo ""
echo -e "${YELLOW}📊 Dashboards are still available at:${NC}"
echo "  • Grafana: http://localhost:3001"
echo "  • Prometheus: http://localhost:9090"
echo "  • Jaeger: http://localhost:16686"
echo ""
echo -e "${YELLOW}To completely shut down monitoring stack:${NC}"
echo "  docker-compose -f docker-compose.loadtest.yml down"
echo ""
echo -e "${GREEN}✅ Load test stopped${NC}"
