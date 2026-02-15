import axios from 'axios';
import { createHttpAgent } from '../utils/httpAgent.js';

const PEXELS_API_KEY = process.env.PEXELS_API_KEY;
const BASE_URL = 'https://api.pexels.com/v1';

const client = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  httpAgent: createHttpAgent(),
  headers: {
    'Authorization': PEXELS_API_KEY
  }
});

// Extract tags from title/alt text - comprehensive keyword matching
function extractTags(text) {
  if (!text) return ['abstract'];
  const keywords = [
    // Nature
    'nature', 'landscape', 'forest', 'mountain', 'ocean', 'sea', 'beach', 'tree', 'water', 'lake', 'river', 'hill', 'valley', 'wave', 'sunset', 'sunrise', 'sky', 'clouds',
    // Urban
    'city', 'urban', 'architecture', 'building', 'street', 'skyline', 'bridge', 'tower',
    // Space
    'space', 'galaxy', 'stars', 'nebula', 'planet', 'cosmos', 'moon', 'universe',
    // Art styles
    'abstract', 'pattern', 'geometric', 'minimal', 'simple', 'art', 'texture', 'gradient',
    // Dark/AMOLED
    'dark', 'black', 'amoled', 'night', 'moody', 'shadow',
    // Animation/Gaming
    'anime', 'illustration', 'digital', 'cartoon', 'game', 'gaming',
    // Vehicles
    'car', 'vehicle', 'automotive', 'racing', 'supercar',
    // Sci-fi
    'cyberpunk', 'neon', 'futuristic', 'sci-fi', 'glow',
    // Fantasy
    'fantasy', 'dragon', 'magical', 'mythical',
    // Plants
    'flower', 'floral', 'plant', 'garden', 'rose', 'leaf',
    // Animals
    'animal', 'wildlife', 'cat', 'dog', 'bird'
  ];
  const lowerText = text.toLowerCase();
  const found = keywords.filter(kw => lowerText.includes(kw));
  // Always return at least one tag
  return found.length > 0 ? found : ['abstract'];
}

function transformPhoto(photo) {
  const title = photo.alt || 'Pexels Wallpaper';
  const tags = extractTags(title);
  
  return {
    id: photo.id,
    title,
    url: photo.src.original,
    thumbnail_url: photo.src.medium,
    preview_url: photo.src.medium,
    full_url: photo.src.original,
    width: photo.width || 1080,
    height: photo.height || 1920,
    photographer: photo.photographer,
    tags
  };
}

export async function fetchPexelsTrending(page = 1, perPage = 15) {
  if (!PEXELS_API_KEY) {
    console.warn('Pexels API key not configured');
    return [];
  }

  try {
    const response = await client.get('/curated', {
      params: { page, per_page: perPage }
    });
    return response.data.photos.map(transformPhoto);
  } catch (error) {
    console.error('Pexels trending error:', error.message);
    return [];
  }
}

export async function fetchPexelsSearch(query, page = 1, perPage = 15) {
  if (!PEXELS_API_KEY) {
    console.warn('Pexels API key not configured');
    return [];
  }

  try {
    const response = await client.get('/search', {
      params: { query, page, per_page: perPage, orientation: 'portrait' }
    });
    return response.data.photos.map(transformPhoto);
  } catch (error) {
    console.error('Pexels search error:', error.message);
    return [];
  }
}
