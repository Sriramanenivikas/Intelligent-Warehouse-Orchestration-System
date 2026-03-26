# Production Architecture Decision Record

Decision-driven production architecture for the new fulfillment platform.

This document supplements [unified-fulfillment-platform.md](/Users/vikas/Documents/capstone/IWOS/docs/hld/unified-fulfillment-platform.md) with:

- architectural decisions
- rejected alternatives
- AWS landing zone and deployment model
- DevOps and SRE operating model
- API governance
- compliance and security controls
- rationale grounded in official references and workload-specific reasoning

This is the final target architecture proposal, written as if a senior platform team were making production decisions for a real product.

## 1. Decision Standard

This document follows four rules:

1. `Prefer reversible decisions`
   If a decision is hard to roll back, it needs stronger justification and phased activation.

2. `Optimize for failure containment`
   Blast radius reduction is more important than elegance.

3. `Use managed primitives where they reduce undifferentiated ops`
   Managed control planes, managed databases, and managed security services are preferred when they materially improve reliability or compliance.

4. `Do not adopt a pattern because it is fashionable`
   Every major pattern used here is selected because it solves a concrete operational problem for this workload.

## 2. Research Basis

This design incorporates official guidance from:

- AWS Well-Architected Framework
- Amazon Builders’ Library
- AWS Prescriptive Guidance
- Amazon EKS Best Practices Guides
- AWS Control Tower multi-account guidance
- AWS Artifact and AWS PCI guidance
- Kubernetes official documentation
- NIST zero trust guidance
- OWASP API Security guidance

References are listed in Section 18.

## 3. Workload Definition

The target product is:

- a combined order-to-delivery fulfillment platform
- supporting dark store, FC, hub, and delivery station operations
- using scan milestones instead of GPS tracking
- designed for `1k+ accepted orders/sec per regional cell`
- designed for global scale by cell replication
- designed for AI-driven demand, stock, replenishment, and SLA risk decisions

The system is not:

- a simple ecommerce monolith
- a pure WMS
- a pure carrier/TMS
- a pure marketplace platform

It is an integrated `commerce + fulfillment + network + planning` platform.

## 4. Executive Decisions

| ID | Decision | Chosen Option | Rejected Option(s) | Why |
|---|---|---|---|---|
| ADR-001 | Global architecture | regional fulfillment cells | single global hot-path control plane | reduces blast radius, supports regional autonomy |
| ADR-002 | Request path | fast accept + async orchestration | synchronous end-to-end checkout workflow | protects latency and availability |
| ADR-003 | Core workflow state | DB-backed state machine + outbox | full event sourcing by default | simpler rollback, operations, and reporting |
| ADR-004 | Transactional store | Aurora PostgreSQL per domain | one shared DB, or DynamoDB-only | strong consistency where needed, bounded-context isolation |
| ADR-005 | High-volume event/timeline store | DynamoDB for scan/timeline/online features | PostgreSQL for all high-write logs | better scale for append-heavy key-value access |
| ADR-006 | Event backbone | MSK / Kafka | direct RPC choreography, SQS-only | replay, fan-out, ordered partitions, backpressure |
| ADR-007 | Compute platform | EKS + managed node groups + Karpenter | self-managed K8s, ECS-only, all-Fargate | balances control, ecosystem, and autoscaling |
| ADR-008 | Delivery model | GitOps with Argo CD | imperative kubectl or manual Helm only | auditability, drift detection, rollback, multi-cluster |
| ADR-009 | API idempotency | caller-provided idempotency key / client token | request hashing only | hashing fails when equal requests can mean different intent |
| ADR-010 | Retry behavior | bounded retry + backoff + jitter | aggressive retries without control | avoids overload amplification |
| ADR-011 | Fallback strategy | fail fast, degrade explicitly, no hidden fallback to primary DB | cache-bypass or direct-DB fallback paths | prevents bimodal behavior and outage amplification |
| ADR-012 | Security boundary | multi-account landing zone + zero trust + least privilege | single-account flat deployment | reduces blast radius and helps compliance |
| ADR-013 | East-west protection | network policies + security groups for pods + selective mesh | fully open east-west traffic | defense-in-depth with controlled operational cost |
| ADR-014 | Deployment safety | two-phase compatibility changes + canary/blue-green | one-shot breaking changes | preserves rollback safety |
| ADR-015 | Public API shape | coarse-grained external APIs, event-driven internals | expose internal microservice APIs externally | simplifies clients and reduces coupling |
| ADR-016 | AI architecture | offline training + online feature store + lightweight inference | large model inference in every hot-path call | protects latency and cost |
| ADR-017 | Compliance strategy | minimize regulated data scope and isolate it | let compliance scope spread across platform | easier audit, lower risk |

