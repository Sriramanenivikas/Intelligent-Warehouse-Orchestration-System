import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { api, formatDateTime } from "../api";

const defaultOrder = {
  customerId: "customer-001",
  channel: "APP",
  paymentMode: "PREPAID",
  currency: "INR",
  totalAmount: "899.00",
  sku: "SKU-APPLE-1KG",
  quantity: "2",
  city: "Bengaluru",
  state: "Karnataka",
  postalCode: "560001",
};

export function OrderFlowPage({ token }: { token: string }) {
  const [form, setForm] = useState(defaultOrder);
  const [orderIntentId, setOrderIntentId] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const idempotencyKey = useMemo(() => `iwos-demo-${Date.now()}`, [orderIntentId]);

  const createOrder = useMutation({
    mutationFn: async () =>
      api.createOrderIntent({
        token,
        idempotencyKey,
        payload: {
          customerId: form.customerId,
          channel: form.channel,
          paymentMode: form.paymentMode,
          currency: form.currency,
          totalAmount: Number(form.totalAmount),
          deliveryAddress: {
            name: "Demo Customer",
            line1: "12 Demo Street",
            line2: "Fulfillment Block",
            city: form.city,
            state: form.state,
            postalCode: form.postalCode,
            country: "IN",
            phone: "9999999999",
          },
          items: [{ sku: form.sku, quantity: Number(form.quantity) }],
        },
      }),
    onSuccess: (response) => {
      setOrderIntentId(response.orderIntentId);
      setMessage(`Accepted order intent ${response.orderIntentId}`);
    },
  });

  const processFulfillment = useMutation({
    mutationFn: async () => {
      if (!orderIntentId) {
        throw new Error("Create an order first");
      }
      return api.processFulfillment(orderIntentId, token);
    },
    onSuccess: (response) => {
      setMessage(`Created fulfillment workflow ${response.fulfillmentOrderId}`);
    },
  });

  const createShipment = useMutation({
    mutationFn: async () => {
      if (!fulfillmentQuery.data) {
        throw new Error("Fulfillment order not ready");
      }
      return api.createShipment(fulfillmentQuery.data.fulfillmentOrderId, token);
    },
    onSuccess: (response) => {
      setMessage(`Created shipment ${response.shipmentId}`);
    },
  });

  const recordScan = useMutation({
    mutationFn: async (scanType: string) => {
      if (!shipmentQuery.data) {
        throw new Error("Shipment not ready");
      }
      return api.recordScan(shipmentQuery.data.shipmentId, token, {
        scanType,
        nodeId: shipmentQuery.data.originNodeId,
        facilityCode: shipmentQuery.data.originNodeId,
        notes: `Demo ${scanType.toLowerCase()} scan`,
        occurredAt: new Date().toISOString(),
      });
    },
    onSuccess: (_, scanType) => {
      setMessage(`Recorded ${scanType} scan`);
    },
  });

  const orderQuery = useQuery({
    queryKey: ["order-intent", orderIntentId],
    queryFn: () => api.getOrderIntent(orderIntentId!, token),
    enabled: Boolean(orderIntentId),
    refetchInterval: 5000,
  });
  const fulfillmentQuery = useQuery({
    queryKey: ["fulfillment-by-order", orderIntentId],
    queryFn: () => api.getFulfillmentByOrderIntent(orderIntentId!, token),
    enabled: Boolean(orderIntentId),
    refetchInterval: 5000,
  });
  const shipmentQuery = useQuery({
    queryKey: ["shipment-by-order", orderIntentId],
    queryFn: () => api.getShipmentByOrderIntent(orderIntentId!, token),
    enabled: Boolean(orderIntentId),
    refetchInterval: 5000,
    retry: false,
  });
  const timelineQuery = useQuery({
    queryKey: ["timeline-by-order", orderIntentId],
    queryFn: () => api.getScanTimelineByOrderIntent(orderIntentId!, token),
    enabled: Boolean(orderIntentId),
    refetchInterval: 5000,
    retry: false,
  });
  const notificationsQuery = useQuery({
    queryKey: ["notifications-by-order", orderIntentId],
    queryFn: () => api.listNotificationsByOrderIntent(orderIntentId!, token),
    enabled: Boolean(orderIntentId),
    refetchInterval: 5000,
    retry: false,
  });

  return (
    <Stack spacing={3}>
      <Stack spacing={0.5}>
        <Typography variant="h4">Order Flow</Typography>
        <Typography color="text.secondary">
          Submit an order, drive fulfillment and shipment actions, and watch the downstream state update.
        </Typography>
      </Stack>
      {message ? <Alert severity="info">{message}</Alert> : null}
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, lg: 5 }}>
          <Card>
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h6">Create Demo Order</Typography>
                <TextField
                  label="Customer ID"
                  onChange={(event) => setForm((current) => ({ ...current, customerId: event.target.value }))}
                  value={form.customerId}
                />
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Channel"
                      onChange={(event) => setForm((current) => ({ ...current, channel: event.target.value }))}
                      select
                      value={form.channel}
                    >
                      <MenuItem value="APP">APP</MenuItem>
                      <MenuItem value="WEB">WEB</MenuItem>
                      <MenuItem value="PARTNER">PARTNER</MenuItem>
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="Payment Mode"
                      onChange={(event) => setForm((current) => ({ ...current, paymentMode: event.target.value }))}
                      select
                      value={form.paymentMode}
                    >
                      <MenuItem value="PREPAID">PREPAID</MenuItem>
                      <MenuItem value="COD">COD</MenuItem>
                    </TextField>
                  </Grid>
                </Grid>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 6 }}>
                    <TextField
                      fullWidth
                      label="SKU"
                      onChange={(event) => setForm((current) => ({ ...current, sku: event.target.value }))}
                      value={form.sku}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      fullWidth
                      label="Qty"
                      onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))}
                      value={form.quantity}
                    />
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField
                      fullWidth
                      label="Amount"
                      onChange={(event) => setForm((current) => ({ ...current, totalAmount: event.target.value }))}
                      value={form.totalAmount}
                    />
                  </Grid>
                </Grid>
                <Button onClick={() => createOrder.mutate()} variant="contained">
                  {createOrder.isPending ? "Submitting..." : "Submit Order Intent"}
                </Button>
                <Stack direction="row" spacing={1}>
                  <Button disabled={!orderIntentId} onClick={() => processFulfillment.mutate()} variant="outlined">
                    Process Fulfillment
                  </Button>
                  <Button disabled={!fulfillmentQuery.data} onClick={() => createShipment.mutate()} variant="outlined">
                    Create Shipment
                  </Button>
                </Stack>
                <Stack direction="row" flexWrap="wrap" spacing={1} useFlexGap>
                  <Button disabled={!shipmentQuery.data} onClick={() => recordScan.mutate("MANIFESTED")} size="small" variant="text">
                    Manifest Scan
                  </Button>
                  <Button disabled={!shipmentQuery.data} onClick={() => recordScan.mutate("HUB_RECEIVED")} size="small" variant="text">
                    Hub Received
                  </Button>
                  <Button disabled={!shipmentQuery.data} onClick={() => recordScan.mutate("OUT_FOR_DELIVERY")} size="small" variant="text">
                    Out For Delivery
                  </Button>
                  <Button disabled={!shipmentQuery.data} onClick={() => recordScan.mutate("DELIVERED")} size="small" variant="text">
                    Delivered
                  </Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, lg: 7 }}>
          <Card>
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h6">Order Lifecycle</Typography>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Stage</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Reference</TableCell>
                      <TableCell>Updated</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    <TableRow>
                      <TableCell>Order Intent</TableCell>
                      <TableCell>{orderQuery.data ? <Chip label="ACCEPTED" size="small" /> : "Pending"}</TableCell>
                      <TableCell>{orderQuery.data?.orderIntentId ?? "-"}</TableCell>
                      <TableCell>{formatDateTime(orderQuery.data?.acceptedAt)}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell>Fulfillment</TableCell>
                      <TableCell>{fulfillmentQuery.data ? <Chip color="primary" label={fulfillmentQuery.data.status} size="small" /> : "Pending"}</TableCell>
                      <TableCell>{fulfillmentQuery.data?.fulfillmentOrderId ?? "-"}</TableCell>
                      <TableCell>{formatDateTime(fulfillmentQuery.data?.updatedAt)}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell>Shipment</TableCell>
                      <TableCell>{shipmentQuery.data ? <Chip color="secondary" label={shipmentQuery.data.status} size="small" /> : "Pending"}</TableCell>
                      <TableCell>{shipmentQuery.data?.awbNumber ?? "-"}</TableCell>
                      <TableCell>{formatDateTime(shipmentQuery.data?.updatedAt)}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell>Timeline</TableCell>
                      <TableCell>{timelineQuery.data ? <Chip label={timelineQuery.data.currentStatus} size="small" /> : "Pending"}</TableCell>
                      <TableCell>{timelineQuery.data?.lastScanType ?? "-"}</TableCell>
                      <TableCell>{formatDateTime(timelineQuery.data?.updatedAt)}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
                <Box>
                  <Typography gutterBottom variant="subtitle2">
                    Notifications
                  </Typography>
                  <Stack spacing={1}>
                    {(notificationsQuery.data ?? []).slice(0, 4).map((notification) => (
                      <Alert key={notification.notificationId} severity="info">
                        {notification.title}: {notification.message}
                      </Alert>
                    ))}
                  </Stack>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Stack>
  );
}

