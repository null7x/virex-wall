import express from 'express';
import compression from 'compression';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';

import wallpapersRouter from './routes/wallpapers.js';
import { startSelfPing } from './utils/selfPing.js';

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());

// CORS - allow all for now (configure for production)
app.use(cors({
  origin: '*',
  methods: ['GET'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

// Compression for faster responses
app.use(compression());

// Rate limiting - 100 requests per minute
const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 100,
  message: { error: 'Too many requests, please try again later.' },
  standardHeaders: true,
  legacyHeaders: false
});
app.use(limiter);

// Parse JSON
app.use(express.json());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Ping endpoint for Koyeb anti-sleep
app.get('/ping', (req, res) => {
  res.json({ pong: true });
});

// Root endpoint - API info
app.get('/', (req, res) => {
  res.json({
    name: 'VIREX Wallpapers API',
    version: '1.0.0',
    status: 'running',
    endpoints: {
      health: '/health',
      trending: '/wallpapers/trending',
      search: '/wallpapers/search?q=query',
      categories: '/wallpapers/categories',
      category: '/wallpapers/category/:name'
    }
  });
});

// API routes
app.use('/wallpapers', wallpapersRouter);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(500).json({ error: 'Internal server error' });
});

// Start server
app.listen(PORT, () => {
  console.log(`🚀 VIREX Backend running on port ${PORT}`);
  
  // Start self-ping to prevent Koyeb cold starts
  startSelfPing();
});

export default app;
