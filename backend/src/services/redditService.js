import axios from 'axios';

// Reddit wallpaper subreddits with category mapping - improved coverage
const SUBREDDITS = {
  'wallpapers': 'abstract',
  'wallpaper': 'abstract', 
  'AMOLEDBACKGROUNDS': 'amoled',
  'Amoledbackgrounds': 'amoled',
  'phonewallpapers': 'abstract',
  'MobileWallpaper': 'abstract',
  'EarthPorn': 'nature',
  'NaturePics': 'nature',
  'spaceporn': 'space',
  'CityPorn': 'city',
  'AnimeWallpaper': 'anime',
  'Art': 'abstract',
  'Cyberpunk': 'cyberpunk',
  'cyberpunkgame': 'cyberpunk',
  'carporn': 'cars',
  'ImaginaryLandscapes': 'fantasy',
  'ImaginaryMindscapes': 'fantasy',
  'DigitalArt': 'abstract',
  'MinimalWallpaper': 'minimal',
  'MountainPics': 'mountain',
  'seaporn': 'ocean',
  'waterporn': 'ocean'
};

// User agent required by Reddit API
const USER_AGENT = 'VIREX-Wallpapers/1.0';

// Fetch posts from a subreddit
async function fetchSubreddit(subreddit, category, limit = 25, after = null) {
  try {
    const url = `https://www.reddit.com/r/${subreddit}/hot.json`;
    const params = { limit, raw_json: 1 };
    if (after) params.after = after;

    const response = await axios.get(url, {
      headers: { 'User-Agent': USER_AGENT },
      params,
      timeout: 10000
    });

    const posts = response.data?.data?.children || [];
    
    return posts
      .filter(post => {
        const data = post.data;
        // Only image posts
        if (!data.url) return false;
        const url = data.url.toLowerCase();
        return url.endsWith('.jpg') || 
               url.endsWith('.jpeg') || 
               url.endsWith('.png') ||
               url.includes('i.redd.it') ||
               url.includes('i.imgur.com');
      })
      .map(post => {
        const data = post.data;
        let imageUrl = data.url;
        
        // Fix imgur links
        if (imageUrl.includes('imgur.com') && !imageUrl.includes('i.imgur.com')) {
          imageUrl = imageUrl.replace('imgur.com', 'i.imgur.com');
          if (!imageUrl.match(/\.(jpg|jpeg|png|gif)$/i)) {
            imageUrl += '.jpg';
          }
        }

        // Get preview for thumbnail
        let thumbnailUrl = imageUrl;
        if (data.preview?.images?.[0]?.resolutions) {
          const resolutions = data.preview.images[0].resolutions;
          const medium = resolutions.find(r => r.width >= 320) || resolutions[resolutions.length - 1];
          if (medium) {
            thumbnailUrl = medium.url.replace(/&amp;/g, '&');
          }
        }

        return {
          id: data.id,
          title: data.title || 'Reddit Wallpaper',
          url: imageUrl,
          full_url: imageUrl,
          preview_url: thumbnailUrl,
          thumbnail_url: thumbnailUrl,
          width: data.preview?.images?.[0]?.source?.width || 1920,
          height: data.preview?.images?.[0]?.source?.height || 1080,
          photographer: `u/${data.author}`,
          user: data.author,
          tags: [category, subreddit, 'reddit'],
          source: 'reddit'
        };
      });
  } catch (error) {
    console.error(`Reddit ${subreddit} error:`, error.message);
    return [];
  }
}

// Fetch trending from multiple subreddits
export async function fetchRedditTrending(page = 1, perPage = 30) {
  const subredditList = Object.entries(SUBREDDITS);
  const postsPerSub = Math.ceil(perPage / subredditList.length);
  
  const results = await Promise.allSettled(
    subredditList.map(([sub, cat]) => fetchSubreddit(sub, cat, postsPerSub))
  );

  const allPosts = results
    .filter(r => r.status === 'fulfilled')
    .flatMap(r => r.value);

  // Shuffle and limit
  return allPosts
    .sort(() => Math.random() - 0.5)
    .slice(0, perPage);
}

// Determine category from query
function categoryFromQuery(query) {
  const q = query.toLowerCase();
  const mapping = {
    nature: ['nature', 'landscape', 'forest', 'mountain'],
    space: ['space', 'galaxy', 'stars', 'nebula'],
    city: ['city', 'urban', 'architecture'],
    amoled: ['dark', 'black', 'amoled', 'minimal'],
    anime: ['anime', 'manga', 'illustration'],
    cars: ['car', 'automotive', 'vehicle'],
    cyberpunk: ['cyberpunk', 'neon', 'futuristic'],
    fantasy: ['fantasy', 'dragon', 'magical'],
    ocean: ['ocean', 'sea', 'beach', 'water'],
    minimal: ['minimal', 'simple', 'abstract']
  };
  for (const [cat, keywords] of Object.entries(mapping)) {
    if (keywords.some(kw => q.includes(kw))) return cat;
  }
  return 'general';
}

// Search Reddit for wallpapers
export async function fetchRedditSearch(query, page = 1, perPage = 30) {
  const category = categoryFromQuery(query);
  
  try {
    const url = `https://www.reddit.com/r/wallpapers+wallpaper+AMOLEDBACKGROUNDS/search.json`;
    const response = await axios.get(url, {
      headers: { 'User-Agent': USER_AGENT },
      params: {
        q: query,
        restrict_sr: true,
        sort: 'relevance',
        limit: perPage,
        raw_json: 1
      },
      timeout: 10000
    });

    const posts = response.data?.data?.children || [];
    
    return posts
      .filter(post => {
        const data = post.data;
        if (!data.url) return false;
        const url = data.url.toLowerCase();
        return url.endsWith('.jpg') || 
               url.endsWith('.jpeg') || 
               url.endsWith('.png') ||
               url.includes('i.redd.it') ||
               url.includes('i.imgur.com');
      })
      .map(post => {
        const data = post.data;
        let imageUrl = data.url;
        
        if (imageUrl.includes('imgur.com') && !imageUrl.includes('i.imgur.com')) {
          imageUrl = imageUrl.replace('imgur.com', 'i.imgur.com');
          if (!imageUrl.match(/\.(jpg|jpeg|png|gif)$/i)) {
            imageUrl += '.jpg';
          }
        }

        let thumbnailUrl = imageUrl;
        if (data.preview?.images?.[0]?.resolutions) {
          const resolutions = data.preview.images[0].resolutions;
          const medium = resolutions.find(r => r.width >= 320) || resolutions[resolutions.length - 1];
          if (medium) {
            thumbnailUrl = medium.url.replace(/&amp;/g, '&');
          }
        }

        return {
          id: data.id,
          title: data.title || 'Reddit Wallpaper',
          url: imageUrl,
          full_url: imageUrl,
          preview_url: thumbnailUrl,
          thumbnail_url: thumbnailUrl,
          width: data.preview?.images?.[0]?.source?.width || 1920,
          height: data.preview?.images?.[0]?.source?.height || 1080,
          photographer: `u/${data.author}`,
          user: data.author,
          tags: [category, data.subreddit, 'reddit'],
          source: 'reddit'
        };
      });
  } catch (error) {
    console.error('Reddit search error:', error.message);
    return [];
  }
}