## 5. Deep Decisions

### ADR-001: Regional Fulfillment Cells

#### Decision

Run the platform as independent regional cells.

Examples:

- `ap-south-1` for India
- `eu-west-1` for Europe
- `us-east-1` for North America

Each cell owns:

- regional order intake
- regional Kafka
- regional transactional data
- regional fulfillment nodes
- regional AI online features

#### Why

AWS Builders’ Library recommends designing for static stability and Availability Zone independence rather than assuming a failing dependency can be repaired just-in-time. That same reasoning extends to regional blast radius reduction. [R2]

#### Why Not Single Global Active-Active

- a single global hot path increases latency
- cross-region consensus for inventory or order state becomes expensive and fragile
- incident blast radius becomes unacceptable
- compliance and data residency become harder

#### Consequence

Global scale is achieved by `N x cells`, not by one enormous database cluster.

### ADR-002: Fast Accept Path

#### Decision

The customer request path ends when the platform durably records:

- order intent
- order snapshot
- idempotency key
- outbox event

Everything else happens asynchronously.

#### Why

This isolates the user-facing API from:

- warehouse queue spikes
- carrier network delays
- payment webhook lag
- downstream gray failures

#### Why Not Synchronous Checkout Orchestration

Synchronous chains create latency multiplication and couple front-door availability to internal operations. For this workload, that is an anti-pattern.

#### Consequence

`1k+ accepted orders/sec` is realistic.

`1k+ fully orchestrated orders/sec in the same request` is not the correct design target.

### ADR-003: DB-backed State Machine, Not Full Event Sourcing

#### Decision

Use:

- explicit order tables
- explicit shipment tables
- explicit task tables
- state transition history
- outbox/inbox

Do not make event sourcing the default persistence model.

#### Why

- easier to query
- easier to reconcile
- easier to roll back and migrate
- easier for ops and analytics teams
- lower implementation risk for a first production platform

#### Why Not Full Event Sourcing

Event sourcing adds complexity in:

- replay logic
- projection rebuilds
- schema evolution
- backfill and debugging

It is valuable in some domains, but it is not required for order, task, and shipment lifecycle in V1.

#### Consequence

Kafka remains the integration event backbone, but not the sole source of truth.

### ADR-004: Aurora PostgreSQL Per Domain

#### Decision

Use Aurora PostgreSQL for:

- order-intake / order-state
- payment
- inventory ledger
- warehouse orchestration
- shipment network

Use domain-level databases, not one DB per tiny service.

#### Why

- strong transactional integrity
- mature SQL ecosystem
- easier reporting and reconciliation
- Aurora operational advantages over self-managed Postgres

#### Why Not One Shared Database

- poor blast radius control
- difficult schema governance
- release coupling across domains

#### Why Not DynamoDB Everywhere

- order/payment/workflow domains need rich transactional semantics
- relational constraints and operator queries remain important

#### Consequence

We choose `database per bounded context`, not `database per class of service`.

### ADR-005: DynamoDB for Scan Timelines and Online Features

#### Decision

Use DynamoDB for:

- scan event timelines
- task acknowledgement event store
- online feature snapshots
- high-volume append-oriented operational histories

#### Why

- predictable key-value scale
- natural access patterns by `shipmentId`, `taskId`, `nodeId`
- built for high write rates
- TTL and streams are useful

