import { fetchPexelsTrending, fetchPexelsSearch } from './pexelsService.js';
import { fetchUnsplashTrending, fetchUnsplashSearch } from './unsplashService.js';
import { fetchWallhavenTrending, fetchWallhavenSearch } from './wallhavenService.js';
import { fetchRedditTrending, fetchRedditSearch } from './redditService.js';
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
  anime: { name: 'Anime', query: 'anime illustration art', coverUrl: 'https://picsum.photos/id/21/400/600' },
  city: { name: 'City', query: 'city urban architecture skyline', coverUrl: 'https://picsum.photos/id/274/400/600' },
  ocean: { name: 'Ocean', query: 'ocean sea beach water waves', coverUrl: 'https://picsum.photos/id/14/400/600' },
  fantasy: { name: 'Fantasy', query: 'fantasy dragon magical mythical', coverUrl: 'https://picsum.photos/id/167/400/600' },
  gaming: { name: 'Gaming', query: 'gaming video game esports', coverUrl: 'https://picsum.photos/id/119/400/600' },
  dark: { name: 'Dark', query: 'dark aesthetic moody black', coverUrl: 'https://picsum.photos/id/1025/400/600' },
  abstract: { name: 'Abstract', query: 'abstract art geometric fluid', coverUrl: 'https://picsum.photos/id/1076/400/600' },
  gradient: { name: 'Gradient', query: 'gradient color smooth colorful', coverUrl: 'https://picsum.photos/id/669/400/600' },
  neon: { name: 'Neon', query: 'neon lights glow vibrant', coverUrl: 'https://picsum.photos/id/1068/400/600' },
  mountain: { name: 'Mountains', query: 'mountain peak alps landscape', coverUrl: 'https://picsum.photos/id/29/400/600' },
  skull: { name: 'Skull', query: 'skull skeleton dark art', coverUrl: 'https://picsum.photos/id/1078/400/600' },
  texture: { name: 'Texture', query: 'texture pattern surface material', coverUrl: 'https://picsum.photos/id/1044/400/600' },
  flowers: { name: 'Flowers', query: 'flower floral rose botanical', coverUrl: 'https://picsum.photos/id/152/400/600' },
  animals: { name: 'Animals', query: 'animal wildlife nature creature', coverUrl: 'https://picsum.photos/id/237/400/600' },
  ultra_4k: { name: '4K', query: '4k ultra hd high resolution', coverUrl: 'https://picsum.photos/id/1084/400/600' },
  ultra_8k: { name: '8K', query: '8k ultra quality sharp', coverUrl: 'https://picsum.photos/id/1080/400/600' },
  new: { name: 'New', query: 'trending popular modern latest', coverUrl: 'https://picsum.photos/id/1069/400/600' }
};

// Cache for category counts
const categoryCounts = new Map();

// Get categories with real wallpaper counts
export async function getCategories() {
  const cacheKey = 'categories_with_counts';
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  // Fetch counts in parallel for all categories
  const entries = Object.entries(CATEGORIES);
  const countPromises = entries.map(async ([id, data]) => {
    // Check if we have cached count for this category
    if (categoryCounts.has(id)) {
      return { id, count: categoryCounts.get(id) };
    }
    
    try {
      // Quick fetch to get count (only need 1 page)
      const wallpapers = await searchWallpapers(data.query, 1, 50);
      const count = wallpapers.length;
      categoryCounts.set(id, count);
      return { id, count };
    } catch {
      return { id, count: 0 };
    }
  });

  const counts = await Promise.all(countPromises);
  const countMap = Object.fromEntries(counts.map(c => [c.id, c.count]));

  const result = entries.map(([id, data]) => ({
    id,
    name: data.name,
    coverUrl: proxyUrl(data.coverUrl),
    count: countMap[id] || 0
  }));

  cache.set(cacheKey, result, 300); // Cache for 5 minutes
  return result;
}

// Backend base URL for image proxy
const BACKEND_URL = process.env.BACKEND_URL || 'https://backend-tau-orcin-14.vercel.app';

// Proxy image URL through our backend to bypass geo-blocks in Russia
function proxyUrl(originalUrl) {
  if (!originalUrl) return originalUrl;
  return `${BACKEND_URL}/proxy/image?url=${encodeURIComponent(originalUrl)}`;
}

// Unified wallpaper format - matches Android VirexWallpaper model
function normalizeWallpaper(raw, source) {
  const originalUrl = raw.full_url || raw.url;
  const thumbnailUrl = raw.preview_url || raw.thumbnail_url;
  
  return {
    id: `${source}_${raw.id}`,
    title: raw.title || raw.alt || raw.description || 'Wallpaper',
    url: proxyUrl(originalUrl),
    thumbnail_url: proxyUrl(thumbnailUrl),
    original_url: originalUrl,
    original_thumbnail_url: thumbnailUrl,
    width: raw.width || 1080,
    height: raw.height || 1920,
    source,
    photographer: raw.photographer || raw.user || 'Unknown',
    tags: raw.tags || []
  };
}

// Merge and shuffle results from multiple sources
function mergeResults(pexels, unsplash, wallhaven, reddit) {
  const all = [
    ...pexels.map(w => normalizeWallpaper(w, 'pexels')),
    ...unsplash.map(w => normalizeWallpaper(w, 'unsplash')),
    ...wallhaven.map(w => normalizeWallpaper(w, 'wallhaven')),
    ...reddit.map(w => normalizeWallpaper(w, 'reddit'))
  ];
  
  // Shuffle to mix sources
  return all.sort(() => Math.random() - 0.5);
}

// Fetch with fallback
async function fetchWithFallback(pexelsFn, unsplashFn, wallhavenFn, redditFn, ...args) {
  const results = await Promise.allSettled([
    pexelsFn(...args),
    unsplashFn(...args),
    wallhavenFn(...args),
    redditFn(...args)
  ]);

  const pexels = results[0].status === 'fulfilled' ? results[0].value : [];
  const unsplash = results[1].status === 'fulfilled' ? results[1].value : [];
  const wallhaven = results[2].status === 'fulfilled' ? results[2].value : [];
  const reddit = results[3].status === 'fulfilled' ? results[3].value : [];

  console.log(`Sources: Pexels=${pexels.length}, Unsplash=${unsplash.length}, Wallhaven=${wallhaven.length}, Reddit=${reddit.length}`);

  if (pexels.length === 0 && unsplash.length === 0 && wallhaven.length === 0 && reddit.length === 0) {
    // Use Picsum as fallback when all API sources fail
    console.log('All API sources failed, using Picsum fallback');
    return null; // Will trigger fallback
  }

  return mergeResults(pexels, unsplash, wallhaven, reddit);
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
      fetchRedditTrending,
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
      fetchRedditSearch,
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
