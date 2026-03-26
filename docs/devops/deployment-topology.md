# Deployment Topology

Target runtime model:

- `AWS multi-account landing zone`
- `EKS` for application workloads
- `MSK` for event backbone
- `Aurora PostgreSQL` for transactional domains
- `Redis` for hot state and idempotency
- `DynamoDB` for append-heavy online state and timelines
- `S3` for data lake and model training inputs
- `Kong` at the north-south edge
- `GitHub Actions + GitHub Environments` for repository-native CI/CD control
- `Helm` as the deployment packaging unit

This repo contains structure only. Environment-specific runtime configuration should be added later under `deploy/` and `infra/`.
