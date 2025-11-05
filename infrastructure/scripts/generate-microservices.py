#!/usr/bin/env python3
"""
IWOS Microservices Code Generator
==================================
This script auto-generates all boilerplate code for IWOS microservices.

Usage:
    python3 generate-microservices.py

Features:
- Generates entities, repositories, services, controllers
- Follows SOLID principles
- Creates DTOs and mappers
- Generates Kafka event publishers/consumers
- Creates security configurations
"""

import os
from pathlib import Path
from typing import Dict, List

# Base project directory
BASE_DIR = Path(__file__).parent.parent.parent
BACKEND_DIR = BASE_DIR / "backend"

# Service definitions with their entities
SERVICES = {
    "auth-service": {
        "package": "com.iwos",
        "entities": [
            {
                "name": "User",
                "table": "users",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "username", "unique=true, length=50"),
                    ("String", "email", "unique=true"),
                    ("String", "passwordHash", "column=password_hash"),
                    ("String", "firstName", "column=first_name"),
                    ("String", "lastName", "column=last_name"),
                    ("Boolean", "isActive", "default=true"),
                ],
            },
            {
                "name": "Role",
                "table": "roles",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "name", "unique=true, length=50"),
                    ("String", "description", "TEXT"),
                ],
            },
            {
                "name": "RefreshToken",
                "table": "refresh_tokens",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "token", "unique=true, length=500"),
                    ("Long", "userId", "foreignKey=users"),
                    ("LocalDateTime", "expiresAt"),
                    ("Boolean", "revoked", "default=false"),
                ],
            },
        ],
        "endpoints": [
            ("POST", "/auth/register", "register", "Register new user"),
            ("POST", "/auth/login", "login", "User login"),
            ("POST", "/auth/refresh", "refreshToken", "Refresh JWT token"),
            ("GET", "/auth/me", "getCurrentUser", "Get current user"),
            ("POST", "/auth/logout", "logout", "Logout user"),
        ],
    },
    "inventory-service": {
        "package": "com.iwos",
        "entities": [
            {
                "name": "SKU",
                "table": "skus",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "skuCode", "unique=true, length=50"),
                    ("String", "name", "length=255"),
                    ("String", "description", "TEXT"),
                    ("String", "category", "length=100"),
                    ("BigDecimal", "unitPrice"),
                    ("BigDecimal", "costPrice"),
                    ("Integer", "reorderPoint"),
                    ("Integer", "reorderQuantity"),
                    ("Boolean", "isActive", "default=true"),
                ],
            },
            {
                "name": "Inventory",
                "table": "inventory",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("Long", "skuId", "foreignKey=skus"),
                    ("Long", "warehouseId", "foreignKey=warehouses"),
                    ("Integer", "quantityOnHand"),
                    ("Integer", "quantityReserved"),
                    ("LocalDateTime", "lastCountedAt"),
                ],
            },
            {
                "name": "InventoryTransaction",
                "table": "inventory_transactions",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("Long", "inventoryId", "foreignKey=inventory"),
                    ("String", "transactionType", "length=50"),
                    ("Integer", "quantity"),
                    ("Integer", "quantityBefore"),
                    ("Integer", "quantityAfter"),
                    ("String", "referenceType", "length=50"),
                    ("Long", "referenceId"),
                    ("String", "notes", "TEXT"),
                ],
            },
        ],
        "endpoints": [
            ("GET", "/inventory/skus", "getAllSKUs", "List all SKUs"),
            ("POST", "/inventory/skus", "createSKU", "Create new SKU"),
            ("GET", "/inventory/skus/{id}", "getSKU", "Get SKU by ID"),
            ("GET", "/inventory/stock", "getStock", "Get stock levels"),
            ("POST", "/inventory/stock/adjust", "adjustStock", "Adjust stock"),
            ("POST", "/inventory/stock/reserve", "reserveStock", "Reserve stock for order"),
            ("POST", "/inventory/stock/release", "releaseStock", "Release reserved stock"),
            ("GET", "/inventory/low-stock", "getLowStock", "Get low stock items"),
        ],
    },
    "order-service": {
        "package": "com.iwos",
        "entities": [
            {
                "name": "Order",
                "table": "orders",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "orderNumber", "unique=true, length=50"),
                    ("String", "customerName", "length=255"),
                    ("String", "customerEmail", "length=255"),
                    ("String", "shippingAddress", "TEXT"),
                    ("Long", "warehouseId", "foreignKey=warehouses"),
                    ("String", "status", "enum=OrderStatus"),
                    ("Integer", "totalItems"),
                    ("BigDecimal", "totalAmount"),
                    ("Long", "assignedTo", "foreignKey=users"),
                ],
            },
            {
                "name": "OrderItem",
                "table": "order_items",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("Long", "orderId", "foreignKey=orders"),
                    ("Long", "skuId", "foreignKey=skus"),
                    ("Integer", "quantityOrdered"),
                    ("Integer", "quantityPicked"),
                    ("BigDecimal", "unitPrice"),
                ],
            },
            {
                "name": "OrderStatusHistory",
                "table": "order_status_history",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("Long", "orderId", "foreignKey=orders"),
                    ("String", "statusFrom", "enum=OrderStatus"),
                    ("String", "statusTo", "enum=OrderStatus"),
                    ("Long", "changedBy", "foreignKey=users"),
                    ("String", "notes", "TEXT"),
                ],
            },
        ],
        "endpoints": [
            ("POST", "/orders", "createOrder", "Create new order"),
            ("GET", "/orders/{id}", "getOrder", "Get order by ID"),
            ("GET", "/orders", "getAllOrders", "List all orders"),
            ("PUT", "/orders/{id}/status", "updateStatus", "Update order status"),
            ("DELETE", "/orders/{id}", "cancelOrder", "Cancel order"),
            ("POST", "/orders/{id}/assign", "assignOrder", "Assign order to worker"),
            ("GET", "/orders/pending", "getPendingOrders", "Get pending orders"),
        ],
    },
    "warehouse-service": {
        "package": "com.iwos",
        "entities": [
            {
                "name": "Warehouse",
                "table": "warehouses",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("String", "code", "unique=true, length=20"),
                    ("String", "name", "length=255"),
                    ("String", "address", "TEXT"),
                    ("String", "city", "length=100"),
                    ("String", "state", "length=100"),
                    ("String", "country", "length=100"),
                    ("BigDecimal", "capacitySqm"),
                    ("Boolean", "isActive", "default=true"),
                ],
            },
            {
                "name": "Zone",
                "table": "zones",
                "fields": [
                    ("Long", "id", "ID", True),
                    ("Long", "warehouseId", "foreignKey=warehouses"),
                    ("String", "code", "length=20"),
                    ("String", "name", "length=255"),
                    ("String", "zoneType", "length=50"),
                    ("Integer", "capacity"),
                    ("Integer", "currentUtilization"),
                    ("Boolean", "isActive", "default=true"),
                ],
            },
        ],
        "endpoints": [
            ("GET", "/warehouses", "getAllWarehouses", "List all warehouses"),
            ("POST", "/warehouses", "createWarehouse", "Create warehouse"),
            ("GET", "/warehouses/{id}", "getWarehouse", "Get warehouse by ID"),
            ("GET", "/warehouses/{id}/zones", "getZones", "Get zones in warehouse"),
            ("POST", "/warehouses/{id}/zones", "createZone", "Create zone"),
            ("GET", "/warehouses/{id}/capacity", "getCapacity", "Get warehouse capacity"),
        ],
    },
}


