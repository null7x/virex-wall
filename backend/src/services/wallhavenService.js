import axios from 'axios';
import { createHttpAgent } from '../utils/httpAgent.js';

const BASE_URL = 'https://wallhaven.cc/api/v1';

const client = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  httpAgent: createHttpAgent()
});

// Category keywords for tag extraction - comprehensive
const CATEGORY_KEYWORDS = {
  nature: ['nature', 'landscape', 'forest', 'mountain', 'lake', 'tree', 'valley', 'river', 'sunset', 'sunrise'],
  space: ['space', 'galaxy', 'stars', 'nebula', 'planet', 'cosmos', 'moon', 'universe', 'astronomical'],
  anime: ['anime', 'manga', 'illustration', 'digital art', 'cartoon', 'japanese'],
  cyberpunk: ['cyberpunk', 'neon', 'futuristic', 'sci-fi', 'synthwave', 'retro future'],
  city: ['city', 'urban', 'architecture', 'building', 'skyline', 'street', 'metro', 'bridge'],
  amoled: ['dark', 'black', 'amoled', 'minimal dark', 'night', 'shadow', 'moody'],
  cars: ['car', 'vehicle', 'automotive', 'racing', 'supercar', 'sports car', 'motorcycle'],
  fantasy: ['fantasy', 'dragon', 'magical', 'mythical', 'castle', 'medieval', 'sword'],
  minimal: ['minimal', 'simple', 'clean', 'abstract', 'geometric', 'flat'],
  ocean: ['ocean', 'sea', 'beach', 'water', 'wave', 'underwater', 'coast'],
  mountain: ['mountain', 'peak', 'alps', 'hill', 'cliff', 'rocky'],
  flowers: ['flower', 'floral', 'rose', 'plant', 'garden', 'bloom'],
  gradient: ['gradient', 'color', 'colorful', 'vibrant'],
  gaming: ['game', 'gaming', 'video game', 'esports', 'gamer'],
  texture: ['texture', 'material', 'surface', 'pattern']
};

function categorizeByTags(tags) {
  const tagStr = tags.join(' ').toLowerCase();
  for (const [category, keywords] of Object.entries(CATEGORY_KEYWORDS)) {
    if (keywords.some(kw => tagStr.includes(kw))) {
      return category;
    }
  }
  return 'abstract';
}

function transformWallpaper(wallpaper) {
  const rawTags = wallpaper.tags?.map(t => t.name) || [];
  const category = categorizeByTags(rawTags);
  // Include raw tags + detected category for better categorization
  const tags = rawTags.length > 0 
    ? [...rawTags.slice(0, 8), category] 
    : [category, 'abstract'];
  
  return {
    id: wallpaper.id,
    title: rawTags[0] || 'Wallhaven Wallpaper',
    url: wallpaper.path,
    thumbnail_url: wallpaper.thumbs?.small || wallpaper.thumbs?.original,
    preview_url: wallpaper.thumbs?.small || wallpaper.thumbs?.original,
    full_url: wallpaper.path,
    width: wallpaper.dimension_x || 1080,
    height: wallpaper.dimension_y || 1920,
    photographer: 'Wallhaven',
    tags
  };
}

export async function fetchWallhavenTrending(page = 1, perPage = 15) {
  try {
    const response = await client.get('/search', {
      params: { 
        sorting: 'toplist',
        page,
        purity: '100', // SFW only
        categories: '111',
        ratios: 'portrait'
      }
    });
    return response.data.data.slice(0, perPage).map(transformWallpaper);
  } catch (error) {
    console.error('Wallhaven trending error:', error.message);
    return [];
  }
}

export async function fetchWallhavenSearch(query, page = 1, perPage = 15) {
  try {
    const response = await client.get('/search', {
      params: { 
        q: query,
        page,
        purity: '100', // SFW only
        categories: '111',
        ratios: 'portrait'
      }
    });
    return response.data.data.slice(0, perPage).map(transformWallpaper);
  } catch (error) {
    console.error('Wallhaven search error:', error.message);
    return [];
  }
}
