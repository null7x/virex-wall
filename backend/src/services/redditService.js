import axios from 'axios';

// Reddit wallpaper subreddits
const SUBREDDITS = [
  'wallpapers',
  'wallpaper', 
  'AMOLEDBACKGROUNDS',
  'phonewallpapers',
  'MobileWallpaper'
];

// User agent required by Reddit API
const USER_AGENT = 'VIREX-Wallpapers/1.0';

// Fetch posts from a subreddit
async function fetchSubreddit(subreddit, limit = 25, after = null) {
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
          tags: [subreddit, 'reddit'],
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
  const postsPerSub = Math.ceil(perPage / SUBREDDITS.length);
  
  const results = await Promise.allSettled(
    SUBREDDITS.map(sub => fetchSubreddit(sub, postsPerSub))
  );

  const allPosts = results
    .filter(r => r.status === 'fulfilled')
    .flatMap(r => r.value);

  // Shuffle and limit
  return allPosts
    .sort(() => Math.random() - 0.5)
    .slice(0, perPage);
}

// Search Reddit for wallpapers
export async function fetchRedditSearch(query, page = 1, perPage = 30) {
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
          tags: [data.subreddit, 'reddit', ...query.split(' ')],
          source: 'reddit'
        };
      });
  } catch (error) {
    console.error('Reddit search error:', error.message);
    return [];
  }
}
