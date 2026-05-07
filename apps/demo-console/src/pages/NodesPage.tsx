import { useQuery } from "@tanstack/react-query";
import { Alert, Grid, LinearProgress, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
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

  return (
    <>
      <PageHeader
        badges={[
          { label: "Node Registry", color: "primary" },
          { label: "Fulfillment Network", color: "secondary" },
        ]}
        description="Reference view of the dark stores, fulfillment centers, and hubs available to the orchestration and planning layers."
        eyebrow="Reference Data"
        title="Fulfillment Node Catalog"
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
          <StatCard label="Dark Stores" value={darkStoreCount} />
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mt: 0.5 }}>
        <Grid size={{ xs: 12 }}>
          <SectionCard subtitle="Node inventory for planning, orchestration, and shipment routing context." title="Node Directory">
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Node ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>City</TableCell>
                  <TableCell>Timezone</TableCell>
                  <TableCell>Capacity / hr</TableCell>
                  <TableCell>Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {nodes.map((node) => (
                  <TableRow key={node.nodeId}>
                    <TableCell>{node.nodeId}</TableCell>
                    <TableCell>
                      <Typography fontWeight={600} variant="body2">
                        {node.nodeName}
                      </Typography>
                      <Typography color="text.secondary" variant="caption">
                        {node.nodeCode}
                      </Typography>
                    </TableCell>
                    <TableCell>{node.nodeType}</TableCell>
                    <TableCell>
                      {node.city}, {node.state}
                    </TableCell>
                    <TableCell>{node.timezone}</TableCell>
                    <TableCell>{node.capacityUnitsPerHour ?? "-"}</TableCell>
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
