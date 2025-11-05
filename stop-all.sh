#!/bin/bash

# IWOS - Stop All Services Script

echo "=========================================="
echo "🛑 Stopping IWOS System"
echo "=========================================="
echo ""

# Stop frontend
if [ -f logs/frontend.pid ]; then
    echo "Stopping Frontend..."
    kill $(cat logs/frontend.pid) 2>/dev/null || true
    rm logs/frontend.pid
    echo "✅ Frontend stopped"
fi

# Stop backend services
for service in auth-service inventory-service order-service warehouse-service; do
    if [ -f logs/${service}.pid ]; then
        echo "Stopping ${service}..."
        kill $(cat logs/${service}.pid) 2>/dev/null || true
        rm logs/${service}.pid
        echo "✅ ${service} stopped"
    fi
done

# Stop Docker containers
echo ""
echo "Stopping Docker containers..."
docker-compose down
echo "✅ Docker containers stopped"

echo ""
echo "=========================================="
echo "✅ All services stopped"
echo "=========================================="
