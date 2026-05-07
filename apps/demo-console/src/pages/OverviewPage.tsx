import { useQuery } from "@tanstack/react-query";
import { Alert, Grid, LinearProgress, Stack, Typography } from "@mui/material";
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
    return <Alert severity="error">Unable to load overview data from the gateway-backed services.</Alert>;
  }

  const loading = forecastSummary.isLoading || controlTower.isLoading || modelRun.isLoading;

  return (
    <Stack spacing={3}>
      <PageHeader
        badges={[
          { label: "Kong Edge", color: "primary" },
          { label: "JWT / RS256", color: "secondary" },
          { label: "Control Tower", color: "success" },
        ]}
        description="This console tracks the production-style demo flow from accepted order intent through warehouse orchestration, shipment movement, notifications, and AI planning outputs."
        eyebrow="Platform Status"
        title="Regional Cell Overview"
      />

      {loading ? <LinearProgress /> : null}

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={`Generated ${formatDateTime(forecastSummary.data?.generatedAt)}`}
            label="Forecast Rows"
            value={forecastSummary.data?.totalForecasts ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={`Critical ${forecastSummary.data?.criticalCount ?? 0} · High ${forecastSummary.data?.highCount ?? 0}`}
            label="Replenishment Units"
            value={forecastSummary.data?.totalRecommendedReplenishmentQuantity ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={`Completed ${formatDateTime(modelRun.data?.trainingCompletedAt)}`}
            label="Model Version"
            value={modelRun.data?.registeredModelVersion ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={`Snapshot ${formatDateTime(controlTower.data?.generatedAt)}`}
            label="Recent Exceptions"
            value={controlTower.data?.recentExceptions.length ?? "-"}
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 7 }}>
          <SectionCard
            subtitle="High-signal items surfaced from the latest forecast batch."
            title="Top Replenishment Candidates"
          >
            {(forecastSummary.data?.topReplenishmentForecasts ?? []).slice(0, 5).map((forecast) => (
              <Grid
                alignItems="center"
                container
                key={forecast.forecastId}
                spacing={2}
                sx={{
                  borderBottom: "1px solid",
                  borderColor: "divider",
                  pb: 2,
                  "&:last-of-type": { borderBottom: "none", pb: 0 },
                }}
              >
                <Grid size={{ xs: 12, md: 4 }}>
                  <Typography fontWeight={600} variant="body1">
                    {forecast.nodeId}
                  </Typography>
                  <Typography color="text.secondary" variant="body2">
                    {forecast.sku}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <Typography variant="body2">15m demand {forecast.predicted15mDemand}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    24h demand {forecast.predicted24hDemand}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, md: 2 }}>
                  <StatusChip value={forecast.stockoutRisk} />
                </Grid>
                <Grid size={{ xs: 12, md: 3 }}>
                  <Typography fontWeight={600} variant="body1">
                    Replenish {forecast.recommendedReplenishmentQuantity}
                  </Typography>
                  <Typography color="text.secondary" variant="body2">
                    Days cover {forecast.daysOfCover}
                  </Typography>
                </Grid>
              </Grid>
            ))}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 5 }}>
          <SectionCard subtitle="Live training metadata from the latest registered model run." title="Model Quality">
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Algorithm
                </Typography>
                <Typography variant="body1">{modelRun.data?.algorithm ?? "-"}</Typography>
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
              <Grid size={{ xs: 12 }}>
                <Typography color="text.secondary" variant="caption">
                  Samples
                </Typography>
                <Typography variant="body1">
                  {modelRun.data?.trainingSampleCount ?? "-"} training / {modelRun.data?.validationSampleCount ?? "-"} validation
                </Typography>
              </Grid>
            </Grid>
          </SectionCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