#### Why Not PostgreSQL for All Scan Histories

The scan stream is append-heavy, high-volume, and mostly accessed by primary key and time-window patterns. That is not the best use of the same transactional store that owns order and inventory invariants.

### ADR-006: Kafka / MSK as Event Backbone

#### Decision

Use Kafka on MSK for:

- domain event fan-out
- ordered partition processing
- replay and reprocessing
- multiple independent consumer groups
- long-lived operational streams

#### Why

This workload has:

- many producers
- many read models
- replay needs
- downstream AI feature generation
- warehouse and shipment state derived from streams

#### Why Not Direct RPC Choreography

- poor resilience
- poor observability
- tight service coupling
- retry storms under degradation

#### Why Not SQS-only

SQS is a strong building block, but the central platform needs a replayable log with many consumer groups and keyed ordering. For this workload, Kafka is the better backbone.

#### Note

This is a workload-driven design inference, supported by the need for fan-out, replay, and ordering, rather than a claim that Kafka is always universally better.

### ADR-007: EKS + Managed Node Groups + Karpenter

#### Decision

Use:

- EKS managed control plane
- managed node groups for baseline core services
- Karpenter for bursty worker and AI-inference capacity

#### Why

AWS EKS reliability guidance emphasizes a managed highly available control plane across three AZs and a shared-responsibility model for the data plane. [R5]

Karpenter is recommended for workloads with changing capacity needs and simplifies node provisioning compared to many static node groups. [R9]

#### Why Not Self-Managed Kubernetes

- too much undifferentiated ops
- upgrade burden
- patching burden
- higher failure risk for a team focused on product delivery

#### Why Not All Fargate

- stateful workloads and DaemonSet-based operational agents do not fit well
- EBS CSI constraints limit stateful usage patterns
- cost and control tradeoffs are less favorable for this platform mix

#### Why Not ECS-only

ECS is a valid production platform, but this workload benefits from:

- GitOps-native workflows
- richer K8s ecosystem
- Karpenter
- service mesh optionality
- workload classes with varied scheduling constraints

For this platform, EKS is the stronger long-term fit.

### ADR-008: Argo CD GitOps

#### Decision

Use GitOps with Argo CD for deployment and drift management.

#### Why

AWS Prescriptive Guidance describes Argo CD as:

- declarative
- Git as source of truth
- drift-detecting
- self-healing
- rollback-friendly
- multi-cluster capable [R13]

#### Why Not Manual Helm / kubectl

- poor auditability
- poor drift visibility
- rollback safety depends on humans
- inconsistent environment promotion

#### Why Argo CD Over Flux

Flux is also viable. Argo CD is chosen here because:

- stronger application-centric UI
- easier rollout visibility
- lower cognitive friction for mixed platform/application teams

If the team strongly prefers CLI-centric workflows, Flux remains a credible alternative.

### ADR-009: Caller-Provided Idempotency Tokens

#### Decision

All mutating public APIs must require an idempotency token.

Headers:

- `Idempotency-Key`
- `X-Correlation-Id`
- `traceparent`

#### Why

Amazon’s Builders’ Library recommends caller-provided identifiers because parameter hashing fails when the same parameters can represent different intent. [R3]

#### Why Not Request Hashing Alone

Two identical-looking requests might intentionally mean:

- launch another instance
- create another shipment
- split another replenishment order

Hashing request parameters is insufficient to express user intent.

### ADR-010: Retries With Backoff and Jitter

#### Decision

Use:

- short timeouts
- bounded retries
- exponential backoff
- jitter

#### Why

Amazon’s Builders’ Library explicitly warns that retries without proper controls can amplify overload, while timeouts, retries, and backoff remain core resilience tools when used carefully. [R10]

#### Why Not Infinite or Aggressive Retries

- increases pressure on already-failing dependencies
- raises tail latency
- can turn partial impairment into broader outage

### ADR-011: No Hidden Fallback to “Direct DB Mode”

#### Decision

Do not build hidden fallback paths such as:

