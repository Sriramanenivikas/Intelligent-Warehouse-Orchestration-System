import { useQuery } from "@tanstack/react-query";
import {
  Alert,
  Card,
  CardContent,
  Chip,
  Grid,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { api, formatDateTime, riskColor } from "../api";
import { StatCard } from "../components/StatCard";

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
  const forecastQuery = useQuery({
    queryKey: ["forecast-list"],
    queryFn: () => api.listForecasts(token),
    refetchInterval: 30000,
  });

  if (summaryQuery.isError || modelRunQuery.isError || forecastQuery.isError) {
    return <Alert severity="error">Unable to load forecasting data.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Stack spacing={0.5}>
        <Typography variant="h4">Forecasting</Typography>
        <Typography color="text.secondary">
          ML-backed demand, replenishment, and stockout-risk outputs refreshed on the latest planning run.
        </Typography>
      </Stack>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="Critical Risk" value={summaryQuery.data?.criticalCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="High Risk" value={summaryQuery.data?.highCount ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="Model Version" value={modelRunQuery.data?.registeredModelVersion ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard
            helper={formatDateTime(modelRunQuery.data?.trainingCompletedAt)}
            label="Recommended Units"
            value={summaryQuery.data?.totalRecommendedReplenishmentQuantity ?? "-"}
          />
        </Grid>
      </Grid>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 4 }}>
          <Card>
            <CardContent>
              <Stack spacing={1.5}>
                <Typography variant="h6">Latest Model Run</Typography>
                <Typography variant="body2">Algorithm: {modelRunQuery.data?.algorithm ?? "-"}</Typography>
                <Typography variant="body2">MAE: {modelRunQuery.data?.mae ?? "-"}</Typography>
                <Typography variant="body2">RMSE: {modelRunQuery.data?.rmse ?? "-"}</Typography>
                <Typography variant="body2">R²: {modelRunQuery.data?.r2 ?? "-"}</Typography>
                <Typography variant="body2">
                  Samples: {modelRunQuery.data?.trainingSampleCount ?? "-"} / {modelRunQuery.data?.validationSampleCount ?? "-"}
                </Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card>
            <CardContent>
              <Typography gutterBottom variant="h6">
                Forecast Grid
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Node</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell>Avail.</TableCell>
                    <TableCell>15m</TableCell>
                    <TableCell>24h</TableCell>
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
                        <Chip color={riskColor(forecast.stockoutRisk)} label={forecast.stockoutRisk} size="small" />
                      </TableCell>
                      <TableCell>{forecast.recommendedReplenishmentQuantity}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Stack>
  );
}

