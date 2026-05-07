import HubRounded from "@mui/icons-material/HubRounded";
import SecurityRounded from "@mui/icons-material/SecurityRounded";
import TimelineRounded from "@mui/icons-material/TimelineRounded";
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
} from "@mui/material";
import { api, ApiError } from "../api";
import { StatCard } from "../components/StatCard";
import type { TokenResponse } from "../types";

const principals = [
  { username: "ops.admin", password: "demo-pass", requestedNodeId: null, label: "Ops Admin" },
  { username: "planner.analyst", password: "demo-pass", requestedNodeId: null, label: "Planner Analyst" },
  { username: "store.manager.blr", password: "demo-pass", requestedNodeId: "NODE-BLR-DS-01", label: "Store Manager BLR DS 01" },
  { username: "fc.operator.blr", password: "demo-pass", requestedNodeId: "NODE-BLR-FC-01", label: "FC Operator BLR FC 01" },
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

  return (
    <Box
      sx={{
        minHeight: "100vh",
        p: { xs: 2, md: 4 },
        background:
          "radial-gradient(circle at top left, rgba(20,75,125,0.22), transparent 22%), radial-gradient(circle at bottom right, rgba(183,103,18,0.18), transparent 24%), linear-gradient(140deg, #071320 0%, #0f2740 58%, #143c60 100%)",
      }}
    >
      <Grid alignItems="stretch" container spacing={3} sx={{ minHeight: "100%" }}>
        <Grid size={{ xs: 12, lg: 7 }}>
          <Box
            sx={{
              height: "100%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",
              borderRadius: 6,
              p: { xs: 3, md: 5 },
              color: "#fff",
            }}
          >
            <Stack spacing={2.5}>
              <Typography sx={{ letterSpacing: 1.6, textTransform: "uppercase", opacity: 0.78 }} variant="caption">
                Intelligent Warehouse Orchestration System
              </Typography>
              <Typography sx={{ maxWidth: 760 }} variant="h3">
                Professional operations console for order intake, warehouse orchestration, parcel movement, and AI planning.
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.78)", maxWidth: 760 }} variant="body1">
                This console is backed by Kong, JWT/RSA auth, event-driven Spring services, control-tower snapshots, and a 15-minute forecasting pipeline.
              </Typography>
            </Stack>

            <Grid container spacing={2.5} sx={{ mt: 2 }}>
              <Grid size={{ xs: 12, md: 4 }}>
                <StatCard helper="Order intake to shipment milestones" label="Flow Coverage" value="E2E" />
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <StatCard helper="JWT + RS256 through gateway" label="Security Mode" value="Protected" />
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <StatCard helper="Model + forecast refresh cadence" label="Planning Cadence" value="15 min" />
              </Grid>
            </Grid>

            <Grid container spacing={2.5} sx={{ mt: 2 }}>
              <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ bgcolor: "rgba(255,255,255,0.08)", color: "#fff", boxShadow: "none" }}>
                  <CardContent>
                    <Stack direction="row" spacing={1.5}>
                      <TimelineRounded />
                      <Stack spacing={0.75}>
                        <Typography fontWeight={700} variant="body1">
                          Forecasting
                        </Typography>
                        <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="body2">
                          Demand, replenishment, and stockout-risk outputs from the planning service.
                        </Typography>
                      </Stack>
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ bgcolor: "rgba(255,255,255,0.08)", color: "#fff", boxShadow: "none" }}>
                  <CardContent>
                    <Stack direction="row" spacing={1.5}>
                      <HubRounded />
                      <Stack spacing={0.75}>
                        <Typography fontWeight={700} variant="body1">
                          Operations
                        </Typography>
                        <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="body2">
                          Control tower, node registry, shipment scans, and notifications in one surface.
                        </Typography>
                      </Stack>
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <Card sx={{ bgcolor: "rgba(255,255,255,0.08)", color: "#fff", boxShadow: "none" }}>
                  <CardContent>
                    <Stack direction="row" spacing={1.5}>
                      <SecurityRounded />
                      <Stack spacing={0.75}>
                        <Typography fontWeight={700} variant="body1">
                          Security
                        </Typography>
                        <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="body2">
                          Role and node-scoped principals issue real access tokens through identity-service.
                        </Typography>
                      </Stack>
                    </Stack>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Box>
        </Grid>

        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ borderRadius: 6, maxWidth: 560, ml: { lg: "auto" } }}>
            <CardContent sx={{ p: { xs: 3, md: 4 } }}>
              <Stack spacing={3}>
                <Stack spacing={1}>
                  <Typography variant="h4">Sign in to the demo console</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Choose a pre-configured principal and authenticate through the gateway-backed identity flow.
                  </Typography>
                </Stack>

                <TextField
                  fullWidth
                  label="Demo principal"
                  onChange={(event) => setSelected(event.target.value)}
                  select
                  value={selected}
                >
                  {principals.map((principal) => (
                    <MenuItem key={principal.label} value={principal.label}>
                      {principal.label}
                    </MenuItem>
                  ))}
                </TextField>

                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField fullWidth InputProps={{ readOnly: true }} label="Username" value={current.username} />
                  </Grid>
                  <Grid size={{ xs: 12, sm: 6 }}>
                    <TextField
                      fullWidth
                      InputProps={{ readOnly: true }}
                      label="Node scope"
                      value={current.requestedNodeId ?? "GLOBAL"}
                    />
                  </Grid>
                </Grid>

                {loginMutation.isError ? (
                  <Alert severity="error">
                    {loginMutation.error instanceof ApiError ? loginMutation.error.message : "Authentication failed"}
                  </Alert>
                ) : null}

                <Button onClick={() => loginMutation.mutate()} size="large" variant="contained">
                  {loginMutation.isPending ? "Signing in..." : "Enter Console"}
                </Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
