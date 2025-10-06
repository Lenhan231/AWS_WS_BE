const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

const app = express();

// Middleware
app.use(helmet()); // Security headers
app.use(cors({
  origin: process.env.CORS_ORIGIN?.split(',') || '*',
  credentials: true
}));
app.use(morgan('dev')); // Logging
app.use(express.json()); // Parse JSON bodies
app.use(express.urlencoded({ extended: true })); // Parse URL-encoded bodies

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV
  });
});

// API Routes
const apiVersion = process.env.API_VERSION || 'v1';
app.use(`/api/${apiVersion}/auth`, require('./routes/auth.routes'));
app.use(`/api/${apiVersion}/gyms`, require('./routes/gym.routes'));
app.use(`/api/${apiVersion}/pt-users`, require('./routes/ptUser.routes'));
app.use(`/api/${apiVersion}/offers`, require('./routes/offer.routes'));
app.use(`/api/${apiVersion}/search`, require('./routes/search.routes'));
app.use(`/api/${apiVersion}/ratings`, require('./routes/rating.routes'));
app.use(`/api/${apiVersion}/reports`, require('./routes/report.routes'));
app.use(`/api/${apiVersion}/admin`, require('./routes/admin.routes'));
app.use(`/api/${apiVersion}/media`, require('./routes/media.routes'));

// 404 handler
app.use((req, res, next) => {
  res.status(404).json({
    status: 404,
    error: 'Not Found',
    message: `Cannot ${req.method} ${req.originalUrl}`,
    timestamp: new Date().toISOString()
  });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);

  const status = err.status || err.statusCode || 500;
  const message = err.message || 'Internal Server Error';

  res.status(status).json({
    status,
    error: err.name || 'Error',
    message,
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack }),
    timestamp: new Date().toISOString(),
    path: req.originalUrl
  });
});

module.exports = app;
