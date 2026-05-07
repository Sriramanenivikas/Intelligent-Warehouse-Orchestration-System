import AssessmentRounded from "@mui/icons-material/AssessmentRounded";
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

const drawerWidth = 292;

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
          borderBottom: "1px solid",
          borderColor: "divider",
          bgcolor: "rgba(255,255,255,0.9)",
          backdropFilter: "blur(16px)",
        }}
      >
        <Toolbar sx={{ gap: 2, justifyContent: "space-between", minHeight: 80 }}>
          <Stack spacing={0.5}>
            <Typography sx={{ color: "primary.dark" }} variant="h6">
              IWOS Operations Console
            </Typography>
            <Typography color="text.secondary" variant="body2">
              Unified order, warehouse, network, notification, and AI planning view
            </Typography>
          </Stack>
          <Stack alignItems="center" direction="row" spacing={1.5}>
            <Chip color="primary" label="Develop" size="small" variant="outlined" />
            <Chip color="secondary" label="Gateway Auth Active" size="small" variant="outlined" />
            <Stack alignItems="flex-end" spacing={0.25}>
              <Typography fontWeight={600} variant="body2">
                {session.username}
              </Typography>
              <Typography color="text.secondary" variant="caption">
                {session.role} · {session.nodeIds.join(", ") || "GLOBAL"}
              </Typography>
            </Stack>
            <Avatar sx={{ bgcolor: "primary.main", color: "#fff" }}>{session.role[0]}</Avatar>
            <Button color="inherit" onClick={onLogout} startIcon={<LogoutRounded />} variant="text">
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
            borderRight: "1px solid rgba(255,255,255,0.08)",
            background:
              "radial-gradient(circle at top left, rgba(255,255,255,0.08), transparent 32%), linear-gradient(180deg, #081521 0%, #0d253a 100%)",
            color: "#fff",
          },
        }}
        variant="permanent"
      >
        <Toolbar sx={{ minHeight: 88, px: 3 }}>
          <Stack spacing={0.75}>
            <Typography fontWeight={700} variant="h6">
              Intelligent Warehouse
            </Typography>
            <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="body2">
              Amazon + Blinkit + parcel-network backend demo
            </Typography>
          </Stack>
        </Toolbar>
        <Divider sx={{ borderColor: "rgba(255,255,255,0.08)" }} />
        <List sx={{ px: 2, py: 2 }}>
          {navigation.map((item) => (
            <ListItemButton
              component={NavLink}
              key={item.path}
              to={item.path}
              sx={{
                borderRadius: 3,
                mb: 0.75,
                minHeight: 52,
                color: "rgba(255,255,255,0.78)",
                "&.active": {
                  color: "#fff",
                  bgcolor: "rgba(255,255,255,0.12)",
                  boxShadow: "inset 0 0 0 1px rgba(255,255,255,0.08)",
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
              p: 2,
              borderRadius: 3,
              bgcolor: "rgba(255,255,255,0.08)",
              border: "1px solid rgba(255,255,255,0.08)",
            }}
          >
            <Stack spacing={1}>
              <Typography fontWeight={600} variant="body2">
                Demo posture
              </Typography>
              <Typography sx={{ color: "rgba(255,255,255,0.72)" }} variant="caption">
                Gateway secured, ML planning on 15-minute refresh, operational snapshot exposed through control tower.
              </Typography>
            </Stack>
          </Box>
        </Box>
      </Drawer>

      <Box component="main" sx={{ ml: { lg: `${drawerWidth}px` }, px: { xs: 2, md: 3 }, pb: 4 }}>
        <Toolbar sx={{ minHeight: 88 }} />
        <Outlet />
      </Box>
    </Box>
  );
}
