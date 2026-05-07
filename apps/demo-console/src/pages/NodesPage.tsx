import { useQuery } from "@tanstack/react-query";
import { Alert, Box, Grid, LinearProgress, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { api } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatCard } from "../components/StatCard";
import { StatusChip } from "../components/StatusChip";

export function NodesPage({ token }: { token: string }) {
  const nodesQuery = useQuery({
    queryKey: ["nodes"],
    queryFn: () => api.listNodes(token),
    refetchInterval: 60000,
  });

  if (nodesQuery.isError) {
    return <Alert severity="error">Unable to load node registry data.</Alert>;
  }

  const nodes = nodesQuery.data ?? [];
  const activeCount = nodes.filter((node) => node.active).length;
  const darkStoreCount = nodes.filter((node) => node.nodeType === "DARK_STORE").length;
  const expressCount = nodes.filter((node) => node.supportsExpress).length;

  return (
    <>
      <PageHeader
        badges={[
          { label: "Node Registry", color: "primary" },
          { label: "Network Topology", color: "secondary" },
        ]}
        description="Node reference layer for dark stores, fulfillment centers, and hubs used by orchestration, planning, and shipment reads."
        eyebrow="Reference Layer"
        title="Fulfillment Node Topology"
      />

      {nodesQuery.isLoading ? <LinearProgress sx={{ mt: 3 }} /> : null}

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard label="Registered Nodes" value={nodes.length} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard label="Active Nodes" value={activeCount} />
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <StatCard helper={`${expressCount} express-capable`} label="Dark Stores" value={darkStoreCount} />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12, lg: 4 }}>
          <SectionCard subtitle="Quick composition view from the node catalog." title="Network Mix">
            <Stack spacing={1.25}>
              {[
                { label: "Dark stores", value: nodes.filter((node) => node.nodeType === "DARK_STORE").length },
                { label: "Fulfillment centers", value: nodes.filter((node) => node.nodeType === "FULFILLMENT_CENTER").length },
                { label: "Hubs", value: nodes.filter((node) => node.nodeType === "HUB").length },
                { label: "Parcel capable", value: nodes.filter((node) => node.supportsParcel).length },
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

        <Grid size={{ xs: 12, lg: 8 }}>
          <SectionCard subtitle="Node inventory for planning, orchestration, and shipment routing context." title="Node Directory">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Node ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>City</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Capabilities</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {nodes.map((node) => (
                  <TableRow key={node.nodeId}>
                    <TableCell>{node.nodeId}</TableCell>
                    <TableCell>
                      <Typography fontWeight={600} variant="body2">
                        {node.displayName}
                      </Typography>
                      <Typography color="text.secondary" variant="caption">
                        {node.nodeCode}
                      </Typography>
                    </TableCell>
                    <TableCell>{node.nodeType}</TableCell>
                    <TableCell>
                      {node.city}, {node.state}
                    </TableCell>
                    <TableCell>{node.priority}</TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {node.supportsExpress ? "Express" : "Standard"} · {node.supportsParcel ? "Parcel" : "No parcel"}
                      </Typography>
                      <Typography color="text.secondary" variant="caption">
                        {node.timezone}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <StatusChip value={node.active ? "ACTIVE" : "INACTIVE"} />
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
