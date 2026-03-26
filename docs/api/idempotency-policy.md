# Idempotency Policy

Write APIs must define:

- caller-provided idempotency key
- key scope
- retention window
- conflict semantics
- replay semantics
- eventual consistency expectations

The first mandatory services for idempotency are:

- `order-intake-service`
- `payment-service`
- `inventory-ledger-service`
- `shipment-network-service`
