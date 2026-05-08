import LockOpenRounded from "@mui/icons-material/LockOpenRounded";
import PrecisionManufacturingRounded from "@mui/icons-material/PrecisionManufacturingRounded";
import RouteRounded from "@mui/icons-material/RouteRounded";
import TrendingUpRounded from "@mui/icons-material/TrendingUpRounded";
import CloudSyncRounded from "@mui/icons-material/CloudSyncRounded";
import DataUsageRounded from "@mui/icons-material/DataUsageRounded";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  MenuItem,
  Stack,
  TextField,
  Typography,
  Chip,
  Divider,
} from "@mui/material";
import { api, ApiError } from "../api";
import { StatCard } from "../components/StatCard";
import type { TokenResponse } from "../types";

const principals = [
  { username: "ops.admin", password: "demo-pass", requestedNodeId: null, label: "Operations Admin (Global)" },
  { username: "planner.analyst", password: "demo-pass", requestedNodeId: null, label: "Planner Analyst (Global)" },
  { username: "store.manager.blr", password: "demo-pass", requestedNodeId: "NODE-BLR-DS-01", label: "Store Manager BLR DS 01" },
  { username: "fc.operator.blr", password: "demo-pass", requestedNodeId: "NODE-BLR-FC-01", label: "FC Operator BLR FC 01" },
];

const systemHighlights = [
  {
    icon: <TrendingUpRounded />,
    title: "Real-time Planning",
    description: "AI-powered forecasting with 15-min refresh, stockout prediction, and automated replenishment.",
  },
  {
    icon: <PrecisionManufacturingRounded />,
    title: "Complete Stack",
    description: "Orders, warehouse operations, shipments, tracking, notifications, returns and network visibility.",
  },
  {
    icon: <CloudSyncRounded />,
    title: "Event-Driven Architecture",
    description: "Kafka-powered async processing, control-tower read models, and real-time data synchronization.",
  },
  {
    icon: <DataUsageRounded />,
    title: "Data Management",
    description: "Full CRUD operations, advanced filtering, pagination, and unified dashboard views.",
  },
  {
    icon: <RouteRounded />,
    title: "Live Workflow",
    description: "End-to-end order submission, fulfillment advancement, shipment tracking with scans.",
  },
  {
    icon: <LockOpenRounded />,
    title: "Secure Access",
    description: "JWT RS256 authentication, role-based access control, node scoping, and granular permissions.",
  },
];

