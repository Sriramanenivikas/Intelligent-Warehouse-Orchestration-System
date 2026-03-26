# 🔐 Security Architecture

## 1. Authentication & Authorization Flow

```mermaid
sequenceDiagram
    participant U as User
    participant GW as API Gateway
    participant AUTH as Auth Service
    participant KC as Keycloak
    participant SVC as Backend Service

    U->>AUTH: POST /auth/login {email, password}
    AUTH->>AUTH: Validate credentials (BCrypt)
    AUTH->>AUTH: Generate JWT (RS256, 15min TTL)
    AUTH->>AUTH: Generate Refresh Token (7 day TTL)
    AUTH-->>U: {accessToken, refreshToken}

    U->>GW: GET /orders (Authorization: Bearer <JWT>)
    GW->>GW: Extract JWT from header
    GW->>GW: Validate signature (public key)
    GW->>GW: Check expiry, issuer, audience
    GW->>GW: Extract roles, userId from claims
    GW->>SVC: Forward + X-User-Id + X-User-Roles headers
    SVC-->>GW: Response
    GW-->>U: Response

    Note over U,GW: Service-to-Service (mTLS)
    SVC->>KC: Validate service token
    KC-->>SVC: Token valid + scopes
    SVC->>SVC: Proceed with authorized scope
```

## 2. JWT Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "iwos-key-2024"
  },
  "payload": {
    "sub": "user-uuid-here",
    "email": "user@example.com",
    "roles": ["CUSTOMER"],
    "iss": "iwos-auth-service",
    "aud": "iwos-api",
    "iat": 1711440000,
    "exp": 1711440900,
    "jti": "unique-token-id"
  }
}
```

## 3. RBAC Role Hierarchy

```mermaid
graph TB
    ADMIN["ADMIN<br>Full access"]
    SELLER["SELLER<br>Own products + orders"]
    WH_STAFF["WAREHOUSE_STAFF<br>WMS + Pick-Pack"]
    DELIVERY["DELIVERY_PARTNER<br>Dispatch + Tracking"]
    CUSTOMER["CUSTOMER<br>Browse + Order"]

    ADMIN --> SELLER & WH_STAFF & DELIVERY & CUSTOMER
```

| Role | Permissions |
|------|------------|
| CUSTOMER | Browse catalog, place orders, track, review, return |
| SELLER | List products, view own orders, settlements, analytics |
| WAREHOUSE_STAFF | WMS operations, pick-pack, inventory management |
| DELIVERY_PARTNER | View assignments, update delivery status, GPS tracking |
| ADMIN | Full access + user management + system configuration |

## 4. Security Layers

```mermaid
graph TB
    subgraph "Layer 1: Edge"
        WAF["AWS WAF v2<br>OWASP Top 10 Rules<br>SQL injection, XSS, CSRF"]
        DDoS["AWS Shield Standard<br>DDoS Protection"]
        GEO["Geo-restriction<br>Block non-operating regions"]
    end

    subgraph "Layer 2: Transport"
        TLS["TLS 1.3<br>All external traffic"]
        mTLS["Mutual TLS (Istio)<br>All internal traffic"]
        CERT["ACM Certificates<br>Auto-renewal"]
    end

    subgraph "Layer 3: Application"
        JWT_V["JWT Validation<br>API Gateway"]
        RATE["Rate Limiting<br>100 req/min per user"]
        CORS_S["CORS Policy<br>Whitelist origins"]
        INPUT["Input Validation<br>Bean Validation + Sanitization"]
        IDMP["Idempotency Keys<br>Payment + Order APIs"]
    end

    subgraph "Layer 4: Data"
        ENC_REST["Encryption at Rest<br>AES-256 (RDS, S3, EBS)"]
        ENC_TRANS["Encryption in Transit<br>TLS 1.3"]
        MASK["Data Masking<br>PII in logs"]
        AUDIT["Audit Trail<br>All write operations"]
    end

    subgraph "Layer 5: Secrets"
        SM_S["AWS Secrets Manager<br>DB credentials, API keys"]
        KMS["AWS KMS<br>Encryption key management"]
        VAULT["Spring Config Server<br>Encrypted properties"]
    end
```

## 5. API Gateway Security Rules

| Rule | Configuration | Purpose |
|------|--------------|---------|
| JWT validation | RS256, 15min expiry | Authentication |
| Rate limiting | 100 req/min/user, 1000 req/min/IP | Abuse prevention |
| Request size limit | 10MB max body | DoS prevention |
| CORS | Whitelist origins | Cross-origin control |
| Circuit breaker | 50% failure threshold, 30s open | Cascade prevention |
| IP blacklist | Dynamic via WAF | Block bad actors |
| Header sanitization | Strip internal headers | Header injection |

## 6. Payment Security (PCI-DSS Compliance)

```
[Customer] → [Razorpay/Stripe Checkout] → [Payment Gateway Webhook]
                ↓                              ↓
         Card data NEVER touches          Signature verified
         our backend servers              (HMAC-SHA256)
                                              ↓
                                    [Payment Service records]
                                    [transaction metadata only]
```

- **No card data storage** — delegated to PCI-compliant gateways
- **Webhook signature verification** — HMAC-SHA256 with shared secret
- **Idempotency** — every payment request has a unique idempotency key
- **Double-entry ledger** — every transaction recorded with CREDIT/DEBIT
