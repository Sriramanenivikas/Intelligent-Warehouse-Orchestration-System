import React from 'react';
import { Box, Paper, Typography } from '@mui/material';

function Warehouses() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>Warehouse Management</Typography>
      <Paper sx={{ p: 3 }}>
        <Typography>Warehouse management interface coming soon...</Typography>
        <Typography color="textSecondary" sx={{ mt: 2 }}>
          This will show warehouses, zones, and capacity utilization.
        </Typography>
      </Paper>
    </Box>
  );
}

export default Warehouses;
