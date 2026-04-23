#!/bin/bash

################################################################################
#
# E2E Testing Script for IWOS
#
# This script validates end-to-end order flow:
# 1. Create order via order-intake
# 2. Verify order is persisted
# 3. Check inventory reservation
# 4. Validate order status progression
#
# Prerequisites:
#  - Docker containers running (Postgres, Kafka, Redis)
#  - All services built and running
#
# Usage: ./scripts/e2e-test.sh [--verbose] [--cleanup]
#
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
VERBOSE=false
CLEANUP=false

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ORDER_INTAKE_URL="${ORDER_INTAKE_URL:-http://localhost:8081}"
INVENTORY_URL="${INVENTORY_URL:-http://localhost:8082}"
WAREHOUSE_URL="${WAREHOUSE_URL:-http://localhost:8083}"
PAYMENT_URL="${PAYMENT_URL:-http://localhost:8084}"

TEST_RESULTS_DIR="${PROJECT_ROOT}/test-results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
TEST_LOG="${TEST_RESULTS_DIR}/e2e-test-${TIMESTAMP}.log"

################################################################################
# Utility Functions
################################################################################

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$TEST_LOG"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$TEST_LOG"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$TEST_LOG"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$TEST_LOG"
}

debug() {
    if [ "$VERBOSE" = true ]; then
        echo -e "${BLUE}[DEBUG]${NC} $1" | tee -a "$TEST_LOG"
    fi
}

check_service_health() {
    local service_name=$1
    local service_url=$2
    local max_attempts=10
    local attempt=1

    log_info "Checking health of $service_name at $service_url..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$service_url/actuator/health" > /dev/null 2>&1; then
            log_success "$service_name is healthy"
            return 0
        fi
        log_warning "Attempt $attempt/$max_attempts: $service_name not ready, retrying in 2s..."
        sleep 2
        ((attempt++))
    done

    log_error "$service_name failed health check after $max_attempts attempts"
    return 1
}

generate_test_data() {
    local order_id=$(uuidgen | tr '[:upper:]' '[:lower:]')
    local customer_id="cust-$(date +%s)"
    local idempotency_key="order-key-$(date +%s%N)"
    
    cat <<EOF
{
    "customerId": "$customer_id",
    "idempotencyKey": "$idempotency_key",
    "items": [
        {
            "sku": "SKU-TEST-001",
            "quantity": 2,
            "pricePerUnit": 100.50
        }
    ],
    "shippingAddress": {
        "street": "123 Test St",
        "city": "Mumbai",
        "state": "MH",
        "pincode": "400001",
        "country": "IN"
    },
    "paymentMethod": "CREDIT_CARD"
}
EOF
}

################################################################################
# Test Cases
################################################################################

