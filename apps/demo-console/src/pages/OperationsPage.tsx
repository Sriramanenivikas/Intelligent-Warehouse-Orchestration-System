import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Grid,
  LinearProgress,
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

  return (
    <>
      <PageHeader
        actions={
          <Button onClick={() => refreshMutation.mutate()} variant="contained">
            {refreshMutation.isPending ? "Refreshing..." : "Refresh Snapshot"}
          </Button>
        }
        badges={[
          { label: "Fulfillment", color: "primary" },
          { label: "Shipment Network", color: "secondary" },
          { label: "Exceptions", color: "warning" },
        ]}
        description="This is the cross-service operational read model. It aggregates the latest control tower snapshot for planning KPIs, shipment states, scan events, and exception visibility."
        eyebrow="Control Tower"
        title="Operations Snapshot"
      />

      {snapshotQuery.isLoading || refreshMutation.isPending ? <LinearProgress sx={{ mt: 3 }} /> : null}

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard label="Forecast Rows" value={snapshotQuery.data?.forecastKpi.totalForecasts ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            label="Critical + High"
            value={(snapshotQuery.data?.forecastKpi.criticalCount ?? 0) + (snapshotQuery.data?.forecastKpi.highCount ?? 0)}
          />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard label="Recent Exceptions" value={snapshotQuery.data?.recentExceptions.length ?? "-"} />
        </Grid>
        <Grid size={{ xs: 12, md: 6, xl: 3 }}>
          <StatCard
            helper={formatDateTime(snapshotQuery.data?.generatedAt)}
            label="Model Version"
            value={snapshotQuery.data?.modelVersion ?? "-"}
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, xl: 6 }}>
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
                {(snapshotQuery.data?.topForecasts ?? []).map((forecast) => (
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
                {(snapshotQuery.data?.recentExceptions ?? []).map((exception) => (
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
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <SectionCard subtitle="Current order-intent snapshot." title="Order Intake">
            {(snapshotQuery.data?.orderIntentsByStatus ?? []).map((item) => (
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
          <SectionCard subtitle="Current fulfillment and shipment status distribution." title="Execution State">
            {(snapshotQuery.data?.fulfillmentOrdersByStatus ?? []).map((item) => (
              <Grid alignItems="center" container key={`fo-${item.key}`}>
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
            {(snapshotQuery.data?.shipmentsByStatus ?? []).map((item) => (
              <Grid alignItems="center" container key={`ship-${item.key}`}>
                <Grid size={{ xs: 8 }}>
                  <Typography color="text.secondary" variant="body2">
                    Shipment {item.key}
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
        <Grid size={{ xs: 12, md: 4 }}>
          <SectionCard subtitle="Notification and scan volume from the snapshot." title="Signals">
            {(snapshotQuery.data?.scanEventsByType ?? []).map((item) => (
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
            {(snapshotQuery.data?.notificationsByStatus ?? []).map((item) => (
              <Grid alignItems="center" container key={`notif-${item.key}`}>
                <Grid size={{ xs: 8 }}>
                  <Typography color="text.secondary" variant="body2">
                    Notification {item.key}
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
