# GitHub Team Operating Model

This is the recommended GitHub workflow for a real team of around 10 engineers.

## Non-Negotiable Position

If this becomes a serious multi-engineer program, the repository should live in a GitHub organization, not only in a personal account.

Why:

- real GitHub teams and CODEOWNERS routing
- branch protection and repository administration by more than one person
- cleaner auditability and offboarding
- environment ownership and secret governance

Until that move happens, this repository uses user-safe defaults.

## Branch Model

- `main`: protected production trunk
- `develop`: protected integration branch
- short-lived working branches:
  - `feat/*`
  - `fix/*`
  - `chore/*`
  - `docs/*`
  - `refactor/*`
  - `hotfix/*`

Do not create long-lived `dev`, `stage`, `perf`, or `prod` branches. Those are environments, not branches.

## PR Flow

- normal work: branch from `develop`, merge back to `develop`
- release promotion: `develop` to `main`
- emergency fix: `hotfix/*` to `main`, then back-merge to `develop`

## Required Checks

These checks should block merges to `main` and `develop`:

- `Repo Validate`
- `Docs Guard`
- `Infra Guard`
- `Structure Guard`
- `PR Guard`

## Review Expectations

- architecture or service-boundary changes: at least 2 reviewers
- docs-only or repo-hygiene changes: at least 1 reviewer
- direct pushes to `main` and `develop`: disabled except administrators

## Merge Strategy

- enable `Squash and merge`
- enable `Merge commit`
- disable `Rebase and merge` unless the team explicitly wants it
- auto-delete merged branches

## Labels

Use at least these labels:

- `architecture`
- `documentation`
- `infra`
- `platform`
- `commerce`
- `fulfillment`
- `network`
- `intelligence`
- `security`
- `release`
- `triage`

## Environments

Use GitHub Environments:

- `dev`
- `stage`
- `perf`
- `prod`

`prod` should require at least 2 reviewers and a manual approval gate.
