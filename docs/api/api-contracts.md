# 🔌 API Contracts — All Services

## Port & Base URL Map

| Service | Port | Base URL |
|---------|------|----------|
| API Gateway | 8080 | `/api/v1/*` (routes to all services) |
| Auth | 8081 | `/api/v1/auth`, `/api/v1/users`, `/api/v1/roles` |
| Order | 8082 | `/api/v1/orders` |
| Cart | 8083 | `/api/v1/cart`, `/api/v1/checkout` |
| Payment | 8084 | `/api/v1/payments`, `/api/v1/webhooks` |
| Inventory | 8085 | `/api/v1/inventory` |
| WMS | 8086 | `/api/v1/warehouses`, `/api/v1/zones`, `/api/v1/bins` |
| Pick-Pack | 8087 | `/api/v1/picklists`, `/api/v1/packing` |
| Dispatch | 8088 | `/api/v1/dispatch`, `/api/v1/delivery-partners` |
| Route | 8089 | `/api/v1/routes` |
| Tracking | 8090 | `/api/v1/tracking` |
| Predictor | 8091 | `/api/v1/predictions` |
| Recommendation | 8092 | `/api/v1/recommendations` |
| Pricing | 8093 | `/api/v1/pricing` |
| Fraud | 8094 | `/api/v1/fraud` |
| Notification | 8095 | `/api/v1/notifications` |
| Catalog | 8096 | `/api/v1/products`, `/api/v1/categories`, `/api/v1/brands` |
| Search | 8097 | `/api/v1/search` |
| Seller | 8098 | `/api/v1/sellers` |
| Returns | 8099 | `/api/v1/returns` |
| Review | 8100 | `/api/v1/reviews` |
| Dark Store | 8101 | `/api/v1/darkstores` |
| Serviceability | 8102 | `/api/v1/serviceability` |

---

## Auth Service (`:8081`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | — | Register new user |
| POST | `/api/v1/auth/login` | — | Login, returns JWT |
| POST | `/api/v1/auth/refresh` | — | Refresh access token |
| POST | `/api/v1/auth/logout` | JWT | Invalidate tokens |
| GET | `/api/v1/users/me` | JWT | Get current user profile |
| PUT | `/api/v1/users/me` | JWT | Update profile |
| GET | `/api/v1/roles` | ADMIN | List all roles |

## Catalog Service (`:8096`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/products` | SELLER | Create product |
| GET | `/api/v1/products/{id}` | — | Get product by ID |
| GET | `/api/v1/products/sku/{skuCode}` | — | Get product by SKU |
| GET | `/api/v1/products/category/{categoryId}` | — | List by category (paginated) |
| GET | `/api/v1/products/seller/{sellerId}` | — | List by seller (paginated) |
| GET | `/api/v1/products/search?q=` | — | Search products |
| GET | `/api/v1/products/featured` | — | Featured products |
| PUT | `/api/v1/products/{id}` | SELLER | Update product |
| DELETE | `/api/v1/products/{id}` | SELLER | Soft-delete product |
| POST | `/api/v1/categories` | ADMIN | Create category |
| GET | `/api/v1/categories` | — | Get root categories (tree) |
| GET | `/api/v1/categories/{id}/subcategories` | — | Get subcategories |
| POST | `/api/v1/brands` | ADMIN | Create brand |
| GET | `/api/v1/brands` | — | List all brands |

## Search Service (`:8097`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/search?q=&categoryId=&brandId=&minPrice=&maxPrice=&sortBy=` | — | Full-text search with filters |
| GET | `/api/v1/search/autocomplete?q=&limit=` | — | Autocomplete suggestions |

## Order Service (`:8082`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/orders` | JWT | Create order |
| GET | `/api/v1/orders/{id}` | JWT | Get order details |
| GET | `/api/v1/orders` | JWT | List user's orders (paginated) |
| GET | `/api/v1/orders/{id}/timeline` | JWT | Order status timeline |
| PUT | `/api/v1/orders/{id}/confirm` | SYSTEM | Confirm order |
| PUT | `/api/v1/orders/{id}/cancel` | JWT | Cancel order |
| PUT | `/api/v1/orders/{id}/status` | ADMIN | Update status |

