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

function transformPhoto(photo) {
  return {
    id: photo.id,
    title: photo.description || photo.alt_description || 'Unsplash Wallpaper',
    url: photo.urls.full,
    thumbnail_url: photo.urls.small,
    preview_url: photo.urls.small,
    full_url: photo.urls.full,
    width: photo.width || 1080,
    height: photo.height || 1920,
    photographer: photo.user?.name || 'Unknown',
    tags: photo.tags?.map(t => t.title) || []
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
