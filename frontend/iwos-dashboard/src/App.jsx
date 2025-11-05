import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Box } from '@mui/material';
import Dashboard from './components/Dashboard';
import Orders from './components/Orders';
import Inventory from './components/Inventory';
import Warehouses from './components/Warehouses';
import Layout from './components/Layout';
import Login from './components/Login';

/**
 * Main Application Component
 * Implements routing for all pages
 */
function App() {
  return (
    <Box sx={{ display: 'flex' }}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="orders" element={<Orders />} />
          <Route path="inventory" element={<Inventory />} />
          <Route path="warehouses" element={<Warehouses />} />
        </Route>
      </Routes>
    </Box>
  );
}

export default App;
