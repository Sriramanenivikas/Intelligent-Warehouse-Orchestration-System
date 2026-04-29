# warehouse-orchestrator-service

Consumes payment-authorized workflow events and materializes warehouse fulfillment work.

Current scope:

- consumes `order-orchestrator.payment-authorized.v1` from `iwos.order-orchestrator.events.v1`
- fetches the workflow snapshot from `order-orchestrator-service`
- creates one fulfillment order per workflow
- creates `PICK` tasks for reserved items and a trailing `PACK` task
- writes outbox events for downstream warehouse consumers
- exposes read APIs for fulfillment orders

Query APIs:

- `GET /api/v1/fulfillment-orders/{fulfillmentOrderId}`
- `GET /api/v1/fulfillment-orders/by-workflow/{workflowId}`
- `GET /api/v1/fulfillment-orders?status=TASKS_CREATED&limit=25`

Local defaults:

- port `8086`
- postgres `jdbc:postgresql://localhost:55432/iwos`
- kafka `localhost:9092`
- orchestrator API `http://localhost:8083`
