import { fetchPexelsTrending, fetchPexelsSearch } from './pexelsService.js';
import { fetchUnsplashTrending, fetchUnsplashSearch } from './unsplashService.js';
import { fetchWallhavenTrending, fetchWallhavenSearch } from './wallhavenService.js';
import { getFallbackTrending, getFallbackSearch, getFallbackByCategory } from './fallbackService.js';
import cache from '../cache/memoryCache.js';

// Category mappings for search queries
const CATEGORIES = {
  nature: { name: 'Nature', query: 'nature landscape forest', coverUrl: 'https://picsum.photos/id/10/400/600' },
  cars: { name: 'Cars', query: 'cars automotive supercar', coverUrl: 'https://picsum.photos/id/111/400/600' },
  amoled: { name: 'AMOLED', query: 'dark black minimal amoled', coverUrl: 'https://picsum.photos/id/36/400/600' },
  space: { name: 'Space', query: 'space galaxy nebula stars', coverUrl: 'https://picsum.photos/id/96/400/600' },
  cyberpunk: { name: 'Cyberpunk', query: 'cyberpunk neon city futuristic', coverUrl: 'https://picsum.photos/id/274/400/600' },
  minimal: { name: 'Minimal', query: 'minimal abstract geometric', coverUrl: 'https://picsum.photos/id/35/400/600' },
  anime: { name: 'Anime', query: 'anime illustration art', coverUrl: 'https://picsum.photos/id/21/400/600' }
};

export function getCategories() {
  return Object.entries(CATEGORIES).map(([id, data]) => ({
    id,
    name: data.name,
    coverUrl: data.coverUrl,
    count: 100
  }));
}

// Unified wallpaper format - matches Android VirexWallpaper model
function normalizeWallpaper(raw, source) {
  return {
    id: `${source}_${raw.id}`,
    title: raw.title || raw.alt || raw.description || 'Wallpaper',
    url: raw.full_url || raw.url,
    thumbnail_url: raw.preview_url || raw.thumbnail_url,
    width: raw.width || 1080,
    height: raw.height || 1920,
    source,
    photographer: raw.photographer || raw.user || 'Unknown',
    tags: raw.tags || []
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
    // Use Picsum as fallback when all API sources fail
    console.log('All API sources failed, using Picsum fallback');
    return null; // Will trigger fallback
  }

  return mergeResults(pexels, unsplash, wallhaven);
}

// GET trending wallpapers
export async function getTrending(page = 1, perPage = 30) {
  const cacheKey = `trending_${page}_${perPage}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const wallpapers = await fetchWithFallback(
      fetchPexelsTrending,
      fetchUnsplashTrending,
      fetchWallhavenTrending,
      page,
      perPage
    );
    
    if (wallpapers && wallpapers.length > 0) {
      cache.set(cacheKey, wallpapers);
      return wallpapers;
    }
  } catch (error) {
    console.error('Trending fetch error:', error.message);
  }
  
  // Fallback to Picsum
  const fallback = getFallbackTrending(page, perPage);
  cache.set(cacheKey, fallback);
  return fallback;
}

// Search wallpapers
export async function searchWallpapers(query, page = 1, perPage = 30) {
  const cacheKey = `search_${query}_${page}_${perPage}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const wallpapers = await fetchWithFallback(
      fetchPexelsSearch,
      fetchUnsplashSearch,
      fetchWallhavenSearch,
      query,
      page,
      perPage
    );
    
    if (wallpapers && wallpapers.length > 0) {
      cache.set(cacheKey, wallpapers);
      return wallpapers;
    }
  } catch (error) {
    console.error('Search fetch error:', error.message);
  }
  
  // Fallback to Picsum
  const fallback = getFallbackSearch(query, page, perPage);
  cache.set(cacheKey, fallback);
  return fallback;
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
