# 🏪 Dark Store Service — Low-Level Design (Blinkit Model)

## 1. What is a Dark Store?

A **dark store** is a micro-fulfillment center (typically 2,000-3,000 sq ft) that serves a 2-4 km radius with **10-minute delivery**. Unlike a regular warehouse, it:
- Has no walk-in customers
- Stocks only ~2,000 high-velocity SKUs
- Uses batch picking (multiple orders at once)
- Auto-replenishes from a parent warehouse

## 2. Class Diagram

```mermaid
classDiagram
    class DarkStoreService {
        +createStore(DarkStore) DarkStore
        +getStore(id) DarkStore
        +findNearbyStores(lat, lng) List
        +getAll() List
    }

    class DarkStoreInventoryService {
        +addStock(storeId, skuCode, productId, qty) DarkStoreStock
        +reserveStock(storeId, skuCode, qty) boolean
        +confirmDeduction(storeId, skuCode, qty) void
        +checkAvailability(storeId, skuCode, qty) boolean
        +getLowStockItems(storeId) List
    }

    class ReplenishmentService {
        +triggerReplenishment(storeId, skuCode, qty) void
        +autoReplenishCheck() void
        -calculateOptimalReorderQty(stock) int
    }

    class BatchPickingService {
        +createBatchPick(storeId, pickerId, orderIds) BatchPick
        +completeBatchPick(batchId) BatchPick
        +getActiveBatches(storeId) List
    }

    class DarkStoreAnalyticsService {
        +getFillRate(storeId) double
        +getTurnoverRate(storeId, skuCode) double
        +getTopSellingSkus(storeId, limit) List
        +getAvgPickTime(storeId) Duration
    }

    class DarkStore {
        -String id
        -String storeCode
        -String name
        -String address
        -Double latitude
        -Double longitude
        -Double serviceRadiusKm
        -Integer maxSkuCapacity
        -Integer currentSkuCount
        -Integer maxDailyOrders
        -Integer deliveryStaffCount
        -StoreStatus status
        -String parentWarehouseId
    }

    class DarkStoreStock {
        -String storeId
        -String skuCode
        -Integer quantity
        -Integer reservedQuantity
        -Integer reorderLevel
        -Integer maxLevel
        -Integer dailySalesAvg
        +getAvailableQuantity() int
        +needsReplenishment() boolean
    }

    class ReplenishmentOrder {
        -String storeId
        -String skuCode
        -Integer requestedQuantity
        -Integer fulfilledQuantity
        -ReplenishmentStatus status
        -String sourceWarehouseId
    }

    class BatchPick {
        -String storeId
        -String pickerId
        -List~String~ orderIds
        -Integer totalItems
        -BatchPickStatus status
    }

    DarkStoreService --> DarkStore
    DarkStoreInventoryService --> DarkStoreStock
    DarkStoreInventoryService --> ReplenishmentService
    ReplenishmentService --> ReplenishmentOrder
    BatchPickingService --> BatchPick
```

## 3. 10-Minute Delivery Flow

```mermaid
sequenceDiagram
    participant C as Customer App
    participant SVC as Serviceability
    participant DS_SVC as Dark Store Service
    participant DS_INV as DS Inventory
    participant BATCH as Batch Picker
    participant DISP as Dispatch
    participant PARTNER as Delivery Partner

    Note over C: Customer opens app at lat=28.63, lng=77.22

    C->>SVC: GET /serviceability/check?lat=28.63&lng=77.22
    SVC->>SVC: Haversine: nearest dark store = 1.2km
    SVC-->>C: ✅ EXPRESS_10MIN, ETA: 9 min, fee: ₹25

    Note over C: Customer adds items to cart & places order

    C->>DS_SVC: Reserve stock for order
    DS_INV->>DS_INV: Check micro-inventory at DS-DEL-003
    DS_INV-->>C: Items reserved ✅

    Note over BATCH: Batch picks every 2 minutes
    rect rgb(255, 230, 200)
        Note over BATCH: T+0: Order enters batch queue
        BATCH->>BATCH: Combine with 2 other nearby orders
        BATCH->>BATCH: Generate optimized pick path
        Note over BATCH: T+1.5 min: Picking complete
        BATCH->>BATCH: Items scanned & verified
        Note over BATCH: T+2 min: Packed & labeled
    end

    BATCH->>DISP: Batch ready for dispatch
    DISP->>DISP: Find nearest idle partner (GPS < 500m)
    DISP->>PARTNER: Assign 3 orders (batch delivery)

    rect rgb(200, 255, 200)
        Note over PARTNER: T+3 min: Partner arrives at store
        PARTNER->>PARTNER: Scan & pick up packages
        Note over PARTNER: T+4 min: Depart to first delivery
        Note over PARTNER: T+8 min: Deliver order #1
        PARTNER->>C: Delivered! 🎉 (8 min total)
    end

    Note over DS_INV: Auto-trigger if stock < reorder level
    DS_INV->>DS_INV: Stock of SKU-123 dropped to 3 (reorder: 5)
    DS_INV->>DS_SVC: triggerReplenishment(DS-DEL-003, SKU-123, 47)
```

## 4. Auto-Replenishment Algorithm

```
Every 5 minutes:
  FOR each active dark_store:
    FOR each low_stock item (available <= reorder_level):
      optimal_qty = max_level - current_quantity
      IF no pending replenishment for this SKU:
        CREATE replenishment_order(
          store_id: store,
          sku_code: item.sku,
          quantity: optimal_qty,
          source: store.parent_warehouse_id,
          priority: item.daily_sales_avg > 20 ? HIGH : NORMAL
        )
        PUBLISH event: darkstore.replenishment.requested
```

## 5. ER Diagram

```mermaid
erDiagram
    DARK_STORES {
        uuid id PK
        varchar store_code UK
        varchar name
        varchar address
        varchar city
        varchar pincode
        double latitude
        double longitude
        double service_radius_km
        int max_sku_capacity
        int current_sku_count
        int max_daily_orders
        int delivery_staff_count
        varchar status
        varchar parent_warehouse_id FK
        timestamp created_at
    }

    DARK_STORE_STOCK {
        uuid id PK
        varchar store_id FK
        varchar sku_code
        varchar product_id FK
        int quantity
        int reserved_quantity
        int reorder_level
        int max_level
        int daily_sales_avg
        timestamp updated_at
    }

    REPLENISHMENT_ORDERS {
        uuid id PK
        varchar store_id FK
        varchar sku_code
        int requested_quantity
        int fulfilled_quantity
        varchar status
        varchar source_warehouse_id FK
        timestamp created_at
        timestamp fulfilled_at
    }

    BATCH_PICKS {
        uuid id PK
        varchar store_id FK
        varchar picker_id
        int total_items
        varchar status
        timestamp created_at
        timestamp completed_at
    }

    BATCH_PICK_ORDERS {
        uuid batch_pick_id FK
        varchar order_id
    }

    DARK_STORES ||--o{ DARK_STORE_STOCK : holds
    DARK_STORES ||--o{ REPLENISHMENT_ORDERS : receives
    DARK_STORES ||--o{ BATCH_PICKS : processes
    BATCH_PICKS ||--o{ BATCH_PICK_ORDERS : includes
```

## 6. Key Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Time to pick** | < 2 min | Batch pick start → complete |
| **Fill rate** | > 95% | Items available / items requested |
| **Avg delivery time** | < 10 min | Order placed → delivered |
| **Stock turnover** | 2-3x/week | Sales / avg inventory |
| **Replenishment SLA** | < 4 hours | Order → received at dark store |
