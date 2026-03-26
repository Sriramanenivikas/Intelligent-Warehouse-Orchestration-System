# API Governance

Rules:

- all synchronous APIs require OpenAPI definitions
- all evented interfaces require AsyncAPI or event contract definitions
- write APIs require idempotency semantics
- public, partner, and internal contracts are separated under `contracts/`
