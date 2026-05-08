import { useMemo, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Button,
  Grid,
  LinearProgress,
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
import { ApiError, api, formatDateTime } from "../api";
import { PageHeader } from "../components/PageHeader";
import { SectionCard } from "../components/SectionCard";
import { StatusChip } from "../components/StatusChip";

const demoSkuOptions = [
  { sku: "SKU-TEST-FLOW-01", label: "Test Flow SKU", note: "High demo stock" },
  { sku: "SKU-MANGO-1KG", label: "Mango 1KG", note: "Good available stock" },
  { sku: "SKU-GRAPE-500G", label: "Grape 500G", note: "Good available stock" },
  { sku: "SKU-BANANA-1DOZEN", label: "Banana 1 Dozen", note: "Limited demo stock" },
  { sku: "SKU-APPLE-1KG", label: "Apple 1KG", note: "Often exhausted during tests" },
];

const defaultOrder = {
  customerId: "customer-001",
  channel: "APP",
  paymentMode: "PREPAID",
  currency: "INR",
  totalAmount: "299.00",
  sku: "SKU-TEST-FLOW-01",
  quantity: "1",
  city: "Bengaluru",
  state: "Karnataka",
  postalCode: "560001",
};

const stageActions = [
  { key: "submit", label: "Submit Order Intent", tone: "contained" as const },
  { key: "workflow", label: "Process Workflow", tone: "outlined" as const },
  { key: "fulfillment", label: "Create Fulfillment", tone: "contained" as const },
  { key: "shipment", label: "Create Shipment", tone: "outlined" as const },
  { key: "manifest", label: "Manifest Shipment", tone: "contained" as const },
];

export function OrderFlowPage({ token }: { token: string }) {
  const [form, setForm] = useState(defaultOrder);
  const [orderIntentId, setOrderIntentId] = useState<string | null>(null);
  const [fulfillmentOrderId, setFulfillmentOrderId] = useState<string | null>(null);
  const [shipmentId, setShipmentId] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [flowWarning, setFlowWarning] = useState<string | null>(null);

  const idempotencyKey = useMemo(() => `iwos-demo-${crypto.randomUUID()}`, [orderIntentId]);

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
      setFulfillmentOrderId(null);
      setShipmentId(null);
      setMessage(`Accepted order intent ${response.orderIntentId}`);
      setFlowWarning(null);
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
      if (response.status.includes("FAILED")) {
        setFlowWarning(response.failureReason ?? `Workflow entered ${response.status}`);
        setMessage(`Workflow ${response.workflowId} ended in ${response.status}`);
      } else {
        setFlowWarning(null);
        setMessage(`Workflow ${response.workflowId} moved to ${response.status}`);
      }
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
      setFulfillmentOrderId(response.fulfillmentOrderId);
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
      setShipmentId(response.shipmentId);
      setMessage(`Created shipment ${response.shipmentId}`);
      shipmentQuery.refetch();
    },
  });

  const manifestShipment = useMutation({
    mutationFn: async () => {
      if (!shipmentQuery.data) {
        throw new Error("Shipment not ready");
      }
      return api.manifestShipment(shipmentQuery.data.shipmentId, token);
    },
    onSuccess: (response) => {
      setMessage(`Manifested shipment ${response.awbNumber}`);
      shipmentQuery.refetch();
      networkShipmentQuery.refetch();
      timelineQuery.refetch();
      notificationsQuery.refetch();
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
    enabled: Boolean(fulfillmentOrderId),
    queryKey: ["fulfillment", fulfillmentOrderId],
    queryFn: () => api.getFulfillmentById(fulfillmentOrderId!, token),
    refetchInterval: 5000,
  });

  const shipmentQuery = useQuery({
    enabled: Boolean(shipmentId),
    queryKey: ["shipment", shipmentId],
    queryFn: () => api.getShipmentById(shipmentId!, token),
    refetchInterval: 5000,
  });

  const canQueryNetwork = shipmentQuery.data?.status != null && shipmentQuery.data.status !== "CREATED";

  const networkShipmentQuery = useQuery({
    enabled: Boolean(shipmentId) && canQueryNetwork,
    queryKey: ["network-shipment", shipmentId],
    queryFn: async () => {
      try {
        return await api.getNetworkShipmentByShipmentId(shipmentId!, token);
      } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
          return null;
        }
        throw error;
      }
    },
    refetchInterval: 5000,
  });

  const timelineQuery = useQuery({
    enabled: Boolean(shipmentId) && canQueryNetwork,
    queryKey: ["timeline", shipmentId],
    queryFn: async () => {
      try {
        return await api.getScanTimelineByShipmentId(shipmentId!, token);
      } catch (error) {
        if (error instanceof ApiError && error.status === 404) {
          return null;
        }
        throw error;
      }
    },
    refetchInterval: 5000,
  });

  const notificationsQuery = useQuery({
    enabled: Boolean(shipmentId),
    queryKey: ["notifications", shipmentId],
    queryFn: () => api.listNotificationsByShipmentId(shipmentId!, token),
    refetchInterval: 5000,
  });

  const anyError =
    createOrder.isError ||
    processWorkflow.isError ||
    processFulfillment.isError ||
    createShipment.isError ||
    manifestShipment.isError ||
    recordScan.isError;

  const mutationErrorDetail =
    [createOrder.error, processWorkflow.error, processFulfillment.error, createShipment.error, manifestShipment.error, recordScan.error]
      .find(Boolean);

  const mutationErrorMessage =
    mutationErrorDetail instanceof ApiError
      ? typeof mutationErrorDetail.details === "object" && mutationErrorDetail.details !== null && "message" in mutationErrorDetail.details
        ? String((mutationErrorDetail.details as { message?: unknown }).message ?? mutationErrorDetail.message)
        : mutationErrorDetail.message
      : mutationErrorDetail instanceof Error
        ? mutationErrorDetail.message
        : null;

  const stages = [
    {
      label: "Order Intent",
      status: orderQuery.data ? "ACCEPTED" : "PENDING",
      ref: orderQuery.data?.orderIntentId ?? "Awaiting intent",
      helper: formatDateTime(orderQuery.data?.acceptedAt),
    },
    {
      label: "Workflow",
      status: workflowQuery.data?.status ?? "PENDING",
      ref: workflowQuery.data?.workflowId ?? "Awaiting workflow",
      helper: workflowQuery.data?.failureReason ?? formatDateTime(workflowQuery.data?.updatedAt),
    },
    {
      label: "Fulfillment",
      status: fulfillmentQuery.data?.status ?? "PENDING",
      ref: fulfillmentQuery.data?.fulfillmentOrderId ?? "Awaiting fulfillment",
      helper: fulfillmentQuery.data?.warehouseCode ?? formatDateTime(fulfillmentQuery.data?.updatedAt),
    },
    {
      label: "Shipment",
      status: shipmentQuery.data?.status ?? "PENDING",
      ref: shipmentQuery.data?.awbNumber ?? "Awaiting shipment",
      helper: shipmentQuery.data?.originNodeId ?? formatDateTime(shipmentQuery.data?.updatedAt),
    },
    {
      label: "Tracking",
      status: timelineQuery.data?.currentStatus ?? "PENDING",
      ref: timelineQuery.data?.lastScanType ?? "Awaiting network scan",
      helper: formatDateTime(timelineQuery.data?.updatedAt),
    },
  ];

  return (
    <Stack spacing={3}>
      <Box sx={{ animation: "fadeUp 420ms ease both" }}>
        <PageHeader
          badges={[
            { label: "Order Intake", color: "primary" },
            { label: "Warehouse", color: "secondary" },
            { label: "Shipment + Notifications", color: "success" },
          ]}
          description="Run the exact demo path in production order. The workflow, reservation, fulfillment, shipment, and tracking states below are all backed by live services, not mocked frontend state."
          eyebrow="Execution Flow"
          title="Order Flow Command"
        />
      </Box>

      {anyError ? <Alert severity="error">{mutationErrorMessage ?? "One of the flow actions failed. Check the latest stage and retry from the next valid action."}</Alert> : null}
      {flowWarning ? <Alert severity="warning">{flowWarning}</Alert> : null}
      {message ? <Alert severity="success">{message}</Alert> : null}
      {createOrder.isPending || processWorkflow.isPending || processFulfillment.isPending || createShipment.isPending || manifestShipment.isPending || recordScan.isPending ? (
        <LinearProgress />
      ) : null}

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 4 }} sx={{ animation: "fadeUp 520ms ease both" }}>
          <SectionCard subtitle="Create one safe demo order and drive it stage by stage." title="Execution Rail">
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}>
                <TextField
                  fullWidth
                  label="Customer ID"
                  onChange={(event) => setForm((current) => ({ ...current, customerId: event.target.value }))}
                  value={form.customerId}
                />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <TextField
                  fullWidth
                  label="Demo SKU"
                  onChange={(event) => setForm((current) => ({ ...current, sku: event.target.value }))}
                  select
                  value={form.sku}
                >
                  {demoSkuOptions.map((option) => (
                    <MenuItem key={option.sku} value={option.sku}>
                      {option.label} · {option.note}
                    </MenuItem>
                  ))}
                </TextField>
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
            </Grid>

            <Alert severity="info">
              Preferred demo SKUs: `Test Flow`, `Mango 1KG`, `Grape 500G`. Avoid `Apple 1KG` after repeated runs because inventory is often exhausted.
            </Alert>

            <Stack spacing={1.25}>
              {stageActions.map((action) => {
                const handlerByKey = {
                  submit: () => createOrder.mutate(),
                  workflow: () => processWorkflow.mutate(),
                  fulfillment: () => processFulfillment.mutate(),
                  shipment: () => createShipment.mutate(),
                  manifest: () => manifestShipment.mutate(),
                };

                const disabledByKey = {
                  submit: false,
                  workflow: !orderIntentId,
                  fulfillment: !orderIntentId,
                  shipment: !fulfillmentQuery.data,
                  manifest: !shipmentQuery.data,
                };

                return (
                  <Button
                    disabled={disabledByKey[action.key as keyof typeof disabledByKey]}
                    key={action.key}
                    onClick={handlerByKey[action.key as keyof typeof handlerByKey]}
                    variant={action.tone}
                  >
                    {action.label}
                  </Button>
                );
              })}
            </Stack>

            <Box
              sx={{
                borderRadius: 4,
                p: 2,
                bgcolor: "rgba(15,94,168,0.05)",
                border: "1px solid rgba(15,94,168,0.08)",
              }}
            >
              <Typography fontWeight={800} sx={{ mb: 1 }} variant="body2">
                Tracking transitions
              </Typography>
              <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
                {["HUB_RECEIVED", "OUT_FOR_DELIVERY", "DELIVERED"].map((scanType) => (
                  <Button
                    disabled={!networkShipmentQuery.data}
                    key={scanType}
                    onClick={() => recordScan.mutate(scanType)}
                    variant="text"
                  >
                    {scanType.replaceAll("_", " ")}
                  </Button>
                ))}
              </Stack>
            </Box>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 8 }} sx={{ animation: "fadeUp 620ms ease both" }}>
          <SectionCard subtitle="Each block below is driven by the live backend state." title="Stage Board">
            <Grid container spacing={2}>
              {stages.map((stage, index) => (
                <Grid key={stage.label} size={{ xs: 12, md: 6, xl: 4 }}>
                  <Box
                    sx={{
                      height: "100%",
                      borderRadius: 4,
                      p: 2.25,
                      border: "1px solid rgba(17,35,53,0.08)",
                      background: "linear-gradient(180deg, rgba(255,255,255,0.94), rgba(244,248,252,0.92))",
                      animation: `fadeUp ${680 + index * 70}ms ease both`,
                    }}
                  >
                    <Stack spacing={1.1}>
                      <Typography color="text.secondary" sx={{ letterSpacing: 1.2, textTransform: "uppercase", fontWeight: 800 }} variant="caption">
                        {stage.label}
                      </Typography>
                      <StatusChip value={stage.status} />
                      <Typography fontWeight={800} variant="body2">
                        {stage.ref}
                      </Typography>
                      <Typography color="text.secondary" variant="caption">
                        {stage.helper}
                      </Typography>
                    </Stack>
                  </Box>
                </Grid>
              ))}
            </Grid>

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
          </SectionCard>
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, xl: 6 }} sx={{ animation: "fadeUp 760ms ease both" }}>
          <SectionCard subtitle="Low-level workflow and reservation details from order-orchestrator." title="Workflow Details">
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Fulfillment node
                </Typography>
                <Typography fontWeight={700} variant="body1">
                  {workflowQuery.data?.fulfillmentNodeId ?? "-"}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography color="text.secondary" variant="caption">
                  Payment status
                </Typography>
                <Typography fontWeight={700} variant="body1">
                  {workflowQuery.data?.payment?.status ?? "-"}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <Typography color="text.secondary" variant="caption">
                  Reservation count
                </Typography>
                <Typography variant="body1">{workflowQuery.data?.reservations?.length ?? 0}</Typography>
              </Grid>
            </Grid>
          </SectionCard>
        </Grid>

        <Grid size={{ xs: 12, xl: 6 }} sx={{ animation: "fadeUp 840ms ease both" }}>
          <SectionCard subtitle="Customer-facing updates created after downstream scan milestones." title="Notification Feed">
            <Stack spacing={1}>
              {(notificationsQuery.data ?? []).slice(0, 4).map((notification) => (
                <Alert key={notification.notificationId} severity="info">
                  {notification.title}: {notification.message}
                </Alert>
              ))}
              {!notificationsQuery.data?.length ? (
                <Alert severity="warning">No customer notifications yet. They appear after shipment scan milestones are ingested.</Alert>
              ) : null}
            </Stack>
          </SectionCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
