import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Grid,
  LinearProgress,
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
    return <Alert severity="error">Unable to load forecasting data.</Alert>;
  }

  return (
    <>
      <PageHeader
        badges={[
          { label: "MLflow Tracked", color: "primary" },
          { label: "15-Min Refresh", color: "secondary" },
          { label: "Node + SKU Planning", color: "success" },
        ]}
        description="This service turns reservation and inventory history into node-level demand, stockout-risk, and replenishment recommendations. The dashboard reads the latest model run and forecast batch directly from the planning backend."
        eyebrow="AI Planning"
        title="Forecasting and Replenishment"
      />

      {summaryQuery.isLoading || modelRunQuery.isLoading || forecastQuery.isLoading || forecastRunQuery.isLoading ? (
        <LinearProgress sx={{ mt: 3 }} />
      ) : null}

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard label="Critical Risk" value={summaryQuery.data?.criticalCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard label="High Risk" value={summaryQuery.data?.highCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard label="Model Version" value={modelRunQuery.data?.registeredModelVersion ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={formatDateTime(forecastRunQuery.data?.completedAt)}
            label="Replenishment Units"
            value={summaryQuery.data?.totalRecommendedReplenishmentQuantity ?? "-"}
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, xl: 4 }}>
          <SectionCard subtitle="Metadata from the champion model run exposed by the planning service." title="Model Run">
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Algorithm
                </Typography>
                <Typography variant="body1">{modelRunQuery.data?.algorithm ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Status
                </Typography>
                <StatusChip value={modelRunQuery.data?.trainingStatus ?? "UNKNOWN"} />
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  MAE
                </Typography>
                <Typography variant="body1">{modelRunQuery.data?.mae ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  RMSE
                </Typography>
                <Typography variant="body1">{modelRunQuery.data?.rmse ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 4 }}>
                <Typography color="text.secondary" variant="caption">
                  R²
                </Typography>
                <Typography variant="body1">{modelRunQuery.data?.r2 ?? "-"}</Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography color="text.secondary" variant="caption">
                  Samples
                </Typography>
                <Typography variant="body1">
                  {modelRunQuery.data?.trainingSampleCount ?? "-"} train / {modelRunQuery.data?.validationSampleCount ?? "-"} validation
                </Typography>
              </Grid>
            </Grid>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 8 }}>
          <SectionCard subtitle="Top forecast rows the operations team should act on first." title="Forecast Grid">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Node</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>Available</TableCell>
                  <TableCell>15m Demand</TableCell>
                  <TableCell>24h Demand</TableCell>
                  <TableCell>Days Cover</TableCell>
                  <TableCell>Risk</TableCell>
                  <TableCell>Replenish</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {(forecastQuery.data ?? []).map((forecast) => (
                  <TableRow key={forecast.forecastId}>
                    <TableCell>{forecast.nodeId}</TableCell>
                    <TableCell>{forecast.sku}</TableCell>
                    <TableCell>{forecast.availableQuantity}</TableCell>
                    <TableCell>{forecast.predicted15mDemand}</TableCell>
                    <TableCell>{forecast.predicted24hDemand}</TableCell>
                    <TableCell>{forecast.daysOfCover}</TableCell>
                    <TableCell>
                      <StatusChip value={forecast.stockoutRisk} />
                    </TableCell>
                    <TableCell sx={{ color: riskColor(forecast.stockoutRisk) === "error" ? "error.main" : "text.primary", fontWeight: 600 }}>
                      {forecast.recommendedReplenishmentQuantity}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SectionCard>
        </Grid>
      </Grid>
    </>
  );
}