export function LoginPage({ onSuccess }: { onSuccess: (token: TokenResponse) => void }) {
  const [selected, setSelected] = useState(principals[0].label);
  const current = principals.find((principal) => principal.label === selected) ?? principals[0];

  const loginMutation = useMutation({
    mutationFn: () =>
      api.login({
        username: current.username,
        password: current.password,
        requestedNodeId: current.requestedNodeId,
      }),
    onSuccess,
  });

  const loginErrorMessage =
    loginMutation.error instanceof ApiError
      ? typeof loginMutation.error.details === "object" && loginMutation.error.details !== null && "message" in loginMutation.error.details
        ? String((loginMutation.error.details as { message?: unknown }).message ?? loginMutation.error.message)
        : loginMutation.error.message
      : "Authentication failed. Try again.";

  return (
    <Box
      sx={{
        minHeight: "100vh",
        px: { xs: 2, md: 4 },
        py: { xs: 2.5, md: 4 },
        background:
          "radial-gradient(circle at top left, rgba(22,102,179,0.25), transparent 20%), radial-gradient(circle at bottom right, rgba(255,138,61,0.2), transparent 20%), linear-gradient(135deg, #05101a 0%, #0a1f35 40%, #102f4d 100%)",
      }}
    >
      <Grid alignItems="stretch" container spacing={3} sx={{ minHeight: "100%" }}>
        {/* Left Panel - Info */}
        <Grid size={{ xs: 12, lg: 7 }}>
          <Box
            sx={{
              height: "100%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",
              borderRadius: 6,
              p: { xs: 3, md: 4.5 },
              color: "#fff",
            }}
          >
            {/* Header */}
            <Stack spacing={3}>
              <Stack spacing={1.5}>
                <Typography sx={{ letterSpacing: 2.2, textTransform: "uppercase", opacity: 0.7, fontWeight: 900, fontSize: "0.7rem" }} variant="caption">
                  Intelligent Warehouse Orchestration System
                </Typography>
                <Typography sx={{ maxWidth: 780, lineHeight: 1.08, letterSpacing: 0.3 }} variant="h2">
                  Unified Control Surface for Modern Fulfillment
                </Typography>
                <Typography sx={{ color: "rgba(255,255,255,0.72)", maxWidth: 750, lineHeight: 1.7, fontSize: "1.05rem", fontWeight: 300 }} variant="body1">
                  Kong-secured APIs, event-driven architecture, AI-powered planning, and real-time execution visibility. Manage orders, warehouse operations, shipments, and forecasting in one professional dashboard.
                </Typography>
              </Stack>

              {/* Key Metrics */}
              <Grid container spacing={2} sx={{ mt: 1 }}>
                <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                  <StatCard helper="Order to delivery" label="Flow" value="E2E" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                  <StatCard helper="Kong + RS256 JWT" label="Security" value="Protected" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                  <StatCard helper="15-minute cadence" label="Planning" value="Live" />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                  <StatCard helper="Multi-service platform" label="Coverage" value="Complete" />
                </Grid>
              </Grid>
            </Stack>

            {/* Features Grid */}
            <Grid container spacing={2.5} sx={{ mt: 2 }}>
              {systemHighlights.map((highlight) => (
                <Grid key={highlight.title} size={{ xs: 12, md: 6 }}>
                  <Card sx={{ bgcolor: "rgba(255,255,255,0.06)", color: "#fff", boxShadow: "none", borderRadius: 3.5, border: "1px solid rgba(255,255,255,0.08)", backdropFilter: "blur(10px)" }}>
                    <CardContent sx={{ p: 2.25 }}>
                      <Stack direction="row" spacing={1.5}>
                        <Box sx={{ color: "rgb(255,182,77)", flexShrink: 0 }}>{highlight.icon}</Box>
                        <Stack spacing={0.6}>
                          <Typography fontWeight={700} variant="body2">
                            {highlight.title}
                          </Typography>
                          <Typography sx={{ color: "rgba(255,255,255,0.65)", fontSize: "0.85rem" }} variant="caption">
                            {highlight.description}
                          </Typography>
                        </Stack>
                      </Stack>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>

            {/* Footer */}
            <Stack spacing={1.5} sx={{ mt: 3 }}>
              <Divider sx={{ borderColor: "rgba(255,255,255,0.1)" }} />
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ pt: 1 }}>
                <Typography sx={{ fontSize: "0.75rem", color: "rgba(255,255,255,0.5)", textTransform: "uppercase", letterSpacing: 1 }}>
                  Powered by Microservices and AI Planning
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Chip label="PostgreSQL" size="small" sx={{ bgcolor: "rgba(255,255,255,0.1)", color: "#fff" }} />
                  <Chip label="Kafka" size="small" sx={{ bgcolor: "rgba(255,255,255,0.1)", color: "#fff" }} />
                  <Chip label="Redis" size="small" sx={{ bgcolor: "rgba(255,255,255,0.1)", color: "#fff" }} />
                  <Chip label="MLflow" size="small" sx={{ bgcolor: "rgba(255,255,255,0.1)", color: "#fff" }} />
                </Stack>
              </Stack>
            </Stack>
          </Box>
        </Grid>

        {/* Right Panel - Login Form */}
        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ borderRadius: 5, maxWidth: 520, ml: { lg: "auto" }, boxShadow: "0 25px 50px rgba(0,0,0,0.3)", backdropFilter: "blur(20px)" }}>
            <CardContent sx={{ p: { xs: 3.5, md: 4 } }}>
              <Stack spacing={3.5}>
                {/* Title */}
                <Stack spacing={0.8}>
                  <Typography variant="h4" sx={{ fontWeight: 800 }}>
                    Welcome to IWOS
                  </Typography>
                  <Typography color="text.secondary" variant="body2" sx={{ lineHeight: 1.6 }}>
                    Choose your role and authenticate. Each principal has specific permissions and node scope.
                  </Typography>
                </Stack>

                {/* Principal Selector */}
                <TextField
                  fullWidth
                  label="Select your role"
                  onChange={(event) => setSelected(event.target.value)}
                  select
                  value={selected}
                  variant="outlined"
                  size="medium"
                  sx={{
                    "& .MuiOutlinedInput-root": {
                      fontSize: "0.95rem",
                    },
                  }}
                >
                  {principals.map((principal) => (
                    <MenuItem key={principal.label} value={principal.label}>
                      {principal.label}
                    </MenuItem>
                  ))}
                </TextField>

                <Divider />

                {/* User Details */}
                <Stack spacing={2}>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <TextField fullWidth InputProps={{ readOnly: true }} label="Username" value={current.username} size="small" variant="outlined" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <TextField
                        fullWidth
                        InputProps={{ readOnly: true }}
                        label="Node Scope"
                        value={current.requestedNodeId ? current.requestedNodeId.split("-").slice(1).join("-") : "Global"}
                        size="small"
                        variant="outlined"
                      />
                    </Grid>
                  </Grid>

                  <Box sx={{ bgcolor: "rgba(0,0,0,0.02)", p: 2, borderRadius: 2 }}>
                    <Typography variant="caption" color="text.secondary" sx={{ display: "block", mb: 0.8 }}>
                      Password (Demo)
                    </Typography>
                    <Typography variant="body2" sx={{ fontFamily: "monospace", fontWeight: 600 }}>
                      {current.password}
                    </Typography>
                  </Box>
                </Stack>

                {/* Error Alert */}
                {loginMutation.isError ? (
                  <Alert severity="error" sx={{ borderRadius: 2 }}>
                    <Typography variant="body2">{loginErrorMessage}</Typography>
                  </Alert>
                ) : null}

                {/* Login Button */}
                <Button
                  onClick={() => loginMutation.mutate()}
                  size="large"
                  variant="contained"
                  fullWidth
                  sx={{
                    py: 1.5,
                    fontSize: "1rem",
                    fontWeight: 700,
                    textTransform: "uppercase",
                    letterSpacing: 1.2,
                  }}
                  disabled={loginMutation.isPending}
                >
                  {loginMutation.isPending ? "Authenticating..." : "Enter Console"}
                </Button>

                {/* Footer Info */}
                <Box sx={{ bgcolor: "rgba(0,0,0,0.02)", p: 1.75, borderRadius: 2, textAlign: "center" }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontSize: "0.75rem", lineHeight: 1.5 }}>
                    Kong-secured API Gateway | Real-time Data | Production-Ready Architecture
                  </Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
