# GitHub Repository Setup

Apply these GitHub settings in the repository UI.

## General

- default branch: `main`
- auto-delete head branches: enabled
- squash merge: enabled
- merge commit: enabled
- rebase merge: disabled

## Branch Protection / Rulesets

### `main`

- require pull request before merge
- require at least 2 approvals
- dismiss stale approvals when new commits are pushed
- require conversation resolution
- require status checks:
  - `PR Gate`
- block force pushes for non-admins
- block deletions

### `develop`

- require pull request before merge
- require at least 1 approval
- require conversation resolution
- require status checks:
  - `PR Gate`
- block force pushes for non-admins

## Environments

Create these GitHub Environments:

- `dev`
- `stage`
- `perf`
- `prod`

Recommended protection:

- `dev`: optional reviewer gate
- `stage`: 1 reviewer
- `perf`: 1 reviewer
- `prod`: 2 reviewers, protected branches only

## Secrets And Variables

Prefer GitHub OIDC with AWS roles. Do not store long-lived AWS access keys.

Repository or environment variables:

- `AWS_REGION`
- `ECR_REGISTRY`
- `EKS_CLUSTER_NAME`
- `HELM_RELEASE_PREFIX`

Environment secrets or variables:

- `AWS_ROLE_ARN`
- `K8S_NAMESPACE`

## Releases

- use semantic tags: `v1.0.0`
- generate release notes from merged PRs
- only tag from `main`
