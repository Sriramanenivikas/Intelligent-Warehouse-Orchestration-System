import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Button,
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
import { api, formatDateTime } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";

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

  const snapshot = snapshotQuery.data;

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => refreshMutation.mutate()} variant="contained">
            {refreshMutation.isPending ? "Refreshing..." : "Refresh Snapshot"}
          </Button>
        }
        badges={[
          { label: "Control Tower", color: "primary" },
          { label: "Execution State", color: "secondary" },
          { label: "Exceptions", color: "warning" },
        ]}
        description="Cross-service read model for backlog, planning alerts, shipment anomalies, and signal volume across the running cell."
        eyebrow="Ops Center"
        title="Cell Operations Snapshot"
      />

      {snapshotQuery.isLoading || refreshMutation.isPending ? <LinearProgress sx={{ mt: 3 }} /> : null}

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard helper="Planned SKU-node combinations" label="Forecast Rows" value={snapshot?.forecastKpi.totalForecasts ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper="Immediate action candidates"
            label="Critical + High"
            value={(snapshot?.forecastKpi.criticalCount ?? 0) + (snapshot?.forecastKpi.highCount ?? 0)}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard helper="Latest scan and network anomalies" label="Recent Exceptions" value={snapshot?.recentExceptions.length ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard helper={formatDateTime(snapshot?.generatedAt)} label="Model Version" value={snapshot?.modelVersion ?? "-"} />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, xl: 4 }}>
          <SectionCard subtitle="Status volumes pulled from the latest snapshot." title="Backlog and Signal Mix">
            <Stack spacing={1.25}>
              {[
                { label: "Order intents", value: (snapshot?.orderIntentsByStatus ?? []).reduce((sum, item) => sum + item.count, 0) },
                { label: "Fulfillment orders", value: (snapshot?.fulfillmentOrdersByStatus ?? []).reduce((sum, item) => sum + item.count, 0) },
                { label: "Shipments", value: (snapshot?.shipmentsByStatus ?? []).reduce((sum, item) => sum + item.count, 0) },
                { label: "Network shipments", value: (snapshot?.networkShipmentsByStatus ?? []).reduce((sum, item) => sum + item.count, 0) },
                { label: "Scan events", value: (snapshot?.scanEventsByType ?? []).reduce((sum, item) => sum + item.count, 0) },
                { label: "Notifications", value: (snapshot?.notificationsByStatus ?? []).reduce((sum, item) => sum + item.count, 0) },
              ].map((row) => (
                <Box key={row.label} sx={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 1 }}>
                  <Typography color="text.secondary" variant="body2">
                    {row.label}
                  </Typography>
                  <Typography fontWeight={700} variant="body2">
                    {row.value}
                  </Typography>
                </Box>
              ))}
            </Stack>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 8 }}>
          <SectionCard subtitle="Highest-priority inventory planning alerts from the control-tower snapshot." title="Top Forecast Alerts">
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
                {(snapshot?.topForecasts ?? []).map((forecast) => (
                  <TableRow key={forecast.forecastId}>
                    <TableCell>{forecast.nodeId}</TableCell>
                    <TableCell>{forecast.sku}</TableCell>
                    <TableCell>
                      <StatusChip value={forecast.stockoutRisk} />
                    </TableCell>
                    <TableCell>{forecast.predicted15mDemand}</TableCell>
                    <TableCell>{forecast.recommendedReplenishmentQuantity}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SectionCard>
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, xl: 6 }}>
          <SectionCard subtitle="Latest shipment anomalies surfaced from scan-event and network state." title="Recent Scan Exceptions">
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
                {(snapshot?.recentExceptions ?? []).map((exception) => (
                  <TableRow key={`${exception.shipmentId}-${exception.occurredAt}`}>
                    <TableCell>{exception.awbNumber}</TableCell>
                    <TableCell>
                      <StatusChip value={exception.currentStatus} />
                    </TableCell>
                    <TableCell>{exception.lastScanType}</TableCell>
                    <TableCell>{formatDateTime(exception.occurredAt)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 6 }}>
          <SectionCard subtitle="Fulfillment and parcel state distribution across the running cell." title="Execution State">
            <Stack spacing={1.25}>
              {(snapshot?.fulfillmentOrdersByStatus ?? []).map((item) => (
                <Box key={`fo-${item.key}`} sx={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 1 }}>
                  <Typography variant="body2">{item.key}</Typography>
                  <Typography fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Box>
              ))}
              {(snapshot?.shipmentsByStatus ?? []).map((item) => (
                <Box key={`ship-${item.key}`} sx={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 1 }}>
                  <Typography color="text.secondary" variant="body2">
                    Shipment {item.key}
                  </Typography>
                  <Typography fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Box>
              ))}
              {(snapshot?.networkShipmentsByStatus ?? []).map((item) => (
                <Box key={`net-${item.key}`} sx={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 1 }}>
                  <Typography color="text.secondary" variant="body2">
                    Network {item.key}
                  </Typography>
                  <Typography fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Box>
              ))}
            </Stack>
          </SectionCard>
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <SectionCard subtitle="Current order-intent backlog from the latest snapshot." title="Order Intake">
            {(snapshot?.orderIntentsByStatus ?? []).map((item) => (
              <Grid alignItems="center" container key={item.key}>
                <Grid size={{ xs: 8 }}>
                  <Typography variant="body2">{item.key}</Typography>
                </Grid>
                <Grid size={{ xs: 4 }}>
                  <Typography align="right" fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Grid>
              </Grid>
            ))}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <SectionCard subtitle="Scan event mix from the latest snapshot." title="Signals">
            {(snapshot?.scanEventsByType ?? []).map((item) => (
              <Grid alignItems="center" container key={`scan-${item.key}`}>
                <Grid size={{ xs: 8 }}>
                  <Typography variant="body2">{item.key}</Typography>
                </Grid>
                <Grid size={{ xs: 4 }}>
                  <Typography align="right" fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Grid>
              </Grid>
            ))}
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <SectionCard subtitle="Notification state and audience split from the latest snapshot." title="Notifications">
            {(snapshot?.notificationsByStatus ?? []).map((item) => (
              <Grid alignItems="center" container key={`notif-${item.key}`}>
                <Grid size={{ xs: 8 }}>
                  <Typography variant="body2">Status {item.key}</Typography>
                </Grid>
                <Grid size={{ xs: 4 }}>
                  <Typography align="right" fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Grid>
              </Grid>
            ))}
            {(snapshot?.notificationsByAudience ?? []).map((item) => (
              <Grid alignItems="center" container key={`aud-${item.key}`}>
                <Grid size={{ xs: 8 }}>
                  <Typography color="text.secondary" variant="body2">
                    Audience {item.key}
                  </Typography>
                </Grid>
                <Grid size={{ xs: 4 }}>
                  <Typography align="right" fontWeight={700} variant="body2">
                    {item.count}
                  </Typography>
                </Grid>
              </Grid>
            ))}
          </SectionCard>
        </Grid>
      </Grid>
    </>
  );
}
