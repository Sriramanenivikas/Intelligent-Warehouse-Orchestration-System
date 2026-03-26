# Delivery And CI/CD

Current delivery posture for this repository:

- PR validation through GitHub Actions
- required checks on `main` and `develop`
- release notes generated from merged PRs
- GitHub Environments used as approval gates for `dev`, `stage`, `perf`, and `prod`
- deployment automation kept intentionally minimal until real infrastructure and service code exist

Planned deployment path later:

1. GitHub Actions builds and validates artifacts
2. GitHub Environments gates promotion
3. GitHub OIDC assumes scoped AWS roles
4. Helm deploys versioned service charts into the target EKS environment

Non-goal for the current repo phase:

- pretending that a full production deployment pipeline already exists
