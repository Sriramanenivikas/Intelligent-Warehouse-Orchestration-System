import LockOpenRounded from "@mui/icons-material/LockOpenRounded";
import PrecisionManufacturingRounded from "@mui/icons-material/PrecisionManufacturingRounded";
import RouteRounded from "@mui/icons-material/RouteRounded";
import TrendingUpRounded from "@mui/icons-material/TrendingUpRounded";
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

const systemHighlights = [
  {
    icon: <TrendingUpRounded />,
    title: "Planning loop",
    description: "Forecast runs, replenishment quantities, and model metrics refresh on a 15-minute cadence.",
  },
  {
    icon: <PrecisionManufacturingRounded />,
    title: "Execution stack",
    description: "Order, warehouse, shipment, scan, notification, returns, and node-registry services are surfaced in one console.",
  },
  {
    icon: <RouteRounded />,
    title: "Demo scope",
    description: "Submit an order, advance fulfillment, inspect shipment state, review notifications, and watch planning outputs.",
  },
  {
    icon: <LockOpenRounded />,
    title: "Access model",
    description: "JWT principals are role and node scoped. Use Ops Admin for the full surface or Planner Analyst for planning review.",
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

  return (
    <Box
      sx={{
        minHeight: "100vh",
        px: { xs: 2, md: 4 },
        py: { xs: 2.5, md: 4 },
        background:
          "radial-gradient(circle at top left, rgba(22,102,179,0.2), transparent 24%), radial-gradient(circle at bottom right, rgba(255,138,61,0.16), transparent 24%), linear-gradient(145deg, #07131f 0%, #0c2135 48%, #102f4d 100%)",
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
              p: { xs: 3, md: 4.5 },
              color: "#fff",
            }}
          >
            <Stack spacing={2.75}>
              <Typography sx={{ letterSpacing: 1.8, textTransform: "uppercase", opacity: 0.78, fontWeight: 800 }} variant="caption">
                IWOS Demo Cell
              </Typography>
              <Typography sx={{ maxWidth: 760, lineHeight: 1.05 }} variant="h3">
                Unified control surface for orders, warehouse execution, parcel state, and planning signals.
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.74)", maxWidth: 720 }} variant="body1">
                Enter with a scoped principal. The console is backed by Kong, JWT/RSA auth, warehouse and network services, control-tower reads, and a 15-minute planning loop.
              </Typography>
            </Stack>

            <Grid container spacing={2} sx={{ mt: 1.5 }}>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <StatCard helper="Order intake to parcel state" label="Flow" value="E2E" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <StatCard helper="Kong + RS256 JWT" label="Security" value="Protected" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <StatCard helper="Retrain and publish cadence" label="Planning" value="15 min" />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, xl: 3 }}>
                <StatCard helper="Ops + planning + reverse flow" label="Coverage" value="Live" />
              </Grid>
            </Grid>

            <Grid container spacing={2} sx={{ mt: 1.5 }}>
              {systemHighlights.map((highlight) => (
                <Grid key={highlight.title} size={{ xs: 12, md: 6 }}>
                  <Card sx={{ bgcolor: "rgba(255,255,255,0.05)", color: "#fff", boxShadow: "none", borderRadius: 4 }}>
                    <CardContent>
                      <Stack direction="row" spacing={1.5}>
                        {highlight.icon}
                        <Stack spacing={0.6}>
                          <Typography fontWeight={700} variant="body1">
                            {highlight.title}
                          </Typography>
                          <Typography sx={{ color: "rgba(255,255,255,0.7)" }} variant="body2">
                            {highlight.description}
                          </Typography>
                        </Stack>
                      </Stack>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Grid>

        <Grid size={{ xs: 12, lg: 5 }}>
          <Card sx={{ borderRadius: 6, maxWidth: 560, ml: { lg: "auto" } }}>
            <CardContent sx={{ p: { xs: 3, md: 4 } }}>
              <Stack spacing={3}>
                <Stack spacing={1}>
                  <Typography variant="h4">Enter console</Typography>
                  <Typography color="text.secondary" variant="body2">
                    Choose a scoped principal and authenticate through the gateway-backed identity flow.
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
