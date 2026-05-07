import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Chip, Grid, Stack, Typography } from "@mui/material";
import { api, formatDateTime } from "../api";
import { StatCard } from "../components/StatCard";

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

  return (
    <Stack spacing={3}>
      <Stack spacing={0.5}>
        <Typography variant="h4">Overview</Typography>
        <Typography color="text.secondary">
          Live operations and planning summary for the current regional cell.
        </Typography>
      </Stack>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard
            helper={formatDateTime(forecastSummary.data?.generatedAt)}
            label="Forecast Rows"
            value={forecastSummary.data?.totalForecasts ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard
            helper={`Critical ${forecastSummary.data?.criticalCount ?? 0} · High ${forecastSummary.data?.highCount ?? 0}`}
            label="Recommended Replenishment"
            value={forecastSummary.data?.totalRecommendedReplenishmentQuantity ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard
            helper={formatDateTime(modelRun.data?.trainingCompletedAt)}
            label="Latest Model Version"
            value={modelRun.data?.registeredModelVersion ?? "-"}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard
            helper={`Snapshot ${formatDateTime(controlTower.data?.generatedAt)}`}
            label="Open Exceptions"
            value={controlTower.data?.recentExceptions.length ?? "-"}
          />
        </Grid>
      </Grid>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 7 }}>
          <Card>
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h6">Top Replenishment Candidates</Typography>
                {forecastSummary.data?.topReplenishmentForecasts.map((forecast) => (
                  <Stack
                    alignItems="center"
                    direction="row"
                    justifyContent="space-between"
                    key={forecast.forecastId}
                    spacing={2}
                  >
                    <Stack>
                      <Typography variant="body1">
                        {forecast.nodeId} · {forecast.sku}
                      </Typography>
                      <Typography color="text.secondary" variant="body2">
                        15m demand {forecast.predicted15mDemand} · 24h demand {forecast.predicted24hDemand}
                      </Typography>
                    </Stack>
                    <Stack alignItems="flex-end">
                      <Chip color="warning" label={forecast.stockoutRisk} size="small" />
                      <Typography variant="body2">Replenish {forecast.recommendedReplenishmentQuantity}</Typography>
                    </Stack>
                  </Stack>
                ))}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card>
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h6">Model Quality</Typography>
                <Typography variant="body2">Algorithm: {modelRun.data?.algorithm ?? "-"}</Typography>
                <Typography variant="body2">MAE: {modelRun.data?.mae ?? "-"}</Typography>
                <Typography variant="body2">RMSE: {modelRun.data?.rmse ?? "-"}</Typography>
                <Typography variant="body2">R²: {modelRun.data?.r2 ?? "-"}</Typography>
                <Typography variant="body2">
                  Samples: {modelRun.data?.trainingSampleCount ?? "-"} train / {modelRun.data?.validationSampleCount ?? "-"} validation
                </Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Stack>
  );
}

