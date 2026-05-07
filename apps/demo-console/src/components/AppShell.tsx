import AssessmentRounded from "@mui/icons-material/AssessmentRounded";
import BoltRounded from "@mui/icons-material/BoltRounded";
import DashboardRounded from "@mui/icons-material/DashboardRounded";
import InsightsRounded from "@mui/icons-material/InsightsRounded";
import Inventory2Rounded from "@mui/icons-material/Inventory2Rounded";
import LocalShippingRounded from "@mui/icons-material/LocalShippingRounded";
import LogoutRounded from "@mui/icons-material/LogoutRounded";
import ReplayRounded from "@mui/icons-material/ReplayRounded";
import {
  AppBar,
  Avatar,
  Box,
  Button,
  Chip,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  Toolbar,
  Typography,
} from "@mui/material";
import { NavLink, Outlet } from "react-router-dom";
import type { TokenResponse } from "../types";

const drawerWidth = 308;

const navigation = [
  { label: "Overview", path: "/", icon: <DashboardRounded /> },
  { label: "Order Flow", path: "/order-flow", icon: <LocalShippingRounded /> },
  { label: "Forecasting", path: "/forecasting", icon: <InsightsRounded /> },
  { label: "Operations", path: "/operations", icon: <AssessmentRounded /> },
  { label: "Nodes", path: "/nodes", icon: <Inventory2Rounded /> },
  { label: "Returns", path: "/returns", icon: <ReplayRounded /> },
];

export function AppShell({ session, onLogout }: { session: TokenResponse; onLogout: () => void }) {
  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        color="inherit"
        elevation={0}
        position="fixed"
        sx={{
          width: { lg: `calc(100% - ${drawerWidth}px)` },
          ml: { lg: `${drawerWidth}px` },
          borderBottom: "1px solid rgba(17,35,53,0.08)",
          bgcolor: "rgba(248,250,252,0.88)",
          backdropFilter: "blur(20px)",
        }}
      >
        <Toolbar sx={{ gap: 2, justifyContent: "space-between", minHeight: 84 }}>
          <Stack spacing={0.4}>
            <Typography sx={{ color: "text.primary" }} variant="h6">
              IWOS Operations Console
            </Typography>
            <Typography color="text.secondary" variant="body2">
              Fulfillment, parcel movement, forecasting, and control-tower visibility
            </Typography>
          </Stack>
          <Stack alignItems="center" direction="row" spacing={1.25}>
            <Chip color="primary" icon={<BoltRounded />} label="Live Local Cell" size="small" variant="filled" />
            <Chip color="secondary" label="JWT Edge" size="small" variant="outlined" />
            <Stack alignItems="flex-end" spacing={0.15}>
              <Typography fontWeight={600} variant="body2">
                {session.username}
              </Typography>
              <Typography color="text.secondary" sx={{ maxWidth: 280, textAlign: "right" }} variant="caption">
                {session.role} · {session.nodeIds.slice(0, 3).join(", ") || "GLOBAL"}
              </Typography>
            </Stack>
            <Avatar sx={{ bgcolor: "primary.dark", color: "#fff", width: 38, height: 38 }}>{session.role[0]}</Avatar>
            <Button color="inherit" onClick={onLogout} startIcon={<LogoutRounded />} variant="outlined">
              Sign out
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>

      <Drawer
        sx={{
          display: { xs: "none", lg: "block" },
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: {
            width: drawerWidth,
            boxSizing: "border-box",
            borderRight: "1px solid rgba(255,255,255,0.06)",
            background:
              "radial-gradient(circle at top left, rgba(255,255,255,0.12), transparent 28%), radial-gradient(circle at bottom right, rgba(255,138,61,0.14), transparent 24%), linear-gradient(180deg, #091624 0%, #10253a 62%, #0a192f 100%)",
            color: "#fff",
          },
        }}
        variant="permanent"
      >
        <Toolbar sx={{ minHeight: 96, px: 3 }}>
          <Stack spacing={0.85}>
            <Typography fontWeight={800} variant="h6">
              IWOS
            </Typography>
            <Typography sx={{ color: "rgba(255,255,255,0.74)" }} variant="body2">
              Operations Console
            </Typography>
          </Stack>
        </Toolbar>
        <Divider sx={{ borderColor: "rgba(255,255,255,0.08)" }} />
        <Box sx={{ px: 2.5, pt: 2.5 }}>
          <Box
            sx={{
              borderRadius: 4,
              p: 2.25,
              border: "1px solid rgba(255,255,255,0.08)",
              bgcolor: "rgba(255,255,255,0.06)",
            }}
          >
            <Stack spacing={1}>
              <Typography sx={{ color: "rgba(255,255,255,0.72)", letterSpacing: 1.2, textTransform: "uppercase" }} variant="caption">
                Session Scope
              </Typography>
              <Typography fontWeight={700} variant="body2">
                {session.role}
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="caption">
                {session.nodeIds.length ? session.nodeIds.join(" · ") : "Global platform scope"}
              </Typography>
            </Stack>
          </Box>
        </Box>
        <List sx={{ px: 2, py: 2 }}>
          {navigation.map((item) => (
            <ListItemButton
              component={NavLink}
              key={item.path}
              to={item.path}
              sx={{
                borderRadius: 3.5,
                mb: 0.75,
                minHeight: 56,
                color: "rgba(255,255,255,0.78)",
                "&.active": {
                  color: "#fff",
                  bgcolor: "rgba(255,255,255,0.13)",
                  boxShadow: "inset 0 0 0 1px rgba(255,255,255,0.08), 0 8px 20px rgba(0,0,0,0.12)",
                },
                "&:hover": {
                  bgcolor: "rgba(255,255,255,0.08)",
                },
              }}
            >
              <ListItemIcon sx={{ color: "inherit", minWidth: 40 }}>{item.icon}</ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontWeight: 600,
                }}
              />
            </ListItemButton>
          ))}
        </List>
        <Box sx={{ mt: "auto", p: 3 }}>
          <Box
            sx={{
              p: 2.25,
              borderRadius: 4,
              bgcolor: "rgba(255,255,255,0.08)",
              border: "1px solid rgba(255,255,255,0.08)",
            }}
          >
            <Stack spacing={1.1}>
              <Typography sx={{ color: "rgba(255,255,255,0.72)", letterSpacing: 1.2, textTransform: "uppercase" }} variant="caption">
                System Posture
              </Typography>
              <Typography fontWeight={700} variant="body2">
                Real demo stack
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.72)", lineHeight: 1.6 }} variant="caption">
                Kong-secured APIs, event-driven services, control-tower read models, and 15-minute forecasting refresh.
              </Typography>
            </Stack>
          </Box>
        </Box>
      </Drawer>

      <Box component="main" sx={{ ml: { lg: `${drawerWidth}px` }, px: { xs: 2, md: 3.5 }, pb: 5 }}>
        <Toolbar sx={{ minHeight: 88 }} />
        <Outlet />
      </Box>
    </Box>
  );
}
