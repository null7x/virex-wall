import express from 'express';
import { getTrending, searchWallpapers, getByCategory, getCategories } from '../services/wallpaperService.js';

const router = express.Router();

// GET /wallpapers/trending
router.get('/trending', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const perPage = parseInt(req.query.per_page) || 30;
    const wallpapers = await getTrending(page, perPage);
    res.json({ wallpapers, page, per_page: perPage });
  } catch (error) {
    console.error('Trending error:', error.message);
    res.status(500).json({ error: 'Failed to fetch trending wallpapers' });
  }
});

// GET /wallpapers/search?q=
router.get('/search', async (req, res) => {
  try {
    const query = req.query.q;
    if (!query) {
      return res.status(400).json({ error: 'Query parameter "q" is required' });
    }
    const page = parseInt(req.query.page) || 1;
    const perPage = parseInt(req.query.per_page) || 30;
    const wallpapers = await searchWallpapers(query, page, perPage);
    res.json({ wallpapers, query, page, per_page: perPage });
  } catch (error) {
    console.error('Search error:', error.message);
    res.status(500).json({ error: 'Failed to search wallpapers' });
  }
});

// GET /wallpapers/categories
router.get('/categories', (req, res) => {
  res.json({ categories: getCategories() });
});

// GET /wallpapers/category/:name
router.get('/category/:name', async (req, res) => {
  try {
    const categoryName = req.params.name;
    const page = parseInt(req.query.page) || 1;
    const perPage = parseInt(req.query.per_page) || 30;
    const wallpapers = await getByCategory(categoryName, page, perPage);
    res.json({ wallpapers, category: categoryName, page, per_page: perPage });
  } catch (error) {
    console.error('Category error:', error.message);
    res.status(500).json({ error: 'Failed to fetch category wallpapers' });
  }
});

export default router;
