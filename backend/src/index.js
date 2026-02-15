import express from 'express';
import compression from 'compression';
import helmet from 'helmet';
import cors from 'cors';
import rateLimit from 'express-rate-limit';

import wallpapersRouter from './routes/wallpapers.js';
import proxyRouter from './routes/proxy.js';
import { startSelfPing } from './utils/selfPing.js';

const app = express();
const PORT = process.env.PORT || 3000;

// Trust proxy (required for Koyeb/Heroku/Railway behind reverse proxy)
app.set('trust proxy', 1);

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

// Rate limiting - 200 requests per minute (higher for image proxy)
const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 200,
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

// Reddit test endpoint
app.get('/test-reddit', async (req, res) => {
  const axios = (await import('axios')).default;
  try {
    const response = await axios.get('https://www.reddit.com/r/wallpapers/hot.json?limit=3&raw_json=1', {
      headers: { 'User-Agent': 'VIREX-Wallpapers/1.0' },
      timeout: 15000
    });
    res.json({ 
      status: 'ok', 
      posts: response.data?.data?.children?.length || 0,
      sample: response.data?.data?.children?.[0]?.data?.title
    });
  } catch (error) {
    res.json({ 
      status: 'error', 
      message: error.message,
      code: error.response?.status,
      data: error.response?.data
    });
  }
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

// Image proxy for Russia (bypasses CDN geo-blocks)
app.use('/proxy', proxyRouter);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(500).json({ error: 'Internal server error' });
});

// Start server only if not running on Vercel
if (!process.env.VERCEL) {
  app.listen(PORT, () => {
    console.log(`🚀 VIREX Backend running on port ${PORT}`);
    
    // Start self-ping to prevent Koyeb cold starts
    startSelfPing();
  });
}

export default app;
