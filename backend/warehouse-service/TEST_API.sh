#!/bin/bash

# Warehouse Service API Test Script
# This script demonstrates how to use the Warehouse Service API endpoints

BASE_URL="http://localhost:8084/api/v1/warehouses"

echo "=========================================="
echo "Warehouse Service API Test Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Health Check
echo -e "${BLUE}1. Health Check${NC}"
curl -s "${BASE_URL}/health" | jq '.'
echo ""
echo ""

# 2. Create a Warehouse
echo -e "${BLUE}2. Creating a new warehouse (WH-TEST-001)${NC}"
curl -s -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WH-TEST-001",
    "name": "Test Warehouse NYC",
    "address": "123 Test Street",
    "city": "New York",
    "state": "NY",
    "country": "USA",
    "postalCode": "10001",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "maxCapacity": 10000,
    "priority": 8,
    "capacitySqm": 5000,
    "inventory": {
      "SKU-001": 100,
      "SKU-002": 200,
      "SKU-003": 150
    },
    "managerName": "Test Manager",
    "contactPhone": "+1-212-555-0100",
    "contactEmail": "test@iwos.com",
    "operatingHours": "Mon-Fri 9AM-5PM"
  }' | jq '.'
echo ""
echo ""

# 3. Get all warehouses
echo -e "${BLUE}3. Fetching all active warehouses${NC}"
curl -s "${BASE_URL}?activeOnly=true" | jq '.'
echo ""
echo ""

# 4. Get warehouse statistics
echo -e "${BLUE}4. Fetching warehouse statistics${NC}"
curl -s "${BASE_URL}/stats" | jq '.'
echo ""
echo ""

# 5. Allocate warehouse (CORE FEATURE)
echo -e "${BLUE}5. Testing Warehouse Allocation Algorithm${NC}"
echo -e "${YELLOW}Scenario: Customer in Manhattan needs EXPRESS delivery${NC}"
curl -s -X POST "${BASE_URL}/allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-001",
    "customerLatitude": 40.7580,
    "customerLongitude": -73.9855,
    "deliveryType": "EXPRESS",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 5,
        "productName": "Test Product 1"
      },
      {
        "sku": "SKU-002",
        "quantity": 10,
        "productName": "Test Product 2"
      }
    ],
    "customerAddress": "Times Square, Manhattan",
    "customerCity": "New York",
    "customerState": "NY",
    "customerCountry": "USA"
  }' | jq '.'
echo ""
echo ""

# 6. Test STANDARD delivery (50km radius)
echo -e "${BLUE}6. Testing STANDARD Delivery Allocation${NC}"
echo -e "${YELLOW}Scenario: Customer in New Jersey needs STANDARD delivery${NC}"
curl -s -X POST "${BASE_URL}/allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-002",
    "customerLatitude": 40.7357,
    "customerLongitude": -74.1724,
    "deliveryType": "STANDARD",
    "items": [
      {
        "sku": "SKU-001",
        "quantity": 3
      }
    ]
  }' | jq '.'
echo ""
echo ""

# 7. Get warehouses by city
echo -e "${BLUE}7. Fetching warehouses in New York${NC}"
curl -s "${BASE_URL}/city/New%20York" | jq '.'
echo ""
echo ""

# 8. Update warehouse inventory
echo -e "${BLUE}8. Updating warehouse inventory${NC}"
WAREHOUSE_ID=1
curl -s -X PATCH "${BASE_URL}/${WAREHOUSE_ID}/inventory?sku=SKU-001&quantity=50" | jq '.'
echo ""
echo ""

# 9. Test edge case - No warehouse found
echo -e "${BLUE}9. Testing Edge Case - No Warehouse Available${NC}"
echo -e "${YELLOW}Scenario: Customer very far away (Los Angeles) with EXPRESS delivery${NC}"
curl -s -X POST "${BASE_URL}/allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-TEST-003",
    "customerLatitude": 34.0522,
    "customerLongitude": -118.2437,
    "deliveryType": "EXPRESS",
    "items": [
      {
        "sku": "SKU-999",
        "quantity": 1
      }
    ]
  }' | jq '.'
echo ""
echo ""

echo -e "${GREEN}=========================================="
echo "Test Script Complete!"
echo "==========================================${NC}"
echo ""
echo "Tips:"
echo "  - Check Swagger UI: http://localhost:8084/swagger-ui.html"
echo "  - Check Health: http://localhost:8084/actuator/health"
echo "  - Check Metrics: http://localhost:8084/actuator/prometheus"
echo ""
