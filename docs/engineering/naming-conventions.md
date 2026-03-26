# Naming Conventions

Use these defaults:

- services: `kebab-case`
- Java packages later: `com.iwos.<boundedcontext>`
- Kafka topics: `domain.entity.action.v1`
- database schemas: `snake_case`
- tables: `snake_case`
- event versions: explicit `v1`, `v2`, ...
- Git branches: `<type>/<short-name>`
- release tags: `v<major>.<minor>.<patch>`
- Terraform modules: one infrastructure capability per module