- cache failed -> hit primary DB directly for everything
- event broker slow -> switch to synchronous downstream calls
- allocation service slow -> query all node databases inline

#### Why

Amazon’s Builders’ Library documents how fallback paths can create bimodal behavior and amplify outages, including a historical case where fallback behavior worsened impact on Amazon’s own fulfillment network. [R11]

#### Preferred Alternative

- fail fast
- degrade gracefully
- proactively push/cache required data
- keep primary path strong

### ADR-012: Multi-Account Landing Zone

#### Decision

Use AWS Control Tower / Organizations style multi-account isolation.

Recommended minimum structure:

- `security`
- `log-archive`
- `shared-infra`
- `prod-workloads`
- `staging`
- `sandbox`
- `data-ml`
- `pci-payments`

#### Why

AWS Control Tower guidance recommends separate AWS accounts as isolation boundaries for security, blast radius, billing, and compliance. [R14]

#### Why Not Single Account

- weak blast radius control
- poor compliance scoping
- difficult least-privilege boundaries
- noisy governance for prod vs non-prod

### ADR-013: Layered East-West Security

#### Decision

Use:

- IRSA for workload IAM
- default-deny NetworkPolicies
- Security Groups for Pods where AWS resource access isolation matters
- Secrets Manager and KMS
- optional service mesh only where L7 policy and mTLS justify the cost

#### Why

AWS EKS network security guidance explicitly recommends layered security, network policies, security groups, and service mesh only when advanced L7 security/traffic management is required. [R7]

NIST zero trust guidance reinforces resource-centric authorization over implicit network trust. [R16]

#### Why Not Blanket Service Mesh Day One

- more moving parts
- higher latency and operational overhead
- certificate and sidecar lifecycle burden

Use a mesh selectively for high-sensitivity namespaces or where traffic policy needs are strong.

### ADR-014: Deployment Safety Over Speed

#### Decision

All breaking protocol, serializer, schema, and contract changes must use:

- prepare phase
- bake period
- activate phase
- explicit rollback verification

#### Why

Amazon’s Builders’ Library documents rollback safety and two-phase deployment to prevent forward/rollback outages. [R12]

#### Why Not One-Step Breaking Changes

- unsafe mixed-version fleets
- rollback failure risk
- incompatible writer/reader states

### ADR-015: External API Plan

#### Decision

Public APIs are coarse-grained and business-oriented. Internal microservice APIs remain private.

#### Public APIs

- `POST /v1/orders`
- `GET /v1/orders/{orderId}`
- `GET /v1/orders/{orderId}/timeline`
- `POST /v1/payments/authorize`
- `POST /v1/payments/webhooks/{provider}`
- `POST /v1/returns`
- `GET /v1/promises`

#### Internal APIs

- scan ingestion
- task acknowledgement
- manifest creation
- replenishment approval
- node capacity update

#### API Governance Rules

- version in URI: `/v1`
- idempotency on all mutating endpoints
- cursor pagination for list APIs
- structured error envelope
- correlation IDs mandatory
- request/response schemas in OpenAPI
- signed webhooks
- explicit rate limits
- strict authz checks at object and function level

#### Security Basis

OWASP API Security Top 10 highlights broken object authorization, broken authentication, unrestricted resource consumption, improper inventory management, and unsafe API consumption as recurring risks. [R17]

### ADR-016: AI Architecture

#### Decision

Use:

- offline training in S3/SageMaker pipelines
- online feature store in Redis/DynamoDB
- lightweight inference for promise/allocation/risk
- batch inference for demand and replenishment planning

#### Why

This preserves the hot-path latency budget while still enabling operational intelligence.

#### Why Not Heavy Model Calls in the Request Path

- unstable latency
- higher outage coupling
- expensive synchronous dependencies

### ADR-017: Compliance Scope Minimization

#### Decision

Design the system to minimize the scope of regulated data.

Examples:

- do not store full card data unless absolutely required
- isolate payment and PCI-sensitive components in dedicated accounts and namespaces
- tokenize or offload payment card handling to PSP-hosted flows when possible
- restrict access to PII by account, role, and workload

