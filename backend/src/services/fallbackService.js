/**
 * Fallback service - returns mock wallpapers when API keys are not configured
 * Uses free sources that don't require API keys
 */

// Picsum.photos - completely free, no API key needed
const PICSUM_BASE = 'https://picsum.photos';

// Backend URL for proxying images
const BACKEND_URL = process.env.BACKEND_URL || 'https://backend-tau-orcin-14.vercel.app';

// Proxy URL through our backend to bypass geo-blocks
function proxyUrl(originalUrl) {
  if (!originalUrl) return originalUrl;
  return `${BACKEND_URL}/proxy/image?url=${encodeURIComponent(originalUrl)}`;
}

// Generate random wallpaper IDs
function generateIds(count = 30, seed = 0) {
  const ids = [];
  for (let i = 0; i < count; i++) {
    ids.push(seed * 100 + i + 1);
  }
  return ids;
}

// Nature-themed wallpapers (Picsum IDs that look like nature)
const NATURE_IDS = [10, 11, 14, 15, 27, 28, 29, 37, 47, 110, 116, 129, 142, 147, 167, 176, 193, 200, 219, 235];
const CITY_IDS = [274, 275, 276, 277, 312, 336, 359, 367, 374, 384, 402, 416, 445, 447, 479, 491, 493, 513, 524, 534];
const ABSTRACT_IDS = [5, 21, 35, 36, 45, 56, 67, 95, 106, 119, 153, 166, 179, 189, 199, 209, 289, 299, 309, 319];

function createWallpaper(id, category = 'general', source = 'picsum') {
  const width = 1080;
  const height = 1920;
  
  const fullUrl = `${PICSUM_BASE}/id/${id}/${width}/${height}.jpg`;
  const thumbUrl = `${PICSUM_BASE}/id/${id}/400/600.jpg`;
  
  return {
    id: `${source}_${id}`,
    title: `Wallpaper #${id}`,
    url: proxyUrl(fullUrl),
    thumbnail_url: proxyUrl(thumbUrl),
    original_url: fullUrl,
    original_thumbnail_url: thumbUrl,
    width,
    height,
    source,
    photographer: 'Picsum',
    photographer_url: null,
    color: null,
    tags: [category]
  };
}

export function getFallbackTrending(page = 1, perPage = 30) {
  const start = (page - 1) * perPage;
  const ids = generateIds(200);
  
  return ids.slice(start, start + perPage).map(id => createWallpaper(id, 'general'));
}

export function getFallbackSearch(query, page = 1, perPage = 30) {
  const q = query.toLowerCase();
  let ids;
  let category = 'general';
  
  if (q.includes('nature') || q.includes('landscape') || q.includes('forest')) {
    ids = NATURE_IDS;
    category = 'nature';
  } else if (q.includes('city') || q.includes('urban') || q.includes('architecture')) {
    ids = CITY_IDS;
    category = 'city';
  } else if (q.includes('space') || q.includes('galaxy')) {
    ids = ABSTRACT_IDS;
    category = 'space';
  } else if (q.includes('dark') || q.includes('amoled') || q.includes('black')) {
    ids = ABSTRACT_IDS;
    category = 'amoled';
  } else if (q.includes('anime') || q.includes('art')) {
    ids = ABSTRACT_IDS;
    category = 'anime';
  } else if (q.includes('minimal') || q.includes('abstract')) {
    ids = ABSTRACT_IDS;
    category = 'minimal';
  } else {
    ids = ABSTRACT_IDS;
  }
  
  // Add more random IDs
  const allIds = [...ids, ...generateIds(100, Math.abs(hashCode(query)) % 10)];
  const start = (page - 1) * perPage;
  
  return allIds.slice(start, start + perPage).map(id => createWallpaper(id, category));
}

export function getFallbackByCategory(category, page = 1, perPage = 30) {
  return getFallbackSearch(category, page, perPage);
}

// Simple hash function for query-based seeding
function hashCode(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return hash;
}
