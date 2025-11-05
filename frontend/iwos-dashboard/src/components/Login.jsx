import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, Container, Paper, TextField, Typography } from '@mui/material';
import { useDispatch } from 'react-redux';
import { setCredentials } from '../features/authSlice';
import { authAPI } from '../services/api';

function Login() {
  const [email, setEmail] = useState('admin@iwos.com');
  const [password, setPassword] = useState('Admin@123');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await authAPI.login({ email, password });
      dispatch(setCredentials(response.data));
      navigate('/');
    } catch (err) {
      setError('Invalid credentials. Use admin@iwos.com / Admin@123');
      console.error('Login error:', err);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Paper sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" align="center" gutterBottom>
            IWOS Login
          </Typography>
          <Typography variant="body2" align="center" color="textSecondary" gutterBottom>
            Intelligent Warehouse Operations System
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              margin="normal"
              required
              fullWidth
              label="Email Address"
              autoComplete="email"
              autoFocus
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              label="Password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {error && (
              <Typography color="error" variant="body2" sx={{ mt: 1 }}>
                {error}
              </Typography>
            )}
            <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
              Sign In
            </Button>
            <Typography variant="body2" color="textSecondary" align="center">
              Default: admin@iwos.com / Admin@123
            </Typography>
          </form>
        </Paper>
      </Box>
    </Container>
  );
}

export default Login;