#### Why

AWS Artifact provides compliance reports, but customers remain responsible for compliance in the cloud. [R15]

AWS PCI guidance makes it clear that PCI obligations apply when storing, processing, or transmitting cardholder data. [R18]

#### Practical Outcome

- payment service is isolated
- audit logs are immutable
- data retention is explicit
- PII fields are classified and masked in logs

## 6. DevOps and Delivery Operating Model

### 6.1 Team Structure

Recommended team topology:

- `platform team`
- `fulfillment core team`
- `warehouse execution team`
- `shipment network team`
- `data/ai team`
- `security/sre guild`

Each product team owns:

- service code
- dashboards
- alerts
- on-call rotation
- operational docs

Platform owns:

- EKS base
- GitOps
- shared observability
- secrets and IAM patterns
- golden templates

### 6.2 SDLC

Recommended:

- trunk-based development
- short-lived feature branches
- mandatory PR review
- automated policy checks
- ADRs for major technical choices

### 6.3 CI Pipeline

Every PR:

- lint
- unit tests
- contract tests
- OpenAPI validation
- SAST
- dependency vulnerability scan
- SBOM generation
- image scan

Every merge to main:

- build immutable image
- sign image
- publish to ECR
- update GitOps manifests
- deploy to dev
- run integration tests

Promotion pipeline:

- dev -> staging -> perf -> prod

### 6.4 Progressive Delivery

Production release flow:

- canary for stateless services
- blue/green for high-risk control plane services
- two-phase for protocol/schema changes
- shadow traffic for AI model validation where possible

### 6.5 Release Gates

Before prod release:

- rollback plan exists
- dashboards exist
- alerts exist
- SLOs defined
- runbook exists
- load test passed
- backward compatibility verified

## 7. AWS Deployment Blueprint

### 7.1 Account Model

Recommended accounts:

- `org-management`
- `security`
- `log-archive`
- `shared-networking`
- `shared-observability`
- `prod-ap-south-1`
- `staging-ap-south-1`
- `prod-us-east-1`
- `staging-us-east-1`
- `data-ml`
- `pci-payments`

### 7.2 Regional Network Layout

Per production region:

- one VPC per environment
- private subnets across 3 AZs
- public subnets only for ingress components where required
- ALB/NLB at ingress
- EKS in private subnets
- DBs in isolated subnets
- VPC endpoints for AWS services

### 7.3 EKS Layout

Namespaces:

- `edge`
- `core-order`
- `inventory`
- `warehouse`
- `shipment`
- `scan`
- `platform`
- `observability`
- `ai-online`

Node pools:

- managed node groups for steady critical services
- Karpenter node pools for burst workers and AI inference
- taints and affinity for special workloads

### 7.4 Storage and Stateful Services

- Aurora PostgreSQL Multi-AZ per domain
- DynamoDB regional tables for scans/features
- ElastiCache Redis cluster mode enabled
- EBS CSI for stateful pods that require block storage

### 7.5 DR and Backups

- Multi-AZ as baseline
- cross-region snapshots for Aurora
- DynamoDB backups and point-in-time recovery
- S3 versioning and lifecycle
- disaster recovery drills every quarter

## 8. API Governance Plan

### 8.1 Public API Rules

- REST over HTTPS
- OpenAPI 3.x as source of truth
- JSON payloads
- semantic versioning at API package level
- URI versioning for breaking changes

### 8.2 Required Headers

- `Authorization`
- `Idempotency-Key`
- `X-Correlation-Id`
- `traceparent`
- `X-Client-Version`

### 8.3 Error Contract

```json
{
  "error": {
    "code": "ORDER_ALREADY_EXISTS",
    "message": "Order with this idempotency key already exists",
    "correlationId": "01HV...",
    "retryable": false,
    "details": {}
  }
}
```

### 8.4 Webhooks

Webhook rules:

- signed payloads
- replay protection
- timestamp validation
- retry with dead-lettering
- idempotent receivers

