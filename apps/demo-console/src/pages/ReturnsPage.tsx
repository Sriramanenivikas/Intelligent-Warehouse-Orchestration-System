import { useQuery } from "@tanstack/react-query";
import { Alert, Card, CardContent, Chip, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { api, formatDateTime } from "../api";

export function ReturnsPage({ token }: { token: string }) {
  const returnsQuery = useQuery({
    queryKey: ["returns"],
    queryFn: () => api.listReturns(token, "customer-001"),
    refetchInterval: 60000,
  });

  if (returnsQuery.isError) {
    return <Alert severity="error">Unable to load returns data.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Stack spacing={0.5}>
        <Typography variant="h4">Returns</Typography>
        <Typography color="text.secondary">Reverse logistics lifecycle for demo customer return requests.</Typography>
      </Stack>
      <Card>
        <CardContent>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Return ID</TableCell>
                <TableCell>Order Intent</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Reason</TableCell>
                <TableCell>Channel</TableCell>
                <TableCell>Updated</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(returnsQuery.data ?? []).map((entry) => (
                <TableRow key={entry.returnRequestId}>
                  <TableCell>{entry.returnRequestId}</TableCell>
                  <TableCell>{entry.orderIntentId}</TableCell>
                  <TableCell>
                    <Chip label={entry.status} size="small" />
                  </TableCell>
                  <TableCell>{entry.reasonCode}</TableCell>
                  <TableCell>{entry.channel}</TableCell>
                  <TableCell>{formatDateTime(entry.updatedAt)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </Stack>
  );
}

