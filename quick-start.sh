#!/bin/bash

# Quick Start Script - Start infrastructure only for development

echo "=========================================="
echo "🚀 IWOS Quick Start (Development Mode)"
echo "=========================================="
echo ""

echo "Starting Infrastructure..."
docker-compose up -d postgres redis zookeeper kafka kafka-ui

echo ""
echo "Waiting for services to be ready..."
sleep 15

echo ""
echo "✅ Infrastructure ready!"
echo ""
echo "🌐 Services:"
echo "   PostgreSQL: localhost:5432"
echo "   Redis:      localhost:6379"
echo "   Kafka:      localhost:9093"
echo "   Kafka UI:   http://localhost:8090"
echo ""
echo "📝 Next Steps:"
echo "   1. Start backend services: cd backend/auth-service && mvn spring-boot:run"
echo "   2. Start frontend: cd frontend/iwos-dashboard && npm run dev"
echo ""
echo "💡 Or run './start-all.sh' to start everything automatically"
echo ""
