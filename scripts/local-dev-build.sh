#!/bin/bash

################################################################################
#
# Local Development Setup & Build Script
#
# This script:
# 1. Builds all services using Maven
# 2. Validates build success
# 3. Provides startup instructions
#
# Usage: ./scripts/local-dev-build.sh [--skip-tests] [--clean]
#
################################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SKIP_TESTS=false
CLEAN_BUILD=false

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

################################################################################
# Parse Arguments
################################################################################

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        *)
            log_error "Unknown argument: $1"
            exit 1
            ;;
    esac
done

################################################################################
# Build
################################################################################

main() {
    log_info "=================================="
    log_info "IWOS Local Development Build"
    log_info "=================================="
    log_info "Start time: $(date)"
    
    cd "$PROJECT_ROOT"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven not found. Please install Maven 3.6+."
        exit 1
    fi
    
    log_info "Maven version: $(mvn -v | head -1)"
    
    # Build command
    BUILD_CMD="mvn clean install"
    
    if [ "$SKIP_TESTS" = true ]; then
        BUILD_CMD="$BUILD_CMD -DskipTests"
        log_warning "Skipping tests per --skip-tests"
    fi
    
    if [ "$CLEAN_BUILD" = true ]; then
        log_info "Performing clean build..."
    fi
    
    log_info "Building all services..."
    log_info "Command: $BUILD_CMD"
    log_info ""
    
    if $BUILD_CMD; then
        log_success "Build completed successfully!"
    else
        log_error "Build failed!"
        exit 1
    fi
    
    # Summary
    log_info ""
    log_info "=================================="
    log_info "Build Summary"
    log_info "=================================="
    
    # Count built services
    SERVICE_COUNT=$(find services -name "pom.xml" | wc -l)
    log_success "Built $SERVICE_COUNT services"
    
    log_info ""
    log_info "Next steps:"
    log_info "1. Start infrastructure: docker-compose -f docker-compose.yml up -d"
    log_info "2. Start observability: docker-compose -f docker-compose.infra.yml up -d"
    log_info "3. Run E2E tests: ./scripts/e2e-test.sh"
    log_info ""
    log_success "Build process completed!"
}

main "$@"