### 8.5 Rate Limits

Public API limits:

- customer-level
- IP-level
- token-level
- flow-specific limits for vulnerable endpoints

This is directly aligned with OWASP API concerns around resource consumption and business-flow abuse. [R17]

## 9. Security and Compliance Controls

### 9.1 Identity

- SSO for humans
- IAM Identity Center for workforce access
- IRSA for workloads
- no long-lived static credentials in pods

### 9.2 Data Protection

- TLS everywhere
- KMS-backed encryption at rest
- Secrets Manager for application secrets
- parameter store for non-secret config
- field-level masking in logs

### 9.3 Network Protection

- default-deny network policies
- security groups for pods when accessing RDS or other AWS services
- WAF at internet edge
- optional mTLS for critical east-west paths

### 9.4 Auditability

- CloudTrail organization-wide
- centralized log archive account
- immutable audit logs
- deployment audit trail from GitOps
- security event routing to SIEM

### 9.5 Compliance Targets

Design for alignment with:

- SOC 2
- ISO 27001
- PCI DSS where payment scope exists
- GDPR / data minimization where applicable

Important note:

AWS compliance reports help, but they do not make the application compliant by themselves. The application and operating model must still satisfy control objectives. [R15][R18]

## 10. Observability and SRE Model

### 10.1 Golden Signals

Every service must expose:

- request rate
- latency
- errors
- saturation

### 10.2 Business SLIs

Key SLIs:

- order accept success rate
- payment auth success rate
- inventory reservation latency
- pick task completion time
- manifest creation latency
- scan ingestion lag
- delivery milestone delay
- replenishment SLA hit rate

### 10.3 Tracing

Use OpenTelemetry end to end.

Every major object carries:

- `orderId`
- `shipmentId`
- `taskId`
- `traceId`

### 10.4 SLOs

Suggested initial SLOs:

- `99.95%` order intake availability
- p95 order accept `< 200 ms`
- p95 reservation result `< 1.5 s`
- p95 scan ingestion `< 2 s`
- p95 order status projection freshness `< 5 s`

### 10.5 On-call and Incident Management

- weekly primary and secondary on-call
- service-level runbooks
- incident commander model
- postmortems for sev1/sev2
- error budget review each sprint

## 11. Data Governance

### 11.1 Retention

- operational OLTP data retained per business requirement
- raw event logs archived to S3
- hot observability data with shorter retention
- legal and financial records retained per jurisdiction

### 11.2 Classification

Classify data as:

- public
- internal
- confidential
- regulated

Apply stricter access and retention policies as classification rises.

### 11.3 PII Handling

- minimize PII propagation in events
- prefer references over copied PII payloads
- encrypt regulated fields
- avoid logging addresses, phone numbers, payment metadata in plaintext

## 12. AI/MLOps Operating Model

### 12.1 Model Lifecycle

- feature definition
- offline training
- evaluation
- approval gate
- deployment
- drift monitoring
- retraining

### 12.2 Model Deployment Strategy

Use:

- canary for online scoring services
- shadow mode before traffic promotion
- offline backtesting for planning models

### 12.3 Model Risk Controls

- version every model
- version every feature schema
- store lineage for training datasets
- enforce rollback for online models
- human override for critical replenishment and allocation policies

## 13. What We Explicitly Choose Not To Use

We reject the following as defaults:

- full event sourcing everywhere
- single global active-active transactional plane
- one shared monolithic database
- synchronous orchestration in the request path
- hidden fallback paths to “direct DB mode”
- self-managed Kubernetes for core production
- single-account production estate
- mandatory full-service mesh from day one
- unrestricted east-west traffic
- manual production deploys
- storing cardholder data broadly across services

## 14. Final Recommended Production Shape

The final recommended production system is:

- `regional`
- `cell-based`
- `event-driven`
- `state-machine based`
- `Aurora + DynamoDB + Redis + Kafka`
- `EKS managed control plane`
- `GitOps deployed`
- `zero-trust aligned`
- `compliance-scope minimized`
- `AI-assisted, not AI-dependent`

