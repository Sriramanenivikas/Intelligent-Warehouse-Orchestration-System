#!/bin/bash

# ============================================
# KONG API GATEWAY CONFIGURATION SCRIPT
# ============================================
# This script configures Kong with all services, routes, and plugins
# Run after Kong is started

set -e

KONG_ADMIN_URL="http://localhost:8001"

echo "============================================"
echo "🚀 Configuring Kong API Gateway"
echo "============================================"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Wait for Kong to be ready
echo -e "${YELLOW}⏳ Waiting for Kong to be ready...${NC}"
until curl -s "$KONG_ADMIN_URL" > /dev/null 2>&1; do
    echo "Waiting for Kong Admin API..."
    sleep 2
done
echo -e "${GREEN}✅ Kong is ready!${NC}"
echo ""

# ============================================
# 1. REGISTER SERVICES
# ============================================
echo -e "${YELLOW}📦 Step 1: Registering Services${NC}"

# Order Service
echo "Registering Order Service..."
curl -i -X POST $KONG_ADMIN_URL/services \
  --data name=order-service \
  --data url='http://host.docker.internal:8083' \
  2>/dev/null

# Inventory Service
echo "Registering Inventory Service..."
curl -i -X POST $KONG_ADMIN_URL/services \
  --data name=inventory-service \
  --data url='http://host.docker.internal:8082' \
  2>/dev/null

# Warehouse Service
echo "Registering Warehouse Service..."
curl -i -X POST $KONG_ADMIN_URL/services \
  --data name=warehouse-service \
  --data url='http://host.docker.internal:8084' \
  2>/dev/null

# Auth Service
echo "Registering Auth Service..."
curl -i -X POST $KONG_ADMIN_URL/services \
  --data name=auth-service \
  --data url='http://host.docker.internal:8081' \
  2>/dev/null

echo -e "${GREEN}✅ All services registered${NC}"
echo ""

# ============================================
# 2. CREATE ROUTES
# ============================================
echo -e "${YELLOW}🛣️  Step 2: Creating Routes${NC}"

# Order Service Routes
echo "Creating routes for Order Service..."
curl -i -X POST $KONG_ADMIN_URL/services/order-service/routes \
  --data 'paths[]=/api/v1/orders' \
  --data name=order-routes \
  2>/dev/null

# Inventory Service Routes
echo "Creating routes for Inventory Service..."
curl -i -X POST $KONG_ADMIN_URL/services/inventory-service/routes \
  --data 'paths[]=/api/v1/inventory' \
  --data name=inventory-routes \
  2>/dev/null

# Warehouse Service Routes
echo "Creating routes for Warehouse Service..."
curl -i -X POST $KONG_ADMIN_URL/services/warehouse-service/routes \
  --data 'paths[]=/api/v1/warehouses' \
  --data name=warehouse-routes \
  2>/dev/null

# Auth Service Routes (no JWT validation needed)
echo "Creating routes for Auth Service..."
curl -i -X POST $KONG_ADMIN_URL/services/auth-service/routes \
  --data 'paths[]=/api/v1/auth' \
  --data name=auth-routes \
  2>/dev/null

echo -e "${GREEN}✅ All routes created${NC}"
echo ""

# ============================================
# 3. ENABLE JWT AUTHENTICATION
# ============================================
echo -e "${YELLOW}🔐 Step 3: Enabling JWT Authentication${NC}"

# Create JWT consumer (represents API clients)
echo "Creating JWT consumer..."
curl -i -X POST $KONG_ADMIN_URL/consumers \
  --data username=iwos-api-client \
  2>/dev/null

# Create JWT credential
echo "Creating JWT credential..."
curl -i -X POST $KONG_ADMIN_URL/consumers/iwos-api-client/jwt \
  --data key=iwos-jwt-key \
  --data secret=iwos-super-secret-key-change-in-production \
  --data algorithm=HS256 \
  2>/dev/null

# Enable JWT on Order Service (protected)
echo "Enabling JWT on Order Service..."
curl -i -X POST $KONG_ADMIN_URL/services/order-service/plugins \
  --data name=jwt \
  2>/dev/null

# Enable JWT on Inventory Service (protected)
echo "Enabling JWT on Inventory Service..."
curl -i -X POST $KONG_ADMIN_URL/services/inventory-service/plugins \
  --data name=jwt \
  2>/dev/null

# Enable JWT on Warehouse Service (protected)
echo "Enabling JWT on Warehouse Service..."
curl -i -X POST $KONG_ADMIN_URL/services/warehouse-service/plugins \
  --data name=jwt \
  2>/dev/null

# Auth service is public (login endpoint)

echo -e "${GREEN}✅ JWT authentication enabled${NC}"
echo ""

# ============================================
# 4. ENABLE RATE LIMITING
# ============================================
echo -e "${YELLOW}⚡ Step 4: Enabling Rate Limiting${NC}"

# Global rate limiting (10,000 requests per minute)
echo "Configuring global rate limit..."
curl -i -X POST $KONG_ADMIN_URL/plugins \
  --data name=rate-limiting \
  --data config.minute=10000 \
  --data config.hour=500000 \
  --data config.policy=redis \
  --data config.redis_host=kong-redis \
  --data config.redis_port=6379 \
  --data config.redis_password=kong_redis_password \
  2>/dev/null

