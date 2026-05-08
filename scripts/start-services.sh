#!/bin/bash

################################################################################
# IWOS Services Startup Script
# Starts all microservices for local development
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
SERVICES_DIR="$PROJECT_ROOT/services"
LOGS_DIR="/tmp/iwos-logs"

# Create logs directory
mkdir -p "$LOGS_DIR"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Services to start (path:port)
declare -a SERVICES=(
    "services/commerce/identity-service:8092"
    "services/commerce/order-intake-service:8081"
    "services/fulfillment/inventory-ledger-service:8082"
    "services/commerce/order-orchestrator-service:8083"
    "services/commerce/promise-allocation-service:8084"
    "services/commerce/payment-service:8085"
    "services/fulfillment/warehouse-orchestrator-service:8086"
    "services/network/shipment-handoff-service:8088"
    "services/network/shipment-network-service:8089"
    "services/network/scan-event-service:8090"
    "services/network/notification-service:8091"
    "services/intelligence/forecasting-planning-service:8093"
    "services/intelligence/control-tower-service:8094"
    "services/fulfillment/node-registry-service:8095"
    "services/fulfillment/returns-service:8096"
)

start_service() {
    local service_path=$1
    local port=$2
    local full_path="$PROJECT_ROOT/$service_path"
    local service_name
    service_name=$(basename "$service_path")

    if [ ! -d "$full_path" ]; then
        log_error "Service path not found: $service_path"
        return 1
    fi

    log_info "Starting $service_name (port $port)..."

    local log_file="$LOGS_DIR/$service_name.log"

    (
      cd "$full_path" && \
      JAVA_HOME=/Users/vikas/Library/Java/JavaVirtualMachines/temurin-21.0.10/Contents/Home \
      mvn -q spring-boot:run -Dspring-boot.run.profiles=local
    ) > "$log_file" 2>&1 &
    
    local pid=$!
    echo $pid > "/tmp/$service_name.pid"
    
    # Wait for service to start
    local max_attempts=20
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://127.0.0.1:$port/actuator/health" > /dev/null 2>&1 || curl -s "http://[::1]:$port/actuator/health" > /dev/null 2>&1; then
            log_success "$service_name started (PID: $pid)"
            return 0
        fi
        sleep 1
        ((attempt++))
    done
    
    log_error "$service_name failed to start (check $log_file)"
    return 1
}

main() {
    log_info "=================================="
    log_info "IWOS Services Startup"
    log_info "=================================="
    
    local failed=0
    local started=0
    
    for service_info in "${SERVICES[@]}"; do
        IFS=':' read -r service_path port <<< "$service_info"

        if start_service "$service_path" "$port"; then
            ((started++))
        else
            ((failed++))
        fi
        
        sleep 1
    done
    
    log_info ""
    log_info "=================================="
    log_info "Startup Summary"
    log_info "=================================="
    log_success "Started: $started services"
    if [ $failed -gt 0 ]; then
        log_error "Failed: $failed services"
    fi
    
    log_info "Logs directory: $LOGS_DIR"
}

main "$@"
