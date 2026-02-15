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

function transformPhoto(photo) {
  return {
    id: photo.id,
    title: photo.alt || 'Pexels Wallpaper',
    url: photo.src.original,
    thumbnail_url: photo.src.medium,
    preview_url: photo.src.medium,
    full_url: photo.src.original,
    width: photo.width || 1080,
    height: photo.height || 1920,
    photographer: photo.photographer,
    tags: []
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
