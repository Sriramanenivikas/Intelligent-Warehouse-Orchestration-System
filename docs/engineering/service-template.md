# Service Template

Every service in this repo follows the same skeleton:

- `README.md`
- `pom.xml`
- `src/main/java/com/iwos/<package>/{api,application,domain,infrastructure,config}`
- `src/main/resources/application.yml`
- `src/test/java/com/iwos/<package>`
- `db/migration/`
- `api/openapi.yaml`
- `events/asyncapi.yaml`
- `deploy/helm/`

No implementation code is included at this stage. This repo is currently a planning and structure scaffold.