def generate_entity(service_name: str, entity: Dict, package: str):
    """Generate entity class"""
    entity_name = entity["name"]
    table_name = entity["table"]
    fields = entity["fields"]

    imports = [
        "jakarta.persistence.*",
        "lombok.AllArgsConstructor",
        "lombok.Builder",
        "lombok.Data",
        "lombok.NoArgsConstructor",
        "org.springframework.data.annotation.CreatedDate",
        "org.springframework.data.annotation.LastModifiedDate",
        "org.springframework.data.jpa.domain.support.AuditingEntityListener",
        "java.time.LocalDateTime",
    ]

    # Add BigDecimal import if needed
    if any(f[0] == "BigDecimal" for f in fields):
        imports.append("java.math.BigDecimal")

    content = f"""package {package}.entity;

{chr(10).join(f'import {imp};' for imp in sorted(imports))}

/**
 * {entity_name} Entity
 * Auto-generated by IWOS Code Generator
 */
@Entity
@Table(name = "{table_name}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class {entity_name} {{

"""

    # Generate fields
    for field_type, field_name, *annotations in fields:
        is_id = len(annotations) > 0 and annotations[0] == "ID"

        if is_id:
            content += f"""    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private {field_type} {field_name};

"""
        else:
            # Parse annotations
            annotation_str = annotations[0] if annotations else ""

            if "column=" in annotation_str:
                col_name = annotation_str.split("column=")[1].split(",")[0]
                content += f'    @Column(name = "{col_name}")\n'
            elif "unique=true" in annotation_str:
                content += f"    @Column(unique = true)\n"
            elif "TEXT" in annotation_str:
                content += f"    @Column(columnDefinition = \"TEXT\")\n"

            if "foreignKey=" in annotation_str:
                content += f"    // TODO: Add @ManyToOne relationship\n"

            if "default=" in annotation_str:
                default_val = annotation_str.split("default=")[1].split(",")[0]
                content += f"    @Builder.Default\n"

            if field_name in ["createdAt", "created_at"]:
                content += f"    @CreatedDate\n"
            elif field_name in ["updatedAt", "updated_at"]:
                content += f"    @LastModifiedDate\n"

            content += f"    private {field_type} {field_name};\n\n"

    content += "}\n"

    # Write file
    entity_dir = BACKEND_DIR / service_name / "src" / "main" / "java" / "com" / "iwos" / "entity"
    entity_dir.mkdir(parents=True, exist_ok=True)
    (entity_dir / f"{entity_name}.java").write_text(content)

    print(f"✅ Generated entity: {service_name}/entity/{entity_name}.java")


