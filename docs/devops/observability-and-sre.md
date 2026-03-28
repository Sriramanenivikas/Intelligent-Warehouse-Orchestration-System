# Observability And SRE

This repository now standardizes on a Grafana-centered observability baseline for local and future shared environments:

- `Grafana` for dashboards and drill-down
- `Prometheus` for metrics
- `Loki` for log aggregation
- `Jaeger` for trace visualization and troubleshooting
- `OpenTelemetry Collector` as the single telemetry intake layer
- `Grafana Alloy` for local file-log shipping to Loki
- `Alertmanager` for alert routing
- `Blackbox Exporter` for synthetic endpoint checks

## Why This Stack

- `OpenTelemetry Collector` keeps future services from wiring exporters directly to multiple backends.
- `Prometheus` remains the simplest metrics backbone for Spring and platform metrics.
- `Loki` is cheaper and operationally lighter than running a full-text search stack just for logs.
- `Jaeger` is acceptable for trace storage and UI at this repo stage, and keeps tracing explicit.
- `Grafana` gives one operator view across metrics, logs, and traces.
- `Alertmanager` gives a real alert path instead of only dashboards.
- `Blackbox Exporter` checks platform endpoints from the outside, not only by internal scrape health.
- `Grafana Alloy` replaces `Promtail`, which is deprecated and EOL in 2026.

## Local Development Contract

Future services should follow these rules from day one:

- logs must be structured JSON
- each service should include `service`, `env`, `trace_id`, `span_id`, and `request_id` in every log line
- only `service`, `env`, and `level` should become Loki labels
- `trace_id`, `span_id`, and `request_id` must stay in the log body or structured metadata, not as Loki labels
- metrics should be exposed on `/actuator/prometheus`
- traces should be exported to OTLP on `localhost:4317` or `localhost:4318`
- local file logs should go to `./logs/<service>.log`

## Current Local Observability Stack

The repository-local stack is started with:

```bash
docker compose -f docker-compose.infra.yml pull
docker compose -f docker-compose.infra.yml up -d
```

Available endpoints:

- Grafana: `http://localhost:3000`
- Prometheus: `http://localhost:9090`
- Loki: `http://localhost:3100`
- Alertmanager: `http://localhost:9093`
- Blackbox Exporter: `http://localhost:9115`
- Jaeger UI: `http://localhost:16686`
- Alloy metrics: `http://localhost:12345/metrics`
- OTel gRPC ingest: `localhost:4317`
- OTel HTTP ingest: `localhost:4318`

Grafana login:

- user: `admin`
- password: `iwos_admin`

Basic operator flow for a new developer:

1. start the stack with `docker compose -f docker-compose.infra.yml up -d`
2. sign in to Grafana
3. confirm the platform dashboards are green
4. add a local JSON log line under `./logs`
5. query it in Loki Explore with `| json`
6. later register host-running service metrics in [services.yml](/Users/vikas/Documents/capstone/IWOS/infra/observability/prometheus/targets/services.yml)

## Non-Negotiable Observability Baseline

Before business logic grows, every service must eventually have:

- health endpoints
- structured logs
- correlation and trace propagation
- Prometheus metrics
- OTLP trace export
- deployment and runtime dashboards
- alerts that map to real operator actions
- synthetic probes for customer-visible endpoints

## SRE Focus

Initial SLO and operational focus areas:

- intake latency
- event lag
- reservation latency
- order confirmation latency
- worker backlog depth
- reconciliation failure count
- alert noise rate
- telemetry pipeline availability

Runbooks should cover:

- rollback
- reconciliation
- incident triage
- telemetry outage handling
- alert routing failure
- dashboard/data-source failure

## Current Stack Scope

The repository baseline now includes:

- metrics scrape for Prometheus, Grafana, Loki, Alloy, OTel Collector, Alertmanager, and Jaeger
- blackbox HTTP probes for control-plane UIs and readiness endpoints
- alert rules for target failure and telemetry control-plane failure
- provisioned Grafana datasources and dashboards

This is enough to start real service development without inventing observability later.
