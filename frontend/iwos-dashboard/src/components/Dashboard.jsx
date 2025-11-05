import React from 'react';
import { Box, Grid, Paper, Typography } from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import InventoryIcon from '@mui/icons-material/Inventory';
import WarehouseIcon from '@mui/icons-material/Warehouse';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';

const StatCard = ({ title, value, icon, color }) => (
  <Paper sx={{ p: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
    <Box>
      <Typography variant="h6" color="textSecondary">{title}</Typography>
      <Typography variant="h3">{value}</Typography>
    </Box>
    <Box sx={{ color, fontSize: 60 }}>{icon}</Box>
  </Paper>
);

function Dashboard() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Total Orders" value="1,234" icon={<ShoppingCartIcon fontSize="inherit" />} color="primary.main" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="SKUs" value="456" icon={<InventoryIcon fontSize="inherit" />} color="success.main" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Warehouses" value="8" icon={<WarehouseIcon fontSize="inherit" />} color="warning.main" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard title="Revenue" value="$45.2K" icon={<TrendingUpIcon fontSize="inherit" />} color="error.main" />
        </Grid>
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>Recent Activity</Typography>
            <Typography>Welcome to IWOS - Intelligent Warehouse Operations System</Typography>
            <Typography color="textSecondary" sx={{ mt: 2 }}>
              Navigate using the sidebar to manage orders, inventory, and warehouses.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

export default Dashboard;
