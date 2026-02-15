# VIREX Backend API

Production-ready backend for VIREX Wallpapers Android app.

## Features

- 🚀 Fast response with in-memory caching (10 min TTL)
- 🔒 Security: Helmet, CORS, Rate limiting
- 🌐 Multi-source: Pexels + Unsplash + Wallhaven
- ⚡ HTTP Keep-Alive for performance
- 🏓 Auto self-ping to prevent cold starts

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /health` | Health check |
| `GET /ping` | Ping endpoint |
| `GET /wallpapers/trending` | Trending wallpapers |
| `GET /wallpapers/search?q={query}` | Search wallpapers |
| `GET /wallpapers/categories` | List categories |
| `GET /wallpapers/category/{name}` | Get category wallpapers |

### Query Parameters

- `page` - Page number (default: 1)
- `per_page` - Items per page (default: 30)

### Categories

- nature
- cars
- amoled
- space
- cyberpunk
- minimal
- anime

## Response Format

```json
{
  "wallpapers": [
    {
      "id": "pexels_123456",
      "title": "Mountain Landscape",
      "preview_url": "https://...",
      "full_url": "https://...",
      "source": "pexels",
      "photographer": "John Doe",
      "isPremium": false
    }
  ],
  "page": 1,
  "per_page": 30
}
```

## Local Development

1. Clone repository
2. Copy environment file:
   ```bash
   cp .env.example .env
   ```
3. Add API keys to `.env`
4. Install dependencies:
   ```bash
   npm install
   ```
5. Start server:
   ```bash
   npm run dev
   ```

## Test with cURL

```bash
# Health check
curl http://localhost:3000/health

# Trending
curl http://localhost:3000/wallpapers/trending

# Search
curl "http://localhost:3000/wallpapers/search?q=nature"

# Category
curl http://localhost:3000/wallpapers/category/space

# Categories list
curl http://localhost:3000/wallpapers/categories
```

## Deploy to Koyeb (Free Tier)

### Option 1: Docker Deployment

1. Push to GitHub
2. Go to [Koyeb Dashboard](https://app.koyeb.com)
3. Create new app → GitHub
4. Select repository
5. Choose Dockerfile
6. Add environment variables:
   - `PEXELS_API_KEY`
   - `UNSPLASH_ACCESS_KEY`
   - `SELF_PING_URL` (set after deployment)
7. Deploy

### Option 2: Native Node.js

Build command:
```bash
npm ci --only=production
```

Run command:
```bash
node src/index.js
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `PORT` | Server port (auto-set by Koyeb) |
| `PEXELS_API_KEY` | Pexels API key |
| `UNSPLASH_ACCESS_KEY` | Unsplash access key |
| `SELF_PING_URL` | Full URL for anti-sleep ping |

## Get API Keys

- **Pexels**: https://www.pexels.com/api/
- **Unsplash**: https://unsplash.com/developers

## Performance Optimizations

- In-memory cache (10 minute TTL)
- HTTP Keep-Alive connections
- Response compression
- Parallel API fetching
- Self-ping every 4 minutes

## Android Integration

```kotlin
interface VirexApi {
    @GET("wallpapers/trending")
    suspend fun getTrending(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): WallpaperResponse

    @GET("wallpapers/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("wallpapers/category/{name}")
    suspend fun getCategory(
        @Path("name") category: String,
        @Query("page") page: Int = 1
    ): WallpaperResponse
}
```

Base URL: `https://virex-backend.koyeb.app/`

## License

MIT