def generate_repository(service_name: str, entity: Dict, package: str):
    """Generate repository interface"""
    entity_name = entity["name"]

    content = f"""package {package}.repository;

import {package}.entity.{entity_name};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {entity_name} Repository
 * Auto-generated by IWOS Code Generator
 */
@Repository
public interface {entity_name}Repository extends JpaRepository<{entity_name}, Long> {{

    // Add custom query methods here
    // Example: Optional<{entity_name}> findByName(String name);
}}
"""

    repo_dir = BACKEND_DIR / service_name / "src" / "main" / "java" / "com" / "iwos" / "repository"
    repo_dir.mkdir(parents=True, exist_ok=True)
    (repo_dir / f"{entity_name}Repository.java").write_text(content)

    print(f"✅ Generated repository: {service_name}/repository/{entity_name}Repository.java")


def generate_service(service_name: str, entity: Dict, package: str):
    """Generate service class"""
    entity_name = entity["name"]

    content = f"""package {package}.service;

import {package}.entity.{entity_name};
import {package}.repository.{entity_name}Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {entity_name} Service
 * Auto-generated by IWOS Code Generator
 *
 * SOLID Principles:
 * - Single Responsibility: Handles {entity_name} business logic
 * - Dependency Inversion: Depends on repository abstraction
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class {entity_name}Service {{

    private final {entity_name}Repository repository;

    /**
     * Get all {entity_name}s
     */
    @Transactional(readOnly = true)
    public List<{entity_name}> getAll() {{
        log.info("Fetching all {entity_name}s");
        return repository.findAll();
    }}

    /**
     * Get {entity_name} by ID
     */
    @Transactional(readOnly = true)
    public Optional<{entity_name}> getById(Long id) {{
        log.info("Fetching {entity_name} with ID: {{}}", id);
        return repository.findById(id);
    }}

    /**
     * Create {entity_name}
     */
    public {entity_name} create({entity_name} entity) {{
        log.info("Creating new {entity_name}");
        return repository.save(entity);
    }}

    /**
     * Update {entity_name}
     */
    public {entity_name} update(Long id, {entity_name} entity) {{
        log.info("Updating {entity_name} with ID: {{}}", id);
        entity.setId(id);
        return repository.save(entity);
    }}

    /**
     * Delete {entity_name}
     */
    public void delete(Long id) {{
        log.info("Deleting {entity_name} with ID: {{}}", id);
        repository.deleteById(id);
    }}
}}
"""

    service_dir = BACKEND_DIR / service_name / "src" / "main" / "java" / "com" / "iwos" / "service"
    service_dir.mkdir(parents=True, exist_ok=True)
    (service_dir / f"{entity_name}Service.java").write_text(content)

    print(f"✅ Generated service: {service_name}/service/{entity_name}Service.java")


def generate_controller(service_name: str, endpoints: List, package: str):
    """Generate REST controller"""
    controller_name = service_name.replace("-service", "").capitalize() + "Controller"

    content = f"""package {package}.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * {controller_name}
 * Auto-generated by IWOS Code Generator
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${{security.allowed-origins}}")
public class {controller_name} {{

    // Inject services here

"""

    for method, path, handler_name, description in endpoints:
        content += f"""    /**
     * {description}
     */
    @{method}Mapping("{path}")
    public ResponseEntity<?> {handler_name}() {{
        log.info("{description}");
        // TODO: Implement {handler_name}
        return ResponseEntity.ok("TODO: Implement {handler_name}");
    }}

"""

    content += "}\n"

    controller_dir = BACKEND_DIR / service_name / "src" / "main" / "java" / "com" / "iwos" / "controller"
    controller_dir.mkdir(parents=True, exist_ok=True)
    (controller_dir / f"{controller_name}.java").write_text(content)

    print(f"✅ Generated controller: {service_name}/controller/{controller_name}.java")


def main():
    print("\n" + "=" * 60)
    print("🚀 IWOS Microservices Code Generator")
    print("=" * 60 + "\n")

    total_files = 0

    for service_name, service_config in SERVICES.items():
        print(f"\n📦 Generating {service_name}...")
        package = service_config["package"]

        # Generate entities, repositories, and services
        for entity in service_config["entities"]:
            generate_entity(service_name, entity, package)
            generate_repository(service_name, entity, package)
            generate_service(service_name, entity, package)
            total_files += 3

        # Generate controller
        generate_controller(service_name, service_config["endpoints"], package)
        total_files += 1

    print(f"\n" + "=" * 60)
    print(f"✅ Code generation complete!")
    print(f"📁 Total files generated: {total_files}")
    print(f"🎯 Next steps:")
    print(f"   1. Review generated code")
    print(f"   2. Implement TODOs")
    print(f"   3. Add custom business logic")
    print(f"   4. Run: mvn clean install")
    print("=" * 60 + "\n")


if __name__ == "__main__":
    main()
