import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Grid,
  LinearProgress,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { api, formatDateTime, riskColor } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";

export function ForecastingPage({ token }: { token: string }) {
  const summaryQuery = useQuery({
    queryKey: ["forecast-summary"],
    queryFn: () => api.getForecastSummary(token),
    refetchInterval: 30000,
  });
  const modelRunQuery = useQuery({
    queryKey: ["model-run"],
    queryFn: () => api.getModelRunLatest(token),
    refetchInterval: 30000,
  });
  const forecastRunQuery = useQuery({
    queryKey: ["forecast-run"],
    queryFn: () => api.getForecastRunLatest(token),
    refetchInterval: 30000,
  });
  const forecastQuery = useQuery({
    queryKey: ["forecast-list"],
    queryFn: () => api.listForecasts(token),
    refetchInterval: 30000,
  });

  if (summaryQuery.isError || modelRunQuery.isError || forecastQuery.isError || forecastRunQuery.isError) {
    return <Alert severity="error">Forecasting upstreams are unavailable right now.</Alert>;
  }

  const summary = summaryQuery.data;
  const model = modelRunQuery.data;
  const forecastRun = forecastRunQuery.data;
  const forecasts = forecastQuery.data ?? [];

  return (
    <Stack spacing={3}>
      <Box sx={{ animation: "fadeUp 420ms ease both" }}>
        <PageHeader
          badges={[
            { label: "MLflow Tracked", color: "primary" },
            { label: "15-Min Refresh", color: "secondary" },
            { label: "Node + SKU Planning", color: "success" },
          ]}
          description="Demand forecasting, replenishment recommendation, and stockout risk are surfaced here as an operator-grade planning workspace rather than a toy dashboard."
          eyebrow="AI Planning"
          title="Forecasting Command"
        />
      </Box>

      {summaryQuery.isLoading || modelRunQuery.isLoading || forecastQuery.isLoading || forecastRunQuery.isLoading ? (
        <LinearProgress />
      ) : null}

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 500ms ease both" }}>
          <StatCard label="High Risk SKUs" value={summary?.highCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 560ms ease both" }}>
          <StatCard label="Critical Risk SKUs" value={summary?.criticalCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 620ms ease both" }}>
          <StatCard label="Model Version" value={model?.registeredModelVersion ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }} sx={{ animation: "fadeUp 680ms ease both" }}>
          <StatCard helper={formatDateTime(forecastRun?.completedAt)} label="Replenishment Units" value={summary?.totalRecommendedReplenishmentQuantity ?? "-"} />
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 4 }} sx={{ animation: "fadeUp 740ms ease both" }}>
          <SectionCard subtitle="Champion model metadata and last completed training run." title="Model Run">
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Algorithm
                </Typography>
                <Typography fontWeight={800} variant="body1">
                  {model?.algorithm ?? "-"}
                </Typography>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Status
                </Typography>
                <StatusChip value={model?.trainingStatus ?? "UNKNOWN"} />
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  MAE
                </Typography>
                <Typography variant="body1">{model?.mae ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  RMSE
                </Typography>
                <Typography variant="body1">{model?.rmse ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  R²
                </Typography>
                <Typography variant="body1">{model?.r2 ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography color="text.secondary" variant="caption">
                  Samples
                </Typography>
                <Typography variant="body1">
                  {model?.trainingSampleCount ?? "-"} train / {model?.validationSampleCount ?? "-"} validation
                </Typography>
              </Grid>
            </Grid>

            <Box sx={{ borderTop: "1px solid", borderColor: "divider", pt: 2 }}>
              <Typography color="text.secondary" variant="caption">
                Forecast batch
              </Typography>
              <Typography fontWeight={800} variant="body1">
                {forecastRun?.runStatus ?? "-"} · {forecastRun?.forecastCount ?? "-"} rows
              </Typography>
            </Box>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 8 }} sx={{ animation: "fadeUp 800ms ease both" }}>
          <SectionCard subtitle="Rows ranked for practical operations review." title="Risk and Replenishment Grid">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Node</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>Available</TableCell>
                  <TableCell>Demand Signal</TableCell>
                  <TableCell>Risk</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {forecasts.map((forecast) => {
                  const width = Math.min(100, Math.max(6, forecast.predicted24hDemand * 2));
                  return (
                    <TableRow key={forecast.forecastId}>
                      <TableCell>{forecast.nodeId}</TableCell>
                      <TableCell>
                        <Typography fontWeight={800} variant="body2">
                          {forecast.sku}
                        </Typography>
                        <Typography color="text.secondary" variant="caption">
                          Days cover {forecast.daysOfCover}
                        </Typography>
                      </TableCell>
                      <TableCell>{forecast.availableQuantity}</TableCell>
                      <TableCell>
                        <Box
                          sx={{
                            width: 120,
                            height: 8,
                            borderRadius: 999,
                            bgcolor: "rgba(15,94,168,0.08)",
                            overflow: "hidden",
                            mb: 0.6,
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
                        <Typography color="text.secondary" variant="caption">
                          15m {forecast.predicted15mDemand} · 24h {forecast.predicted24hDemand}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <StatusChip value={forecast.stockoutRisk} />
                      </TableCell>
                      <TableCell sx={{ color: riskColor(forecast.stockoutRisk) === "error" ? "error.main" : "text.primary", fontWeight: 800 }}>
                        +{forecast.recommendedReplenishmentQuantity}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </SectionCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
