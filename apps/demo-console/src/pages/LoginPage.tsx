import { useState } from "react";
import { Alert, Box, Button, Card, CardContent, MenuItem, Stack, TextField, Typography } from "@mui/material";
import { useMutation } from "@tanstack/react-query";
import { api, ApiError } from "../api";
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
        display: "grid",
        placeItems: "center",
        background:
          "radial-gradient(circle at top left, rgba(15,76,129,0.18), transparent 40%), linear-gradient(135deg, #071623 0%, #0f4c81 100%)",
        p: 3,
      }}
    >
      <Card sx={{ maxWidth: 520, width: "100%" }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={3}>
            <Stack spacing={1}>
              <Typography variant="h4">IWOS Demo Console</Typography>
              <Typography color="text.secondary">
                Authenticate through Kong and drive the order-to-operations flow from one frontend.
              </Typography>
            </Stack>
            <TextField
              fullWidth
              label="Demo Principal"
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
            <Stack spacing={0.5}>
              <Typography variant="body2">Username: {current.username}</Typography>
              <Typography color="text.secondary" variant="body2">
                Node scope: {current.requestedNodeId ?? "global"}
              </Typography>
            </Stack>
            {loginMutation.isError ? (
              <Alert severity="error">
                {loginMutation.error instanceof ApiError
                  ? `${loginMutation.error.message}`
                  : "Authentication failed"}
              </Alert>
            ) : null}
            <Button onClick={() => loginMutation.mutate()} size="large" variant="contained">
              {loginMutation.isPending ? "Signing in..." : "Sign in"}
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}

