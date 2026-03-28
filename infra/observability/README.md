# Observability Stack

Local observability stack for the repository:

- `Grafana` for dashboards and drill-down
- `Prometheus` for metrics
- `Loki` for log aggregation
- `Grafana Alloy` for local file log shipping and collector-side log processing
- `OpenTelemetry Collector` for telemetry intake and routing
- `Jaeger` for trace storage and trace UI
- `Alertmanager` for alert routing
- `Blackbox Exporter` for synthetic HTTP probe monitoring

This stack is intentionally local-infra only. It does not require Java implementation code to exist yet.

## Current Pinned Versions

- Grafana: `12.4.2`
- Prometheus: `3.10.0`
- Loki: `3.7.0`
- Grafana Alloy: `1.14.2`
- Jaeger: `2.16.0`
- OpenTelemetry Collector Contrib: `0.148.0`
- Alertmanager: `0.31.1`
- Blackbox Exporter: `0.28.0`

## Ports

- Grafana: `3000`
- Prometheus: `9090`
- Loki: `3100`
- Alertmanager: `9093`
- Blackbox Exporter: `9115`
- Jaeger UI: `16686`
- Alloy metrics UI: `12345`
- OTel gRPC: `4317`
- OTel HTTP: `4318`
- OTel metrics: `8888`, `8889`

## Local Convention For Future Services

- logs: write structured JSON to `./logs/<service>.log` in local mode
- metrics: expose Prometheus metrics on `/actuator/prometheus`
- traces: export OTLP to `http://localhost:4317` or `http://localhost:4318`
- keep only low-cardinality log labels such as `service`, `env`, and `level`
- keep `trace_id`, `span_id`, and `request_id` inside the JSON log body, not as Loki labels

## Included Production-Shaped Baseline

- prewired Grafana datasources for Prometheus, Loki, Jaeger, and Alertmanager
- Prometheus alert rules for control-plane health and missing app targets
- Blackbox probes for Grafana, Prometheus, Loki, Jaeger, Alertmanager, and OTel Collector
- OTel Collector health endpoint on `13133`
- platform dashboards for stack health and telemetry flow

## Start

```bash
docker compose -f docker-compose.infra.yml pull
docker compose -f docker-compose.infra.yml up -d
```

## First Login

- Grafana user: `admin`
- Grafana password: `iwos_admin`

## First Use

1. Open Grafana at `http://localhost:3000`.
2. Sign in with `admin` / `iwos_admin`.
3. Open the `IWOS Observability Overview` dashboard.
4. Open the `IWOS Platform Health` dashboard.
5. Confirm Prometheus, Loki, Jaeger, Alloy, Alertmanager, and OTel Collector are up.

## See Your First Log

Run this once from the repo root:

```bash
echo '{"timestamp":"2026-03-27T12:00:00Z","level":"INFO","service":"demo-service","env":"local","trace_id":"trace-123","span_id":"span-123","request_id":"req-123","message":"hello from local dev"}' >> logs/demo-service.log
```

Then in Grafana:

1. Go to `Explore`.
2. Choose the `Loki` datasource.
3. Run: `{job="iwos-local",service="demo-service"}`
4. If you want to parse the JSON fields, run: `{job="iwos-local",service="demo-service"} | json`

## Add A Local Service Later

When a future Spring service runs on your Mac:

1. Expose metrics on `/actuator/prometheus`.
2. Add its host port in [services.yml](/Users/vikas/Documents/capstone/IWOS/infra/observability/prometheus/targets/services.yml).
3. Reload Prometheus from `http://localhost:9090/-/reload` or restart the stack.
4. Write JSON logs to `./logs/<service>.log`.
5. Send traces to `localhost:4317` or `localhost:4318`.

## Common Checks

- stack status: `docker compose -f docker-compose.infra.yml ps`
- stop stack: `docker compose -f docker-compose.infra.yml down`
- reset all observability data: `docker compose -f docker-compose.infra.yml down -v`
