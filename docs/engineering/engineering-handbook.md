# Engineering Handbook

This document is the short operational handbook for engineers and AI agents working on this repository.

It explains how to think about the system, what not to assume, and which architectural rules are considered canonical.

## 1. Start With The Right Mental Model

Do not treat this project as a standard ecommerce backend.

The target system is a combined:

- commerce intake platform
- fulfillment orchestration platform
- warehouse execution platform
- parcel-network milestone platform
- AI planning platform

The right abstraction is:

- `accept order fast`
- `orchestrate fulfillment asynchronously`
- `move stock and parcels through task and scan events`
- `use AI to improve planning`

## 2. Canonical Documents

If documents conflict, trust them in this order:

1. [Production Architecture Decision Record](../decisions/production-architecture-decisions.md)
2. [Unified Fulfillment Platform](../architecture/unified-fulfillment-platform.md)
3. [Repo Restructure Plan](repo-restructure-plan.md)
4. legacy HLD and LLD docs

## 3. Non-Negotiable Architecture Rules

### Edge and ingress

- `Kong` is the target edge gateway.
- Edge concerns should not live inside a custom Spring gateway service.
- Public ingress belongs at the platform edge, not inside business services.

### Service discovery and configuration

- On EKS, prefer Kubernetes service discovery.
- Do not reintroduce `Eureka` or Spring `Config Server` as target production defaults.
- Secrets belong in AWS Secrets Manager or equivalent secure stores.

### Hot path design

- The synchronous path must stay small.
- Order placement should stop after durable acceptance and event publication.
- Avoid chaining inventory, payment, warehouse, and shipment steps in the request thread.

### Reliability patterns

- Use caller-provided idempotency keys on write APIs.
- Use transactional outbox for domain event publication.
- Use inbox or dedupe patterns for consumers.
- Use retries with backoff and jitter.
- Use DLQs for irrecoverable event-processing failures.

### Data ownership

- Each bounded context owns its data.
- Prefer domain databases over one global shared schema.
- Do not use Kafka as the only source of truth for core business state in V1.

### AI placement

- Keep heavy training and forecasting off the hot path.
- Use fast online inference only when latency budgets support it.
- AI should augment allocation, replenishment, and planning. It should not replace transactional rules.

## 4. Current Repo Status

The repo currently contains many service folders from an earlier architecture.

Important implication:

- not every existing module should survive into the target platform
- some services need to be split
- some need to be merged
- some should be retired

Do not interpret the present folder layout as the final architecture.

Use [Repo Restructure Plan](repo-restructure-plan.md) for the target service map.

## 5. Target System Layers

The future platform should be understood in layers.

### Edge layer

- CDN or edge cache if needed
- WAF
- ALB or NLB
- Kong Gateway

### Commerce and promise layer

- identity
- cart
- catalog
- pricing
- order intake
- promise and allocation
- payment

### Fulfillment layer

- inventory ledger
- node registry
- warehouse orchestrator
- task execution
- returns

### Network layer

- shipment network
- scan event processing
- notification

### Intelligence layer

- forecasting and planning
- feature pipeline
- read-model analytics

## 6. Glossary

- `Cell`: an independently deployable regional stack
- `Node`: a physical fulfillment location such as dark store, FC, hub, or station
- `Order intent`: the durable initial record of a customer order request
- `Promise`: the ETA and fulfillment commitment shown to the customer
- `Reservation`: atomic hold placed on inventory for an order
- `Task`: a unit of warehouse work such as putaway, pick, pack, or count
- `Scan event`: a milestone emitted when stock or a parcel changes hands or location
- `Outbox`: durable table of events to be published after the main transaction commits
- `Inbox`: consumer-side dedupe and processing ledger

## 7. Guardrails For Future Engineers And AI Agents

### Do not add these patterns casually

- full event sourcing as the default persistence model
- synchronous service chains on checkout
- shared databases across unrelated domains
- global inventory locking
- GPS tracking as a central dependency
- one service per tiny concept without a clear failure-isolation benefit

### Prefer these defaults

- explicit state tables plus history tables
- asynchronous workflows over long request chains
- clear service boundaries
- reversible infrastructure decisions
- managed AWS primitives where they materially reduce ops burden

## 8. How To Evaluate A Proposed Change

Before adding a new service, API, or data flow, ask:

1. Which bounded context owns this responsibility?
2. Does it belong on the synchronous path?
3. What is the source of truth?
4. What is the idempotency story?
5. How is failure retried or reconciled?
6. What is the blast radius?
7. Does this belong in the current phase, or should it wait?

## 9. Expected Service Standard

Every production service should eventually have:

- a clear bounded context
- OpenAPI for synchronous APIs
- AsyncAPI or event contract documentation for emitted and consumed events
- DB migrations
- health probes
- metrics and tracing
- deployment manifests or Helm chart
- runbook notes

## 10. Expected Package Standard

Within each Java service, prefer package separation by responsibility:

- `api`
- `application`
- `domain`
- `infrastructure`
- `config`

Do not let all logic collapse into controller-service-repository only.

## 11. How To Use Legacy Docs

Legacy docs are useful when:

- extracting domain language
- understanding original entities
- reusing data concepts
- locating code that may be migrated

Legacy docs are not sufficient when:

- deciding target production architecture
- deciding AWS topology
- deciding service boundaries
- deciding scale and reliability patterns

## 12. Immediate Next-Level References

- [Project Context](../architecture/project-context.md)
- [Repo Restructure Plan](repo-restructure-plan.md)
- [Unified Fulfillment Platform](../architecture/unified-fulfillment-platform.md)
- [Production Architecture Decision Record](../decisions/production-architecture-decisions.md)