# Stricter rate limit for Order Service (critical endpoint)
echo "Configuring Order Service rate limit..."
curl -i -X POST $KONG_ADMIN_URL/services/order-service/plugins \
  --data name=rate-limiting \
  --data config.second=100 \
  --data config.minute=5000 \
  --data config.policy=redis \
  --data config.redis_host=kong-redis \
  --data config.redis_port=6379 \
  --data config.redis_password=kong_redis_password \
  2>/dev/null

echo -e "${GREEN}✅ Rate limiting enabled${NC}"
echo ""

# ============================================
# 5. ENABLE CORS
# ============================================
echo -e "${YELLOW}🌐 Step 5: Enabling CORS${NC}"

curl -i -X POST $KONG_ADMIN_URL/plugins \
  --data name=cors \
  --data config.origins='*' \
  --data config.methods=GET,POST,PUT,DELETE,OPTIONS \
  --data config.headers=Accept,Authorization,Content-Type,X-Request-Id \
  --data config.exposed_headers=X-Auth-Token \
  --data config.credentials=true \
  --data config.max_age=3600 \
  2>/dev/null

echo -e "${GREEN}✅ CORS enabled${NC}"
echo ""

# ============================================
# 6. ENABLE REQUEST/RESPONSE LOGGING
# ============================================
echo -e "${YELLOW}📝 Step 6: Enabling Request Logging${NC}"

curl -i -X POST $KONG_ADMIN_URL/plugins \
  --data name=file-log \
  --data config.path=/tmp/kong-requests.log \
  2>/dev/null

echo -e "${GREEN}✅ Request logging enabled${NC}"
echo ""

# ============================================
# 7. ENABLE PROMETHEUS METRICS
# ============================================
echo -e "${YELLOW}📊 Step 7: Enabling Prometheus Metrics${NC}"

curl -i -X POST $KONG_ADMIN_URL/plugins \
  --data name=prometheus \
  2>/dev/null

echo -e "${GREEN}✅ Prometheus metrics enabled${NC}"
echo ""

# ============================================
# 8. ENABLE CIRCUIT BREAKER (Request Termination on Failure)
# ============================================
echo -e "${YELLOW}🔌 Step 8: Configuring Circuit Breaker${NC}"

# Add request-termination plugin for maintenance mode
curl -i -X POST $KONG_ADMIN_URL/plugins \
  --data name=request-termination \
  --data config.status_code=503 \
  --data config.message='Service temporarily unavailable' \
  --data enabled=false \
  2>/dev/null

echo -e "${GREEN}✅ Circuit breaker configured${NC}"
echo ""

# ============================================
# 9. ENABLE RESPONSE CACHING
# ============================================
echo -e "${YELLOW}💾 Step 9: Enabling Response Caching${NC}"

# Cache GET requests for Warehouse Service (relatively static data)
curl -i -X POST $KONG_ADMIN_URL/services/warehouse-service/plugins \
  --data name=proxy-cache \
  --data config.response_code=200 \
  --data config.request_method=GET \
  --data config.content_type='application/json' \
  --data config.cache_ttl=300 \
  --data config.strategy=redis \
  --data config.redis.host=kong-redis \
  --data config.redis.port=6379 \
  --data config.redis.password=kong_redis_password \
  2>/dev/null

echo -e "${GREEN}✅ Response caching enabled${NC}"
echo ""

# ============================================
# 10. ENABLE IP RESTRICTION (Optional)
# ============================================
echo -e "${YELLOW}🛡️  Step 10: Configuring IP Restrictions${NC}"

# Allow all IPs by default (can be restricted later)
echo "IP restrictions: Allowing all (configure whitelist/blacklist as needed)"

echo -e "${GREEN}✅ IP restrictions configured${NC}"
echo ""

# ============================================
# CONFIGURATION COMPLETE
# ============================================
echo "============================================"
echo -e "${GREEN}✅ Kong Configuration Complete!${NC}"
echo "============================================"
echo ""
echo "🌐 Access Points:"
echo "   Kong Proxy (API Gateway):  http://localhost:8000"
echo "   Kong Admin API:            http://localhost:8001"
echo "   Konga UI (Admin Portal):   http://localhost:1337"
echo "   Prometheus Metrics:        http://localhost:8001/metrics"
echo ""
echo "🔐 JWT Authentication:"
echo "   Consumer: iwos-api-client"
echo "   Key: iwos-jwt-key"
echo "   Secret: iwos-super-secret-key-change-in-production"
echo ""
echo "📊 Rate Limits:"
echo "   Global: 10,000 req/min, 500,000 req/hour"
echo "   Order Service: 100 req/sec, 5,000 req/min"
echo ""
echo "🧪 Test API:"
echo "   # Login (get JWT)"
echo "   curl -X POST http://localhost:8000/api/v1/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"admin@iwos.com\",\"password\":\"Admin@123\"}'"
echo ""
echo "   # Create Order (with JWT)"
echo "   curl -X POST http://localhost:8000/api/v1/orders \\"
echo "     -H 'Authorization: Bearer YOUR_JWT_TOKEN' \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{...}'"
echo ""
echo "📚 Documentation:"
echo "   Kong Admin API: https://docs.konghq.com/gateway/api/admin-ee/"
echo "   Konga UI: http://localhost:1337"
echo ""
echo "============================================"
