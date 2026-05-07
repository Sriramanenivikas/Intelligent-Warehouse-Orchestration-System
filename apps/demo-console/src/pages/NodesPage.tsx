import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Chip, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { api } from "../api";

export function NodesPage({ token }: { token: string }) {
  const nodesQuery = useQuery({
    queryKey: ["nodes"],
    queryFn: () => api.listNodes(token),
    refetchInterval: 60000,
  });

  if (nodesQuery.isError) {
    return <Alert severity="error">Unable to load node registry data.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Stack spacing={0.5}>
        <Typography variant="h4">Nodes</Typography>
        <Typography color="text.secondary">Fulfillment node catalog used by dark stores, FCs, and hubs.</Typography>
      </Stack>
      <Card>
        <CardContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Node ID</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>City</TableCell>
                <TableCell>Capacity</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(nodesQuery.data ?? []).map((node) => (
                <TableRow key={node.nodeId}>
                  <TableCell>{node.nodeId}</TableCell>
                  <TableCell>{node.nodeName}</TableCell>
                  <TableCell>{node.nodeType}</TableCell>
                  <TableCell>{node.city}</TableCell>
                  <TableCell>{node.capacityUnitsPerHour ?? "-"}</TableCell>
                  <TableCell>
                    <Chip color={node.active ? "success" : "default"} label={node.active ? "ACTIVE" : "INACTIVE"} size="small" />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Stack>
  );
}

