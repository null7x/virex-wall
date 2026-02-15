import axios from 'axios';
import { createHttpAgent } from '../utils/httpAgent.js';

const UNSPLASH_ACCESS_KEY = process.env.UNSPLASH_ACCESS_KEY;
const BASE_URL = 'https://api.unsplash.com';

const client = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  httpAgent: createHttpAgent(),
  headers: {
    'Authorization': `Client-ID ${UNSPLASH_ACCESS_KEY}`
  }
});

// Extract category from tags or title - comprehensive matching
function extractCategory(tags, title) {
  const keywords = {
    nature: ['nature', 'landscape', 'forest', 'mountain', 'lake', 'tree', 'valley', 'river', 'sunset', 'sunrise'],
    space: ['space', 'galaxy', 'stars', 'nebula', 'planet', 'cosmos', 'moon', 'universe'],
    city: ['city', 'urban', 'architecture', 'building', 'skyline', 'street', 'bridge', 'tower'],
    amoled: ['dark', 'black', 'night', 'shadow', 'moody'],
    ocean: ['ocean', 'sea', 'beach', 'water', 'wave', 'coast', 'underwater'],
    mountain: ['mountain', 'peak', 'alps', 'hill', 'cliff'],
    anime: ['anime', 'illustration', 'cartoon', 'digital art'],
    cars: ['car', 'vehicle', 'automotive', 'racing', 'supercar'],
    cyberpunk: ['cyberpunk', 'neon', 'futuristic', 'synthwave', 'glow'],
    fantasy: ['fantasy', 'magical', 'mythical', 'dragon', 'castle'],
    minimal: ['minimal', 'simple', 'clean', 'flat'],
    abstract: ['abstract', 'pattern', 'geometric', 'art', 'texture'],
    flowers: ['flower', 'floral', 'rose', 'plant', 'garden', 'bloom'],
    gradient: ['gradient', 'color', 'colorful', 'vibrant'],
    gaming: ['game', 'gaming', 'gamer', 'esports']
  };
  
  const searchText = [...tags, title].join(' ').toLowerCase();
  for (const [cat, kws] of Object.entries(keywords)) {
    if (kws.some(kw => searchText.includes(kw))) return cat;
  }
  return 'abstract';
}

function transformPhoto(photo) {
  const title = photo.description || photo.alt_description || 'Unsplash Wallpaper';
  const rawTags = photo.tags?.map(t => t.title) || [];
  const category = extractCategory(rawTags, title);
  // Always include tags for better categorization
  const tags = rawTags.length > 0 ? [...rawTags.slice(0, 8), category] : [category, 'abstract'];
  
  return {
    id: photo.id,
    title,
    url: photo.urls.full,
    thumbnail_url: photo.urls.small,
    preview_url: photo.urls.small,
    full_url: photo.urls.full,
    width: photo.width || 1080,
    height: photo.height || 1920,
    photographer: photo.user?.name || 'Unknown',
    tags
  };
}

export async function fetchUnsplashTrending(page = 1, perPage = 15) {
  if (!UNSPLASH_ACCESS_KEY) {
    console.warn('Unsplash API key not configured');
    return [];
  }

  try {
    const response = await client.get('/photos', {
      params: { page, per_page: perPage, order_by: 'popular' }
    });
    return response.data.map(transformPhoto);
  } catch (error) {
    console.error('Unsplash trending error:', error.message);
    return [];
  }
}

export async function fetchUnsplashSearch(query, page = 1, perPage = 15) {
  if (!UNSPLASH_ACCESS_KEY) {
    console.warn('Unsplash API key not configured');
    return [];
  }

  try {
    const response = await client.get('/search/photos', {
      params: { query, page, per_page: perPage, orientation: 'portrait' }
    });
    return response.data.results.map(transformPhoto);
  } catch (error) {
    console.error('Unsplash search error:', error.message);
    return [];
  }
}
