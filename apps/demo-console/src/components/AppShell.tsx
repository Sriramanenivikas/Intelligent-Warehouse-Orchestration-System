import DashboardRounded from "@mui/icons-material/DashboardRounded";
import InsightsRounded from "@mui/icons-material/InsightsRounded";
import Inventory2Rounded from "@mui/icons-material/Inventory2Rounded";
import LocalShippingRounded from "@mui/icons-material/LocalShippingRounded";
import ReplayRounded from "@mui/icons-material/ReplayRounded";
import TravelExploreRounded from "@mui/icons-material/TravelExploreRounded";
import {
  AppBar,
  Avatar,
  Box,
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

const drawerWidth = 260;

const navigation = [
  { label: "Overview", path: "/", icon: <DashboardRounded /> },
  { label: "Order Flow", path: "/order-flow", icon: <LocalShippingRounded /> },
  { label: "Forecasting", path: "/forecasting", icon: <InsightsRounded /> },
  { label: "Operations", path: "/operations", icon: <TravelExploreRounded /> },
  { label: "Nodes", path: "/nodes", icon: <Inventory2Rounded /> },
  { label: "Returns", path: "/returns", icon: <ReplayRounded /> },
];

export function AppShell({ session }: { session: TokenResponse }) {
  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar
        color="inherit"
        elevation={0}
        position="fixed"
        sx={{
          width: `calc(100% - ${drawerWidth}px)`,
          ml: `${drawerWidth}px`,
          borderBottom: "1px solid rgba(15, 76, 129, 0.08)",
          bgcolor: "rgba(255,255,255,0.92)",
          backdropFilter: "blur(10px)",
        }}
      >
        <Toolbar sx={{ justifyContent: "space-between" }}>
          <Stack>
            <Typography variant="h6">IWOS Demo Console</Typography>
            <Typography color="text.secondary" variant="body2">
              Hybrid order-to-operations view through Kong
            </Typography>
          </Stack>
          <Stack alignItems="center" direction="row" spacing={2}>
            <Stack alignItems="flex-end" spacing={0.25}>
              <Typography variant="body2">{session.username}</Typography>
              <Typography color="text.secondary" variant="caption">
                {session.role} · {session.nodeIds.join(", ") || "global"}
              </Typography>
            </Stack>
            <Avatar sx={{ bgcolor: "primary.main" }}>{session.role[0]}</Avatar>
          </Stack>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          [`& .MuiDrawer-paper`]: {
            width: drawerWidth,
            boxSizing: "border-box",
            borderRight: "1px solid rgba(15, 76, 129, 0.08)",
            bgcolor: "#0b1f33",
            color: "#fff",
          },
        }}
      >
        <Toolbar>
          <Stack spacing={0.5}>
            <Typography variant="h6">Intelligent Warehouse</Typography>
            <Typography sx={{ color: "rgba(255,255,255,0.7)" }} variant="caption">
              Unified fulfillment platform
            </Typography>
          </Stack>
        </Toolbar>
        <Divider sx={{ borderColor: "rgba(255,255,255,0.08)" }} />
        <List sx={{ px: 1.5, py: 2 }}>
          {navigation.map((item) => (
            <ListItemButton
              component={NavLink}
              key={item.path}
              to={item.path}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                color: "rgba(255,255,255,0.8)",
                "&.active": {
                  bgcolor: "rgba(255,255,255,0.12)",
                  color: "#fff",
                },
              }}
            >
              <ListItemIcon sx={{ color: "inherit", minWidth: 36 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, ml: `${drawerWidth}px`, p: 3 }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}

