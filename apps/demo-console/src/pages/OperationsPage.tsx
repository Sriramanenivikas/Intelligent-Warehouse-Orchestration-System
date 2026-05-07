import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Card,
  CardContent,
  Grid,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { api, formatDateTime } from "../api";
import { StatCard } from "../components/StatCard";

export function OperationsPage({ token }: { token: string }) {
  const snapshotQuery = useQuery({
    queryKey: ["control-tower"],
    queryFn: () => api.getControlTowerLatest(token),
    refetchInterval: 30000,
  });
  const refreshMutation = useMutation({
    mutationFn: () => api.refreshControlTower(token),
    onSuccess: () => snapshotQuery.refetch(),
  });

  if (snapshotQuery.isError) {
    return <Alert severity="error">Unable to load control tower snapshot.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: "column", md: "row" }} justifyContent="space-between" spacing={2}>
        <Stack spacing={0.5}>
          <Typography variant="h4">Operations</Typography>
          <Typography color="text.secondary">
            Aggregated operational snapshot for fulfillment, shipment, scan, notification, and planning domains.
          </Typography>
        </Stack>
        <Button onClick={() => refreshMutation.mutate()} variant="contained">
          {refreshMutation.isPending ? "Refreshing..." : "Refresh Snapshot"}
        </Button>
      </Stack>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="Forecast Rows" value={snapshotQuery.data?.forecastKpi.totalForecasts ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="Critical + High" value={(snapshotQuery.data?.forecastKpi.criticalCount ?? 0) + (snapshotQuery.data?.forecastKpi.highCount ?? 0)} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard label="Recent Exceptions" value={snapshotQuery.data?.recentExceptions.length ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 3 }}>
          <StatCard helper={formatDateTime(snapshotQuery.data?.generatedAt)} label="Snapshot Version" value={snapshotQuery.data?.modelVersion ?? "-"} />
        </Grid>
      </Grid>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardContent>
              <Typography gutterBottom variant="h6">
                Top Forecast Alerts
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Node</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell>Risk</TableCell>
                    <TableCell>15m Demand</TableCell>
                    <TableCell>Replenish</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(snapshotQuery.data?.topForecasts ?? []).map((forecast) => (
                    <TableRow key={forecast.forecastId}>
                      <TableCell>{forecast.nodeId}</TableCell>
                      <TableCell>{forecast.sku}</TableCell>
                      <TableCell>{forecast.stockoutRisk}</TableCell>
                      <TableCell>{forecast.predicted15mDemand}</TableCell>
                      <TableCell>{forecast.recommendedReplenishmentQuantity}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 6 }}>
          <Card>
            <CardContent>
              <Typography gutterBottom variant="h6">
                Recent Scan Exceptions
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>AWB</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Last Scan</TableCell>
                    <TableCell>Occurred</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(snapshotQuery.data?.recentExceptions ?? []).map((exception) => (
                    <TableRow key={`${exception.shipmentId}-${exception.occurredAt}`}>
                      <TableCell>{exception.awbNumber}</TableCell>
                      <TableCell>{exception.currentStatus}</TableCell>
                      <TableCell>{exception.lastScanType}</TableCell>
                      <TableCell>{formatDateTime(exception.occurredAt)}</TableCell>
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

