# promise-allocation-service

Resolves fulfillment promises by selecting the highest-priority active node that has enough inventory for the full order.

## Responsibilities

- maintain active node allocation profiles and priority ordering
- evaluate order items against inventory availability
- persist promise evaluation outcomes for auditability
- return `ALLOCATED` or `UNFULFILLABLE`

## Local Run

```bash
JAVA_HOME=/Users/vikas/Library/Java/JavaVirtualMachines/temurin-21.0.10/Contents/Home \
PROMISE_ALLOCATION_LOG_FILE=/Users/vikas/Documents/capstone/IWOS/logs/promise-allocation-service.log \
mvn -q spring-boot:run -Dspring-boot.run.profiles=local
```

## Local API

- `POST /api/v1/promises/resolve`
- `GET /api/v1/promises/{evaluationId}`
