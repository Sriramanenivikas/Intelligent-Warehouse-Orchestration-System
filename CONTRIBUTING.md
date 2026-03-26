# Contributing

This repository is currently a planning and scaffolding repo. Treat every change as architecture or platform work unless a service implementation phase has been explicitly started.

## Branching

- `main` is the protected production trunk.
- `develop` is the protected integration branch.
- Use short-lived branches only:
  - `feat/<name>`
  - `fix/<name>`
  - `chore/<name>`
  - `docs/<name>`
  - `refactor/<name>`
  - `hotfix/<name>`

## Pull Requests

- Open PRs into `develop` for normal work.
- Open PRs into `main` only for release promotion or hotfixes.
- Use conventional PR titles:
  - `feat(scope): summary`
  - `fix(scope): summary`
  - `docs(scope): summary`
  - `chore(scope): summary`
- Keep PRs focused. Do not mix architecture, infra, and unrelated service changes in one PR.

## Required Engineering Rules

1. Start with the canonical docs under `docs/`.
2. Do not add new services without updating the service catalog and architecture decisions.
3. Keep service boundaries aligned with bounded contexts.
4. Add OpenAPI and AsyncAPI definitions before adding implementation logic.
5. Add migrations before adding persistence logic.
6. Add Helm and environment configuration alongside each service change.
7. Keep idempotency, retries, and observability explicit.
8. Do not add fake deployment automation that cannot be exercised honestly.
9. Do not introduce long-lived environment branches.

## Merge Policy

- Prefer squash merges for feature and chore branches.
- Use hotfix branches only for urgent fixes that must go directly to `main`.
- After every `main` hotfix, back-merge to `develop`.
