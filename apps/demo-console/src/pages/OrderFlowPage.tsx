import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Button,
  Grid,
  LinearProgress,
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
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatusChip } from "../components/StatusChip";

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

  const processWorkflow = useMutation({
    mutationFn: async () => {
      if (!orderIntentId) {
        throw new Error("Create an order first");
      }
      return api.processOrderWorkflow(orderIntentId, token);
    },
    onSuccess: (response) => {
      setMessage(`Workflow ${response.workflowId} moved to ${response.status}`);
      workflowQuery.refetch();
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
      setMessage(`Created fulfillment order ${response.fulfillmentOrderId}`);
      fulfillmentQuery.refetch();
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
      shipmentQuery.refetch();
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
      networkShipmentQuery.refetch();
      timelineQuery.refetch();
      notificationsQuery.refetch();
    },
  });

  const orderQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["order-intent", orderIntentId],
    queryFn: () => api.getOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const workflowQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["order-workflow", orderIntentId],
    queryFn: () => api.getOrderWorkflow(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const fulfillmentQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["fulfillment", orderIntentId],
    queryFn: () => api.getFulfillmentByOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const shipmentQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["shipment", orderIntentId],
    queryFn: () => api.getShipmentByOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const networkShipmentQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["network-shipment", orderIntentId],
    queryFn: () => api.getNetworkShipmentByOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const timelineQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["timeline", orderIntentId],
    queryFn: () => api.getScanTimelineByOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const notificationsQuery = useQuery({
    enabled: Boolean(orderIntentId),
    queryKey: ["notifications", orderIntentId],
    queryFn: () => api.listNotificationsByOrderIntent(orderIntentId!, token),
    refetchInterval: 5000,
  });

  const anyError =
    createOrder.isError ||
    processWorkflow.isError ||
    processFulfillment.isError ||
    createShipment.isError ||
    recordScan.isError;

  return (
    <Stack spacing={3}>
      <PageHeader
        badges={[
          { label: "Order Intake", color: "primary" },
          { label: "Warehouse", color: "secondary" },
          { label: "Shipment + Notifications", color: "success" },
        ]}
        description="Drive one realistic demo path: accept an order intent, process the order workflow, allocate fulfillment, create a shipment, and record network scans that feed timeline and notifications."
        eyebrow="Operational Demo"
        title="Order Flow Console"
      />

      {anyError ? <Alert severity="error">One of the flow actions failed. Check the latest stage and retry from the next valid action.</Alert> : null}
      {message ? <Alert severity="success">{message}</Alert> : null}
      {createOrder.isPending || processWorkflow.isPending || processFulfillment.isPending || createShipment.isPending || recordScan.isPending ? (
        <LinearProgress />
      ) : null}

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 4 }}>
          <SectionCard subtitle="Use one compact order form to seed the entire orchestration flow." title="Create Demo Order">
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}>
                <TextField
                  fullWidth
                  label="Customer ID"
                  onChange={(event) => setForm((current) => ({ ...current, customerId: event.target.value }))}
                  value={form.customerId}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  label="SKU"
                  onChange={(event) => setForm((current) => ({ ...current, sku: event.target.value }))}
                  value={form.sku}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  label="Quantity"
                  onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))}
                  value={form.quantity}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  label="Total Amount"
                  onChange={(event) => setForm((current) => ({ ...current, totalAmount: event.target.value }))}
                  value={form.totalAmount}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  fullWidth
                  label="City"
                  onChange={(event) => setForm((current) => ({ ...current, city: event.target.value }))}
                  value={form.city}
                />
              </Grid>
            </Grid>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
              <Button fullWidth onClick={() => createOrder.mutate()} variant="contained">
                {createOrder.isPending ? "Submitting..." : "Submit Order Intent"}
              </Button>
              <Button
                disabled={!orderIntentId}
                fullWidth
                onClick={() => processWorkflow.mutate()}
                variant="outlined"
              >
                {processWorkflow.isPending ? "Processing..." : "Process Workflow"}
              </Button>
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
              <Button
                disabled={!orderIntentId}
                fullWidth
                onClick={() => processFulfillment.mutate()}
                variant="contained"
              >
                {processFulfillment.isPending ? "Allocating..." : "Create Fulfillment"}
              </Button>
              <Button
                disabled={!fulfillmentQuery.data}
                fullWidth
                onClick={() => createShipment.mutate()}
                variant="outlined"
              >
                {createShipment.isPending ? "Creating..." : "Create Shipment"}
              </Button>
            </Stack>

            <Stack direction={{ xs: "column", md: "row" }} spacing={1.5}>
              {["MANIFESTED", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"].map((scanType) => (
                <Button
                  disabled={!shipmentQuery.data}
                  key={scanType}
                  onClick={() => recordScan.mutate(scanType)}
                  variant="text"
                >
                  {scanType}
                </Button>
              ))}
            </Stack>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 8 }}>
          <SectionCard subtitle="Every stage shown here reflects real backend state from the running services." title="Lifecycle Timeline">
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
                  <TableCell>{orderQuery.data ? <StatusChip value="ACCEPTED" /> : "Pending"}</TableCell>
                  <TableCell>{orderQuery.data?.orderIntentId ?? "-"}</TableCell>
                  <TableCell>{formatDateTime(orderQuery.data?.acceptedAt)}</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>Order Workflow</TableCell>
                  <TableCell>{workflowQuery.data ? <StatusChip value={workflowQuery.data.status} /> : "Pending"}</TableCell>
                  <TableCell>{workflowQuery.data?.workflowId ?? "-"}</TableCell>
                  <TableCell>{formatDateTime(workflowQuery.data?.updatedAt)}</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>Fulfillment</TableCell>
                  <TableCell>{fulfillmentQuery.data ? <StatusChip value={fulfillmentQuery.data.status} /> : "Pending"}</TableCell>
                  <TableCell>{fulfillmentQuery.data?.fulfillmentOrderId ?? "-"}</TableCell>
                  <TableCell>{formatDateTime(fulfillmentQuery.data?.updatedAt)}</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>Shipment</TableCell>
                  <TableCell>{shipmentQuery.data ? <StatusChip value={shipmentQuery.data.status} /> : "Pending"}</TableCell>
                  <TableCell>{shipmentQuery.data?.awbNumber ?? "-"}</TableCell>
                  <TableCell>{formatDateTime(shipmentQuery.data?.updatedAt)}</TableCell>
                </TableRow>
                <TableRow>
                  <TableCell>Network Timeline</TableCell>
                  <TableCell>{timelineQuery.data ? <StatusChip value={timelineQuery.data.currentStatus} /> : "Pending"}</TableCell>
                  <TableCell>{timelineQuery.data?.lastScanType ?? "-"}</TableCell>
                  <TableCell>{formatDateTime(timelineQuery.data?.updatedAt)}</TableCell>
                </TableRow>
              </TableBody>
            </Table>

            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography fontWeight={600} variant="subtitle2">
                  Workflow details
                </Typography>
                <Typography color="text.secondary" variant="body2">
                  Node {workflowQuery.data?.fulfillmentNodeId ?? "-"} · Customer {workflowQuery.data?.customerId ?? "-"}
                </Typography>
                <Typography color="text.secondary" variant="body2">
                  Payment {workflowQuery.data?.payment?.status ?? "-"} · Reservations {workflowQuery.data?.reservations?.length ?? 0}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography fontWeight={600} variant="subtitle2">
                  Notification feed
                </Typography>
                <Stack spacing={1} sx={{ mt: 1 }}>
                  {(notificationsQuery.data ?? []).slice(0, 4).map((notification) => (
                    <Alert key={notification.notificationId} severity="info">
                      {notification.title}: {notification.message}
                    </Alert>
                  ))}
                </Stack>
              </Grid>
            </Grid>
          </SectionCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