This is the architecture we recommend building.

## 15. Open Decisions for Final Confirmation

These are the last decisions the team should confirm before implementation:

1. Aurora PostgreSQL vs Aurora PostgreSQL + selective DynamoDB reservations for specific ultra-hot inventory partitions
2. Argo CD vs Flux if the platform team prefers CLI-only workflows
3. selective service mesh vs broader mesh adoption in core namespaces
4. exact payment-provider model and PCI scope boundary
5. regional topology for initial rollout and DR target

## 16. Implementation Order

1. landing zone and account structure
2. EKS platform baseline
3. GitOps and CI/CD
4. order-intake and order-state
5. payment and inventory ledger
6. warehouse orchestration and task execution
7. shipment network and scan ingestion
8. observability and SRE controls
9. AI feature platform and planning models
10. compliance hardening and audits

## 17. Summary

If this were a real production program, the key message to the team would be:

- keep the request path tiny
- isolate failures by region, account, and domain
- protect every mutation with idempotency and outbox
- avoid fallback magic
- make rollback safety a first-class design requirement
- use AWS managed primitives where they meaningfully lower risk
- let AI improve decisions, not hold the system hostage

That is the final production design posture for the new system.

## 18. References

`[R1]` AWS Well-Architected Framework  
https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html

`[R2]` Amazon Builders’ Library: Static stability using Availability Zones  
https://aws.amazon.com/builders-library/static-stability-using-availability-zones/

`[R3]` Amazon Builders’ Library: Making retries safe with idempotent APIs  
https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/

`[R4]` AWS Prescriptive Guidance: Transactional outbox pattern  
https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/transactional-outbox.html

`[R5]` Amazon EKS Best Practices: Reliability  
https://docs.aws.amazon.com/eks/latest/best-practices/reliability.html

`[R6]` Amazon EKS Best Practices: Security  
https://docs.aws.amazon.com/eks/latest/best-practices/security.html

`[R7]` Amazon EKS Best Practices: Network security  
https://docs.aws.amazon.com/eks/latest/best-practices/network-security.html

`[R8]` Amazon EKS Best Practices: Cluster Autoscaler  
https://docs.aws.amazon.com/eks/latest/best-practices/cas.html

`[R9]` Amazon EKS Best Practices: Karpenter  
https://docs.aws.amazon.com/eks/latest/best-practices/karpenter.html

`[R10]` Amazon Builders’ Library: Timeouts, retries, and backoff with jitter  
https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/

`[R11]` Amazon Builders’ Library: Avoiding fallback in distributed systems  
https://aws.amazon.com/builders-library/avoiding-fallback-in-distributed-systems/

`[R12]` Amazon Builders’ Library: Ensuring rollback safety during deployments  
https://aws.amazon.com/builders-library/ensuring-rollback-safety-during-deployments/

`[R13]` AWS Prescriptive Guidance: Argo CD for EKS GitOps  
https://docs.aws.amazon.com/prescriptive-guidance/latest/eks-gitops-tools/argo-cd.html

`[R14]` AWS Control Tower: AWS multi-account strategy  
https://docs.aws.amazon.com/controltower/latest/userguide/aws-multi-account-landing-zone.html

`[R15]` AWS Artifact: What is AWS Artifact?  
https://docs.aws.amazon.com/artifact/latest/ug/what-is-aws-artifact.html

`[R16]` NIST SP 800-207: Zero Trust Architecture  
https://csrc.nist.gov/pubs/sp/800/207/final

`[R17]` OWASP API Security Top 10  
https://owasp.org/API-Security/

`[R18]` AWS PCI FAQs  
https://aws.amazon.com/compliance/pci-faqs/

`[R19]` Kubernetes: Liveness, Readiness, and Startup Probes  
https://kubernetes.io/docs/concepts/configuration/liveness-readiness-startup-probes/

`[R20]` Kubernetes: Pod Disruption Budgets  
https://kubernetes.io/docs/tasks/run-application/configure-pdb/
