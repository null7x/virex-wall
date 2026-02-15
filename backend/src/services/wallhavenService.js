import axios from 'axios';
import { createHttpAgent } from '../utils/httpAgent.js';

const BASE_URL = 'https://wallhaven.cc/api/v1';

const client = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  httpAgent: createHttpAgent()
});

function transformWallpaper(wallpaper) {
  return {
    id: wallpaper.id,
    title: 'Wallhaven Wallpaper',
    preview_url: wallpaper.thumbs?.small || wallpaper.thumbs?.original,
    full_url: wallpaper.path,
    photographer: 'Wallhaven'
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
