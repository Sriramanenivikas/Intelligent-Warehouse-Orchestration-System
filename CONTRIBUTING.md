# Contributing

Rules for the implementation phase:

1. Start with the canonical docs under `docs/`.
2. Do not add new services without updating the service catalog and architecture decisions.
3. Keep service boundaries aligned with bounded contexts.
4. Add OpenAPI and AsyncAPI definitions before adding implementation logic.
5. Add migrations before adding persistence logic.
6. Add Helm and environment configuration alongside each service change.
7. Keep idempotency, retries, and observability explicit.