## Cart Service (`:8083`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/cart` | JWT | Get user's cart |
| POST | `/api/v1/cart/items` | JWT | Add item to cart |
| PUT | `/api/v1/cart/items/{skuCode}` | JWT | Update cart item quantity |
| DELETE | `/api/v1/cart/items/{skuCode}` | JWT | Remove item from cart |
| DELETE | `/api/v1/cart` | JWT | Clear cart |
| POST | `/api/v1/checkout` | JWT | Initiate checkout |

## Payment Service (`:8084`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/payments/initiate` | JWT | Initiate payment |
| GET | `/api/v1/payments/{id}` | JWT | Get payment status |
| POST | `/api/v1/payments/refund` | ADMIN | Initiate refund |
| POST | `/api/v1/webhooks/razorpay` | — | Razorpay webhook |
| POST | `/api/v1/webhooks/stripe` | — | Stripe webhook |

## Inventory Service (`:8085`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/inventory/{skuCode}` | — | Get stock level |
| POST | `/api/v1/inventory/reserve` | SYSTEM | Reserve stock |
| POST | `/api/v1/inventory/release` | SYSTEM | Release reservation |
| POST | `/api/v1/inventory/confirm-deduction` | SYSTEM | Confirm stock deduction |
| PUT | `/api/v1/inventory/{skuCode}` | WH_STAFF | Update stock |
| GET | `/api/v1/inventory/alerts` | WH_STAFF | Low stock alerts |

## Seller Service (`:8098`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/sellers` | JWT | Register as seller |
| GET | `/api/v1/sellers/user/{userId}` | JWT | Get seller profile |
| PUT | `/api/v1/sellers/{id}/approve` | ADMIN | Approve seller |
| GET | `/api/v1/sellers/{id}/settlements` | SELLER | Get settlement history |

## Returns Service (`:8099`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/returns` | JWT | Create return request |
| PUT | `/api/v1/returns/{id}/approve` | ADMIN | Approve return |
| PUT | `/api/v1/returns/{id}/schedule-pickup` | SYSTEM | Schedule pickup |
| GET | `/api/v1/returns/user/{userId}` | JWT | User's returns |

## Review Service (`:8100`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/reviews` | JWT | Create review |
| GET | `/api/v1/reviews/product/{productId}` | — | Get product reviews |
| GET | `/api/v1/reviews/product/{productId}/rating` | — | Get rating snapshot |
| GET | `/api/v1/reviews/user/{userId}` | JWT | User's reviews |
| POST | `/api/v1/reviews/{id}/helpful` | JWT | Mark review helpful |

## Dark Store Service (`:8101`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/darkstores` | ADMIN | Create dark store |
| GET | `/api/v1/darkstores/{id}` | — | Get store details |
| GET | `/api/v1/darkstores/nearby?lat=&lng=` | — | Find nearby stores |
| POST | `/api/v1/darkstores/{storeId}/inventory/stock` | WH_STAFF | Add stock |
| POST | `/api/v1/darkstores/{storeId}/inventory/reserve` | SYSTEM | Reserve stock |
| GET | `/api/v1/darkstores/{storeId}/inventory/check` | — | Check availability |
| GET | `/api/v1/darkstores/{storeId}/inventory/low-stock` | WH_STAFF | Low stock items |

## Serviceability Service (`:8102`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/serviceability/check?lat=&lng=` | — | Check by coordinates |
| GET | `/api/v1/serviceability/check/pincode/{pincode}` | — | Check by pincode |

## Dispatch Service (`:8088`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/dispatch/assign` | SYSTEM | Assign delivery partner |
| GET | `/api/v1/dispatch/order/{orderId}` | JWT | Get dispatch status |
| PUT | `/api/v1/dispatch/{id}/status` | DELIVERY | Update delivery status |
| GET | `/api/v1/delivery-partners/{id}/assignments` | DELIVERY | Partner's assignments |

## Tracking Service (`:8090`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/tracking/{orderId}` | JWT | Get tracking history |
| WS | `/ws/tracking/{orderId}` | JWT | Live GPS WebSocket |
| POST | `/api/v1/tracking/gps` | DELIVERY | Post GPS coordinate |

## Common Response Format

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": "2026-03-26T11:00:00Z",
  "requestId": "7f4d3c2b-1a0e-..."
}
```

## Error Response Format (ProblemDetail — RFC 7807)

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Product not found with id: abc-123",
  "instance": "/api/v1/products/abc-123",
  "timestamp": "2026-03-26T11:00:00Z"
}
```
