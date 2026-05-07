import { useQuery } from "@tanstack/react-query";
import { Alert, Grid, LinearProgress, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { api, formatDateTime } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";

export function ReturnsPage({ token }: { token: string }) {
  const returnsQuery = useQuery({
    queryKey: ["returns"],
    queryFn: () => api.listReturns(token, "customer-001"),
    refetchInterval: 60000,
  });

  if (returnsQuery.isError) {
    return <Alert severity="error">Unable to load returns data.</Alert>;
  }

  const returns = returnsQuery.data ?? [];
  const receivedCount = returns.filter((entry) => entry.status === "RECEIVED").length;

  return (
    <>
      <PageHeader
        badges={[
          { label: "Reverse Logistics", color: "primary" },
          { label: "Customer 001", color: "secondary" },
        ]}
        description="This view is intentionally minimal. It proves the reverse-logistics lifecycle exists in the backend and can be surfaced next to the primary order and shipment flows."
        eyebrow="Returns"
        title="Return Request Lifecycle"
      />

      {returnsQuery.isLoading ? <LinearProgress sx={{ mt: 3 }} /> : null}

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard label="Return Requests" value={returns.length} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard label="Received" value={receivedCount} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard
            helper={returns[0] ? formatDateTime(returns[0].requestedAt) : "N/A"}
            label="Latest Request"
            value={returns[0]?.reasonCode ?? "-"}
          />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12 }}>
          <SectionCard subtitle="Backend-backed reverse logistics states for the demo customer." title="Return Requests">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Return ID</TableCell>
                  <TableCell>Order Intent</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Reason</TableCell>
                  <TableCell>Node</TableCell>
                  <TableCell>Updated</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {returns.map((entry) => (
                  <TableRow key={entry.returnRequestId}>
                    <TableCell>{entry.returnRequestId}</TableCell>
                    <TableCell>{entry.orderIntentId}</TableCell>
                    <TableCell>
                      <StatusChip value={entry.status} />
                    </TableCell>
                    <TableCell>
                      <Typography fontWeight={600} variant="body2">
                        {entry.reasonCode}
                      </Typography>
                      <Typography color="text.secondary" variant="caption">
                        {entry.reasonDetail ?? "No extra detail"}
                      </Typography>
                    </TableCell>
                    <TableCell>{entry.nodeId}</TableCell>
                    <TableCell>{formatDateTime(entry.updatedAt)}</TableCell>
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
