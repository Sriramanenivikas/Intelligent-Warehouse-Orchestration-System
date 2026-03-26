# 🔄 End-to-End Data Flows

## Flow 1: Customer Places an Order (Standard)

```mermaid
sequenceDiagram
    participant C as Customer
    participant GW as API Gateway
    participant CART as Cart Service
    participant SVC as Serviceability
    participant ORD as Order Service
    participant TEMP as Temporal Saga
    participant INV as Inventory
    participant PAY as Payment
    participant DISP as Dispatch
    participant NOTIF as Notification
    participant KAFKA as Kafka

    C->>GW: POST /api/v1/orders (JWT)
    GW->>GW: Validate JWT + Rate Limit
    GW->>ORD: Forward request
    
    ORD->>CART: GET /cart/{userId}
    CART-->>ORD: Cart items + prices
    
    ORD->>SVC: GET /serviceability/check?lat=&lng=
    SVC-->>ORD: DeliveryPromise (type, ETA, fee)
    
    ORD->>ORD: Create Order (status=CREATED)
    ORD->>KAFKA: Publish OrderCreatedEvent
    
    ORD->>TEMP: Start OrderProcessingSaga
    
    rect rgb(200, 230, 200)
        Note over TEMP: Saga Step 1: Reserve Inventory
        TEMP->>INV: reserveStock(items[])
        INV-->>TEMP: Reserved ✅
    end

    rect rgb(200, 220, 240)
        Note over TEMP: Saga Step 2: Process Payment
        TEMP->>PAY: initiatePayment(orderId, amount)
        PAY-->>TEMP: Payment SUCCESS ✅
    end

    rect rgb(240, 220, 200)
        Note over TEMP: Saga Step 3: Assign Delivery
        TEMP->>DISP: assignDelivery(orderId, address)
        DISP-->>TEMP: Assignment created ✅
    end

    TEMP->>ORD: Saga COMPLETED
    ORD->>ORD: Update status = CONFIRMED
    ORD->>KAFKA: Publish OrderConfirmedEvent

    KAFKA->>NOTIF: OrderConfirmedEvent
    NOTIF->>C: 📧 Order confirmed email + 📱 Push
    KAFKA->>CART: Clear cart
```

## Flow 2: Blinkit-Style 10-Minute Delivery

```mermaid
sequenceDiagram
    participant C as Customer
    participant GW as Gateway
    participant SVC as Serviceability
    participant DS as Dark Store
    participant ORD as Order Service
    participant BATCH as Batch Picker
    participant DISP as Dispatch
    participant TRACK as Tracking
    participant NOTIF as Notification

    C->>GW: GET /serviceability/check?lat=28.6&lng=77.2
    GW->>SVC: Check delivery zones
    SVC->>SVC: Haversine distance < 3km?
    SVC-->>C: ✅ EXPRESS_10MIN, ETA: 8 min 🚀

    C->>GW: POST /orders (type=EXPRESS)
    GW->>ORD: Create express order
    
    ORD->>DS: POST /darkstores/{id}/inventory/reserve
    DS->>DS: Check micro-inventory
    DS-->>ORD: Reserved from store DS-MUM-001

    ORD->>DS: POST /batch-pick
    DS->>BATCH: Create batch pick (3 orders combined)
    BATCH-->>DS: Batch #B123 assigned to picker

    Note over BATCH: Picker scans items (2 min)
    BATCH->>BATCH: Pick completed ✅

    ORD->>DISP: Assign nearest delivery partner
    DISP->>DISP: GPS proximity match (< 500m)
    DISP-->>ORD: Partner assigned

    DISP->>TRACK: Start GPS tracking
    TRACK->>C: 🔴 WebSocket: Live location stream

    Note over TRACK: Delivery in progress (6 min)

    TRACK->>ORD: Delivery completed
    ORD->>NOTIF: Send delivery confirmation
    NOTIF->>C: 📱 "Delivered in 8 minutes!"
```

## Flow 3: Payment Processing (Strategy Pattern)

