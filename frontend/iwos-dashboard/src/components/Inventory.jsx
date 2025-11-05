import React from 'react';
import { Box, Paper, Typography } from '@mui/material';

function Inventory() {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>Inventory Management</Typography>
      <Paper sx={{ p: 3 }}>
        <Typography>Inventory management interface coming soon...</Typography>
        <Typography color="textSecondary" sx={{ mt: 2 }}>
          This will show SKUs, stock levels, and allow inventory adjustments.
        </Typography>
      </Paper>
    </Box>
  );
}

export default Inventory;
