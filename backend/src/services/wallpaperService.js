import { fetchPexelsTrending, fetchPexelsSearch } from './pexelsService.js';
import { fetchUnsplashTrending, fetchUnsplashSearch } from './unsplashService.js';
import { fetchWallhavenTrending, fetchWallhavenSearch } from './wallhavenService.js';
import cache from '../cache/memoryCache.js';

// Category mappings for search queries
const CATEGORIES = {
  nature: { name: 'Nature', query: 'nature landscape forest' },
  cars: { name: 'Cars', query: 'cars automotive supercar' },
  amoled: { name: 'AMOLED', query: 'dark black minimal amoled' },
  space: { name: 'Space', query: 'space galaxy nebula stars' },
  cyberpunk: { name: 'Cyberpunk', query: 'cyberpunk neon city futuristic' },
  minimal: { name: 'Minimal', query: 'minimal abstract geometric' },
  anime: { name: 'Anime', query: 'anime illustration art' }
};

export function getCategories() {
  return Object.entries(CATEGORIES).map(([id, data]) => ({
    id,
    name: data.name
  }));
}

// Unified wallpaper format
function normalizeWallpaper(raw, source) {
  return {
    id: `${source}_${raw.id}`,
    title: raw.title || raw.alt || raw.description || 'Wallpaper',
    preview_url: raw.preview_url,
    full_url: raw.full_url,
    source,
    photographer: raw.photographer || raw.user || 'Unknown',
    isPremium: false
  };
}

// Merge and shuffle results from multiple sources
function mergeResults(pexels, unsplash, wallhaven) {
  const all = [
    ...pexels.map(w => normalizeWallpaper(w, 'pexels')),
    ...unsplash.map(w => normalizeWallpaper(w, 'unsplash')),
    ...wallhaven.map(w => normalizeWallpaper(w, 'wallhaven'))
  ];
  
  // Shuffle to mix sources
  return all.sort(() => Math.random() - 0.5);
}

// Fetch with fallback
async function fetchWithFallback(pexelsFn, unsplashFn, wallhavenFn, ...args) {
  const results = await Promise.allSettled([
    pexelsFn(...args),
    unsplashFn(...args),
    wallhavenFn(...args)
  ]);

  const pexels = results[0].status === 'fulfilled' ? results[0].value : [];
  const unsplash = results[1].status === 'fulfilled' ? results[1].value : [];
  const wallhaven = results[2].status === 'fulfilled' ? results[2].value : [];

  if (pexels.length === 0 && unsplash.length === 0 && wallhaven.length === 0) {
    throw new Error('All sources failed');
  }

  return mergeResults(pexels, unsplash, wallhaven);
}

// GET trending wallpapers
export async function getTrending(page = 1, perPage = 30) {
  const cacheKey = `trending_${page}_${perPage}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const wallpapers = await fetchWithFallback(
    fetchPexelsTrending,
    fetchUnsplashTrending,
    fetchWallhavenTrending,
    page,
    perPage
  );

  cache.set(cacheKey, wallpapers);
  return wallpapers;
}

// Search wallpapers
export async function searchWallpapers(query, page = 1, perPage = 30) {
  const cacheKey = `search_${query}_${page}_${perPage}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const wallpapers = await fetchWithFallback(
    fetchPexelsSearch,
    fetchUnsplashSearch,
    fetchWallhavenSearch,
    query,
    page,
    perPage
  );

  cache.set(cacheKey, wallpapers);
  return wallpapers;
}

// Get wallpapers by category
export async function getByCategory(categoryName, page = 1, perPage = 30) {
  const category = CATEGORIES[categoryName.toLowerCase()];
  if (!category) {
    throw new Error(`Unknown category: ${categoryName}`);
  }

  const cacheKey = `category_${categoryName}_${page}_${perPage}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const wallpapers = await searchWallpapers(category.query, page, perPage);
  cache.set(cacheKey, wallpapers);
  return wallpapers;
}