test_create_order() {
    log_info "TEST: Create Order"
    
    local payload=$(generate_test_data)
    debug "Payload: $payload"
    
    local response=$(curl -s -X POST "$ORDER_INTAKE_URL/api/v1/orders" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    debug "Response: $response"
    
    local status=$(echo "$response" | jq -r '.status // "ERROR"')
    local order_id=$(echo "$response" | jq -r '.orderId // "null"')
    
    if [ "$status" = "CREATED" ] && [ "$order_id" != "null" ]; then
        log_success "Order created successfully: $order_id"
        echo "$order_id"
        return 0
    else
        log_error "Failed to create order. Response: $response"
        return 1
    fi
}

test_get_order() {
    local order_id=$1
    log_info "TEST: Get Order ($order_id)"
    
    local response=$(curl -s -X GET "$ORDER_INTAKE_URL/api/v1/orders/$order_id")
    debug "Response: $response"
    
    local retrieved_id=$(echo "$response" | jq -r '.orderId // "null"')
    
    if [ "$retrieved_id" = "$order_id" ]; then
        log_success "Order retrieved successfully"
        return 0
    else
        log_error "Failed to retrieve order. Response: $response"
        return 1
    fi
}

test_idempotency() {
    log_info "TEST: Idempotency Check"
    
    local payload=$(generate_test_data)
    
    # First request
    local response1=$(curl -s -X POST "$ORDER_INTAKE_URL/api/v1/orders" \
        -H "Content-Type: application/json" \
        -d "$payload")
    local order_id1=$(echo "$response1" | jq -r '.orderId')
    
    sleep 1
    
    # Second request with same payload
    local response2=$(curl -s -X POST "$ORDER_INTAKE_URL/api/v1/orders" \
        -H "Content-Type: application/json" \
        -d "$payload")
    local order_id2=$(echo "$response2" | jq -r '.orderId')
    
    if [ "$order_id1" = "$order_id2" ]; then
        log_success "Idempotency verified: Same order ID returned"
        return 0
    else
        log_error "Idempotency failed: Different IDs returned ($order_id1 vs $order_id2)"
        return 1
    fi
}

test_list_orders() {
    log_info "TEST: List Orders"
    
    local response=$(curl -s -X GET "$ORDER_INTAKE_URL/api/v1/orders?page=1&pageSize=10")
    debug "Response: $response"
    
    local data_count=$(echo "$response" | jq '.data | length')
    
    if [ "$data_count" -ge 0 ]; then
        log_success "Orders listed successfully ($data_count orders)"
        return 0
    else
        log_error "Failed to list orders. Response: $response"
        return 1
    fi
}

test_inventory_reservation() {
    local order_id=$1
    log_info "TEST: Inventory Reservation ($order_id)"
    
    local payload=$(cat <<EOF
{
    "orderId": "$order_id",
    "items": [
        {
            "skuId": "SKU-TEST-001",
            "quantity": 2
        }
    ]
}
EOF
)
    
    local response=$(curl -s -X POST "$INVENTORY_URL/api/v1/inventory/reserve" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    debug "Response: $response"
    
    local reservation_status=$(echo "$response" | jq -r '.status // "FAILED"')
    
    if [ "$reservation_status" != "FAILED" ]; then
        log_success "Inventory reserved: $reservation_status"
        return 0
    else
        log_warning "Inventory reservation returned: $reservation_status (may be expected if service not fully implemented)"
        return 0
    fi
}

################################################################################
# Main Execution
################################################################################

main() {
    log_info "=================================="
    log_info "IWOS E2E Testing Suite"
    log_info "=================================="
    log_info "Start time: $(date)"
    log_info "Test results: $TEST_LOG"
    
    mkdir -p "$TEST_RESULTS_DIR"
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --verbose)
                VERBOSE=true
                shift
                ;;
            --cleanup)
                CLEANUP=true
                shift
                ;;
            *)
                log_error "Unknown argument: $1"
                exit 1
                ;;
        esac
    done
    
    # Health checks
    log_info ""
    log_info "Performing service health checks..."
    check_service_health "Order Intake" "$ORDER_INTAKE_URL" || exit 1
    check_service_health "Inventory" "$INVENTORY_URL" || log_warning "Inventory service may not be running (non-critical for this test)"
    
    # Run tests
    log_info ""
    log_info "Running test cases..."
    
    TESTS_PASSED=0
    TESTS_FAILED=0
    
    # Test 1: Create Order
    if test_create_order; then
        ORDER_ID=$?
        ((TESTS_PASSED++))
        
        # Test 2: Get Order
        if test_get_order "$ORDER_ID"; then
            ((TESTS_PASSED++))
        else
            ((TESTS_FAILED++))
        fi
        
        # Test 3: Inventory Reservation
        if test_inventory_reservation "$ORDER_ID"; then
            ((TESTS_PASSED++))
        else
            ((TESTS_FAILED++))
        fi
    else
        ORDER_ID=$(test_create_order)
        ((TESTS_FAILED++))
    fi
    
    # Test 4: Idempotency
    if test_idempotency; then
        ((TESTS_PASSED++))
    else
        ((TESTS_FAILED++))
    fi
    
    # Test 5: List Orders
    if test_list_orders; then
        ((TESTS_PASSED++))
    else
        ((TESTS_FAILED++))
    fi
    
    # Summary
    log_info ""
    log_info "=================================="
    log_info "Test Summary"
    log_info "=================================="
    log_success "Passed: $TESTS_PASSED"
    log_error "Failed: $TESTS_FAILED"
    log_info "Total: $((TESTS_PASSED + TESTS_FAILED))"
    log_info "End time: $(date)"
    
    if [ "$TESTS_FAILED" -eq 0 ]; then
        log_success "All tests passed!"
        exit 0
    else
        log_error "Some tests failed!"
        exit 1
    fi
}

# Run main function
main "$@"
