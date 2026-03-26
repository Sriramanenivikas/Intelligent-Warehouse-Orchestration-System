# Intelligent Warehouse Orchestration System

Documentation-first planning skeleton for a production-style unified fulfillment platform.

The repository is intentionally prepared for implementation without carrying legacy Java application code.

## Start Here

- [Documentation Index](docs/README.md)
- [Project Context](docs/architecture/project-context.md)
- [Service Catalog](docs/architecture/service-catalog.md)
- [Architecture Decisions](docs/decisions/production-architecture-decisions.md)
- [Deployment Topology](docs/devops/deployment-topology.md)
- [GitHub Team Operating Model](docs/devops/github-team-operating-model.md)
- [GitHub Repository Setup](docs/devops/github-repository-setup.md)

## Repository Shape

- `services/`: bounded-context service skeletons
- `platform/`: shared modules and Kong edge skeleton
- `contracts/`: OpenAPI, AsyncAPI, and event contracts
- `deploy/`: Helm and environment scaffolding
- `infra/`: Terraform module and environment scaffolding
- `tests/`: contract, integration, and performance-test scaffolding
- `runbooks/`: operations, compliance, and recovery runbooks

## Current State

- no Java implementation code
- no business logic
- service, package, and deployment structure only
- starter infra/runtime files ready for implementation

## Validation

The current repository validates as a Maven reactor:

```bash
mvn -q -DskipTests validate
```

## GitHub Working Model

- `main`: protected production trunk
- `develop`: protected integration branch
- short-lived branches only: `feat/*`, `fix/*`, `chore/*`, `docs/*`, `refactor/*`, `hotfix/*`
- pull requests are required for `main` and `develop`
- GitHub Actions validates structure, docs, and repo integrity on every PR
