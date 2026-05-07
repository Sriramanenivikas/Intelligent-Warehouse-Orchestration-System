import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Grid,
  LinearProgress,
  Stack,
  Typography,
} from "@mui/material";
import { api, formatDateTime } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";

export function OverviewPage({ token }: { token: string }) {
  const forecastSummary = useQuery({
    queryKey: ["forecast-summary"],
    queryFn: () => api.getForecastSummary(token),
    refetchInterval: 30000,
  });
  const controlTower = useQuery({
    queryKey: ["control-tower-latest"],
    queryFn: () => api.getControlTowerLatest(token),
    refetchInterval: 30000,
  });
  const modelRun = useQuery({
    queryKey: ["model-run-latest"],
    queryFn: () => api.getModelRunLatest(token),
    refetchInterval: 30000,
  });

  if (forecastSummary.isError || controlTower.isError || modelRun.isError) {
    return <Alert severity="error">Overview upstreams are unavailable right now. Refresh after the backend cell is healthy.</Alert>;
  }

  const loading = forecastSummary.isLoading || controlTower.isLoading || modelRun.isLoading;

  return (
    <Stack spacing={3}>
      <Box sx={{ animation: "fadeUp 420ms ease both" }}>
        <PageHeader
          badges={[
            { label: "Gateway Secured", color: "primary" },
            { label: "Planning Active", color: "secondary" },
            { label: "Control Tower Live", color: "success" },
          ]}
          description="This is the executive surface for the demo. It combines AI planning, order execution, shipment state, and customer communication signals into one operator-grade command layer."
          eyebrow="Mission Control"
          title="Regional Cell Command"
        />
      </Box>

      {loading ? <LinearProgress /> : null}

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 460ms ease both" }}>
          <StatCard
            helper={`Generated ${formatDateTime(forecastSummary.data?.generatedAt)}`}
            label="Forecast Rows"
            value={forecastSummary.data?.totalForecasts ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 520ms ease both" }}>
          <StatCard
            helper={`High risk ${forecastSummary.data?.highCount ?? 0}`}
            label="Replenishment Units"
            value={forecastSummary.data?.totalRecommendedReplenishmentQuantity ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 580ms ease both" }}>
          <StatCard
            helper={`Completed ${formatDateTime(modelRun.data?.trainingCompletedAt)}`}
            label="Champion Model"
            value={modelRun.data?.registeredModelVersion ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 640ms ease both" }}>
          <StatCard
            helper={`Snapshot ${formatDateTime(controlTower.data?.generatedAt)}`}
            label="Open Exceptions"
            value={controlTower.data?.recentExceptions.length ?? "-"}
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 8 }} sx={{ animation: "fadeUp 700ms ease both" }}>
          <SectionCard
            subtitle="Highest-priority replenishment candidates from the latest planning cycle."
            title="Priority Inventory Actions"
          >
            {(forecastSummary.data?.topReplenishmentForecasts ?? []).slice(0, 5).map((forecast) => {
              const width = Math.min(100, Math.max(8, forecast.predicted24hDemand * 2));

              return (
                <Grid
                  alignItems="center"
                  container
                  key={forecast.forecastId}
                  spacing={2}
                  sx={{
                    borderBottom: "1px solid",
                    borderColor: "divider",
                    pb: 2.25,
                    "&:last-of-type": { borderBottom: "none", pb: 0 },
                  }}
                >
                  <Grid size={{ xs: 12, md: 4 }}>
                    <Typography fontWeight={800} variant="body1">
                      {forecast.sku}
                    </Typography>
                    <Typography color="text.secondary" variant="body2">
                      {forecast.nodeId}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <Box
                      sx={{
                        height: 10,
                        borderRadius: 999,
                        bgcolor: "rgba(15,94,168,0.08)",
                        overflow: "hidden",
                      }}
                    >
                      <Box
                        sx={{
                          width: `${width}%`,
                          height: "100%",
                          borderRadius: 999,
                          background: "linear-gradient(90deg, #0f5ea8, #ff8a3d)",
                        }}
                      />
                    </Box>
                    <Typography color="text.secondary" sx={{ mt: 0.75 }} variant="caption">
                      15m demand {forecast.predicted15mDemand} · 24h demand {forecast.predicted24hDemand}
                    </Typography>
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <StatusChip value={forecast.stockoutRisk} />
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <Typography fontWeight={800} variant="body1">
                      +{forecast.recommendedReplenishmentQuantity}
                    </Typography>
                    <Typography color="text.secondary" variant="caption">
                      Days cover {forecast.daysOfCover}
                    </Typography>
                  </Grid>
                </Grid>
              );
            })}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 4 }} sx={{ animation: "fadeUp 760ms ease both" }}>
          <SectionCard
            subtitle="Training quality and live operational posture for the current cell."
            title="Model and Cell Health"
          >
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Algorithm
                </Typography>
                <Typography fontWeight={700} variant="body1">
                  {modelRun.data?.algorithm ?? "-"}
                </Typography>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Status
                </Typography>
                <StatusChip value={modelRun.data?.trainingStatus ?? "UNKNOWN"} />
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  MAE
                </Typography>
                <Typography variant="body1">{modelRun.data?.mae ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  RMSE
                </Typography>
                <Typography variant="body1">{modelRun.data?.rmse ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  R²
                </Typography>
                <Typography variant="body1">{modelRun.data?.r2 ?? "-"}</Typography>
              </Grid>
            </Grid>

            <Box sx={{ borderTop: "1px solid", borderColor: "divider", pt: 2 }}>
              <Grid container spacing={1.5}>
                <Grid size={{ xs: 12 }}>
                  <Typography color="text.secondary" variant="caption">
                    Shipments
                  </Typography>
                </Grid>
                {(controlTower.data?.shipmentsByStatus ?? []).slice(0, 3).map((item) => (
                  <Grid key={item.key} size={{ xs: 12 }}>
                    <Stack alignItems="center" direction="row" justifyContent="space-between">
                      <Typography variant="body2">{item.key}</Typography>
                      <Typography fontWeight={800} variant="body2">
                        {item.count}
                      </Typography>
                    </Stack>
                  </Grid>
                ))}
              </Grid>
            </Box>
          </SectionCard>
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 4 }} sx={{ animation: "fadeUp 820ms ease both" }}>
          <SectionCard subtitle="Current order ingestion state." title="Order Intake Snapshot">
            {(controlTower.data?.orderIntentsByStatus ?? []).map((item) => (
              <Stack alignItems="center" direction="row" justifyContent="space-between" key={item.key}>
                <Typography variant="body2">{item.key}</Typography>
                <Typography fontWeight={800} variant="body2">
                  {item.count}
                </Typography>
              </Stack>
            ))}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }} sx={{ animation: "fadeUp 880ms ease both" }}>
          <SectionCard subtitle="Execution backlog across fulfillment and network." title="Execution State">
            {(controlTower.data?.fulfillmentOrdersByStatus ?? []).map((item) => (
              <Stack alignItems="center" direction="row" justifyContent="space-between" key={item.key}>
                <Typography variant="body2">{item.key}</Typography>
                <Typography fontWeight={800} variant="body2">
                  {item.count}
                </Typography>
              </Stack>
            ))}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }} sx={{ animation: "fadeUp 940ms ease both" }}>
          <SectionCard subtitle="Signal generation across scans and notifications." title="Live Signals">
            <Stack alignItems="center" direction="row" justifyContent="space-between">
              <Typography variant="body2">Customer notifications</Typography>
              <Typography fontWeight={800} variant="body2">
                {controlTower.data?.notificationsByAudience[0]?.count ?? 0}
              </Typography>
            </Stack>
            <Stack alignItems="center" direction="row" justifyContent="space-between">
              <Typography variant="body2">Recent scan types</Typography>
              <Typography fontWeight={800} variant="body2">
                {controlTower.data?.scanEventsByType.length ?? 0}
              </Typography>
            </Stack>
          </SectionCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
