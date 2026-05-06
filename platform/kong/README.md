# Kong Edge

This folder holds the north-south edge gateway structure.

Current contents:

- `gateway-api/`
  Production-shaped Gateway API manifests for Kubernetes and Kong Ingress Controller.
- `plugins/`
  Shared Kong plugin manifests for request ID, authn, and rate limiting.
- `local/kong.yml`
  Local DB-less Kong config for demo routing across the core service flow.

Local demo use:

```bash
docker compose -f docker-compose.gateway.yml up -d
```

Local proxy endpoints:

- `http://localhost:8008/api/v1/order-intents`
- `http://localhost:8008/api/v1/fulfillment-orders`
- `http://localhost:8008/api/v1/shipments`
- `http://localhost:8008/api/v1/network-shipments`
- `http://localhost:8008/api/v1/scan-events`
- `http://localhost:8008/api/v1/notifications`
