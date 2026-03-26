# 💳 Payment Service — Low-Level Design

## 1. Strategy Pattern Class Diagram

```mermaid
classDiagram
    class PaymentService {
        -PaymentGatewayFactory factory
        -PaymentRepository repository
        -LedgerService ledgerService
        +initiatePayment(InitiatePaymentRequest) PaymentResponse
        +handleWebhook(provider, payload, signature) void
        +getPayment(paymentId) PaymentResponse
    }

    class PaymentGatewayFactory {
        -Map~PaymentMethod_PaymentGateway~ gateways
        +getGateway(PaymentMethod) PaymentGateway
    }

    class PaymentGateway {
        <<interface>>
        +createOrder(amount, currency, metadata) GatewayOrderResponse
        +verifyPayment(gatewayOrderId) boolean
        +processRefund(gatewayPaymentId, amount) RefundResponse
        +verifyWebhookSignature(payload, signature) boolean
    }

    class RazorpayGateway {
        -String keyId
        -String keySecret
        +createOrder(amount, currency, metadata) GatewayOrderResponse
        +verifyPayment(gatewayOrderId) boolean
        +processRefund(gatewayPaymentId, amount) RefundResponse
        +verifyWebhookSignature(payload, signature) boolean
    }

    class StripeGateway {
        -String apiKey
        -String webhookSecret
        +createOrder(amount, currency, metadata) GatewayOrderResponse
        +verifyPayment(gatewayOrderId) boolean
        +processRefund(gatewayPaymentId, amount) RefundResponse
        +verifyWebhookSignature(payload, signature) boolean
    }

    class RefundService {
        +initiateRefund(RefundRequest) RefundResponse
        +processRefund(refundId) void
        +getRefund(refundId) RefundResponse
    }

    class LedgerService {
        +createEntry(LedgerEntry) void
        +getBalance(entityId) BigDecimal
        +getStatements(entityId, from, to) List
    }

    class Payment {
        -String id
        -String orderId
        -String userId
        -BigDecimal amount
        -String currency
        -PaymentMethod method
        -PaymentStatus status
        -String gatewayOrderId
        -String gatewayPaymentId
        -String idempotencyKey
        -Instant createdAt
    }

    class PaymentMethod {
        <<enumeration>>
        UPI
        CREDIT_CARD
        DEBIT_CARD
        NET_BANKING
        WALLET
        COD
    }

    class PaymentStatus {
        <<enumeration>>
        INITIATED
        PENDING
        SUCCESS
        FAILED
        REFUND_INITIATED
        REFUNDED
        PARTIALLY_REFUNDED
    }

    class LedgerEntry {
        -String id
        -String entityId
        -String entityType
        -LedgerType type
        -BigDecimal amount
        -BigDecimal balanceAfter
        -String referenceId
        -Instant createdAt
    }

    PaymentService --> PaymentGatewayFactory
    PaymentService --> Payment
    PaymentService --> LedgerService
    PaymentGatewayFactory --> PaymentGateway
    PaymentGateway <|.. RazorpayGateway
    PaymentGateway <|.. StripeGateway
    RefundService --> PaymentGateway
    LedgerService --> LedgerEntry
    Payment --> PaymentMethod
    Payment --> PaymentStatus
```

## 2. Payment Gateway Selection

| Payment Method | Primary Gateway | Fallback | Reason |
|---------------|----------------|----------|--------|
| UPI | **Razorpay** | — | Best UPI success rates in India (97%+) |
| Credit Card | **Stripe** | Razorpay | Superior fraud detection + international cards |
| Debit Card | **Razorpay** | Stripe | Better domestic bank coverage |
| Net Banking | **Razorpay** | — | 50+ Indian banks supported |
| Wallet | **Razorpay** | — | PayTM, PhonePe, Amazon Pay |
| COD | **Internal** | — | No gateway needed, manual reconciliation |

## 3. Webhook Processing

```mermaid
sequenceDiagram
    participant GW as Razorpay
    participant WH as Webhook Controller
    participant PAY as Payment Service
    participant DB as PostgreSQL
    participant LEDGER as Ledger
    participant KAFKA as Kafka

    GW->>WH: POST /webhooks/razorpay {event, payload}
    WH->>WH: Verify HMAC-SHA256 signature
    
    alt Invalid signature
        WH-->>GW: 401 Unauthorized
    end

    WH->>PAY: handleWebhook("razorpay", payload)
    PAY->>DB: Find payment by gatewayOrderId
    
    alt event = "payment.captured"
        PAY->>DB: UPDATE status = SUCCESS
        PAY->>LEDGER: INSERT CREDIT entry
        PAY->>KAFKA: Publish PaymentCompletedEvent
        PAY-->>GW: 200 OK
    else event = "payment.failed"
        PAY->>DB: UPDATE status = FAILED
        PAY->>KAFKA: Publish PaymentFailedEvent
        PAY-->>GW: 200 OK
    end
```

## 4. ER Diagram

```mermaid
erDiagram
    PAYMENTS {
        uuid id PK
        varchar order_id FK
        varchar user_id FK
        decimal amount
        varchar currency
        varchar method
        varchar status
        varchar gateway_provider
        varchar gateway_order_id
        varchar gateway_payment_id
        varchar idempotency_key UK
        jsonb metadata
        timestamp created_at
        timestamp updated_at
    }

    REFUNDS {
        uuid id PK
        uuid payment_id FK
        decimal amount
        varchar status
        varchar gateway_refund_id
        varchar reason
        timestamp initiated_at
        timestamp completed_at
    }

    LEDGER_ENTRIES {
        uuid id PK
        varchar entity_id
        varchar entity_type
        varchar type
        decimal amount
        decimal balance_after
        varchar reference_id
        varchar description
        timestamp created_at
    }

    PAYMENT_METHODS {
        uuid id PK
        varchar user_id FK
        varchar type
        varchar token
        varchar last_four
        varchar bank_name
        boolean is_default
        timestamp created_at
    }

    PAYMENTS ||--o{ REFUNDS : has
    PAYMENTS ||--o{ LEDGER_ENTRIES : records
```
