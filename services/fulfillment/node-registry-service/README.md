# node-registry-service

Fulfillment node registry service

Provides the authoritative read API for fulfillment nodes used across the platform.

Implemented endpoints:

- `GET /api/v1/nodes`
- `GET /api/v1/nodes/{nodeId}`

Supported filters:

- `type`
- `city`
- `active`