```mermaid
sequenceDiagram
    participant ORD as Order Service
    participant PAY as Payment Service
    participant FACTORY as GatewayFactory
    participant RPY as RazorpayGateway
    participant STR as StripeGateway
    participant LEDGER as Ledger
    participant KAFKA as Kafka

    ORD->>PAY: POST /payments/initiate
    Note over PAY: {orderId, amount: ₹1,299, method: UPI}

    PAY->>FACTORY: getGateway(method=UPI)
    FACTORY-->>PAY: RazorpayGateway (UPI → Razorpay)

    PAY->>RPY: createPaymentOrder(amount, currency=INR)
    RPY-->>PAY: razorpay_order_id + checkout_url
    PAY-->>ORD: PaymentResponse(PENDING, checkout_url)

    Note over ORD: Customer completes UPI payment

    RPY->>PAY: POST /webhooks/razorpay (signature verified)
    PAY->>PAY: Verify webhook signature (HMAC-SHA256)
    PAY->>PAY: Update payment status = SUCCESS
    
    PAY->>LEDGER: Create ledger entry (CREDIT)
    LEDGER->>LEDGER: Double-entry bookkeeping

    PAY->>KAFKA: Publish PaymentCompletedEvent
    KAFKA->>ORD: Update order status = PAYMENT_CONFIRMED
```

## Flow 4: Return & Refund Flow

```mermaid
sequenceDiagram
    participant C as Customer
    participant RET as Returns Service
    participant ORD as Order Service
    participant DISP as Dispatch (Reverse)
    participant WMS as WMS
    participant QC as Quality Check
    participant PAY as Payment
    participant INV as Inventory
    participant NOTIF as Notification

    C->>RET: POST /returns {orderId, reason: DEFECTIVE, items[]}
    RET->>ORD: Validate order exists & within return window
    ORD-->>RET: Order valid (delivered 2 days ago) ✅
    RET->>RET: Create ReturnRequest (status=REQUESTED)
    RET-->>C: Return request #R456 created

    Note over RET: Auto-approval for verified reasons
    RET->>RET: status = APPROVED

    RET->>DISP: Schedule reverse pickup
    DISP-->>RET: Pickup slot: Tomorrow 10AM-12PM
    RET->>NOTIF: Notify customer of pickup slot

    Note over DISP: Partner picks up item
    DISP->>RET: Item picked up ✅

    RET->>WMS: Route to returns dock
    WMS->>QC: Initiate quality check

    QC->>QC: Inspect item condition
    QC-->>RET: Verdict: APPROVED_FULL_REFUND

    RET->>PAY: POST /refunds {paymentId, amount: ₹1,299}
    PAY->>PAY: Process refund via original gateway
    PAY-->>RET: Refund initiated

    RET->>INV: Restock item (if condition OK)
    INV->>INV: Add back to available stock

    RET->>NOTIF: Refund processed notification
    NOTIF->>C: 📧 "Refund of ₹1,299 initiated. 3-5 business days."
```

## Flow 5: Seller Onboarding & Commission

```mermaid
sequenceDiagram
    participant S as Seller
    participant SEL as Seller Service
    participant AUTH as Auth Service
    participant CAT as Catalog Service
    participant ORD as Order Service
    participant COM as Commission
    participant SET as Settlement

    S->>SEL: POST /sellers {businessName, GSTIN, bankAccount}
    SEL->>AUTH: Create seller user account
    SEL->>SEL: Status = PENDING_VERIFICATION

    Note over SEL: Admin verifies GSTIN & bank
    SEL->>SEL: Status = ACTIVE ✅

    S->>CAT: POST /products {name, price, images[]}
    CAT->>CAT: Generate SKU: IWOS-ELE-000123
    CAT-->>S: Product listed ✅

    Note over ORD: Customer orders seller's product

    ORD->>COM: Calculate commission (orderId, sellerId)
    COM->>COM: Order ₹999 × 10% = ₹99.90 commission
    COM->>COM: Net payable = ₹899.10

    Note over SET: Weekly settlement cycle
    SET->>SET: Aggregate week's commissions
    SET->>SET: Deduct TDS + platform fees
    SET->>SET: Initiate bank transfer (NEFT/IMPS)
    SET->>S: 💰 ₹12,450 settled to bank account
```
