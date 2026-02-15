import express from 'express';
import axios from 'axios';

const router = express.Router();

// Image proxy - streams images through our server to bypass geo-blocks
// GET /proxy/image?url=https://images.pexels.com/...
router.get('/image', async (req, res) => {
  const imageUrl = req.query.url;
  
  if (!imageUrl) {
    return res.status(400).json({ error: 'Missing "url" parameter' });
  }

  // Whitelist allowed image domains
  const allowedDomains = [
    'images.pexels.com',
    'images.unsplash.com',
    'w.wallhaven.cc',
    'th.wallhaven.cc',
    'picsum.photos',
    'fastly.picsum.photos',
    'i.picsum.photos',
    'i.redd.it',
    'preview.redd.it',
    'i.imgur.com',
    'imgur.com',
  ];

  try {
    const parsedUrl = new URL(imageUrl);
    if (!allowedDomains.some(d => parsedUrl.hostname === d || parsedUrl.hostname.endsWith('.' + d))) {
      return res.status(403).json({ error: 'Domain not allowed' });
    }
  } catch {
    return res.status(400).json({ error: 'Invalid URL' });
  }

  try {
    const response = await axios.get(imageUrl, {
      responseType: 'stream',
      timeout: 30000,
      headers: {
        'User-Agent': 'VIREX-Wallpapers-Backend/1.0',
        'Accept': 'image/*',
      },
      maxRedirects: 5,
    });

    // Forward content headers
    const contentType = response.headers['content-type'];
    const contentLength = response.headers['content-length'];
    
    if (contentType) res.setHeader('Content-Type', contentType);
    if (contentLength) res.setHeader('Content-Length', contentLength);
    
    // Cache for 24 hours
    res.setHeader('Cache-Control', 'public, max-age=86400, s-maxage=86400');
    res.setHeader('X-Proxy-Source', 'virex-backend');

    response.data.pipe(res);
  } catch (error) {
    console.error('Image proxy error:', error.message);
    res.status(502).json({ error: 'Failed to fetch image' });
  }
});

export default router;
