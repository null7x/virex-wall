# Automatic Wallpaper Sync Feature

## Overview

This feature automatically syncs wallpapers from **FREE** and **LEGAL** sources (Unsplash and Pexels APIs).

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         VirexApp                                 │
│  (Schedules periodic sync on app start)                         │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    WallpaperSyncWorker                          │
│  (WorkManager - runs daily on WiFi, battery not low)            │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                  WallpaperSyncRepository                        │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│  │  UnsplashApi   │  │   PexelsApi    │  │ SyncedWallpaper  │  │
│  │  (50 req/hr)   │  │ (200 req/hr)   │  │      DAO         │  │
│  └────────────────┘  └────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Room Database                               │
│  synced_wallpapers (category, source, viewed, cached)           │
│  sync_status (lastSyncAt, totalSynced, error)                   │
└─────────────────────────────────────────────────────────────────┘
```

## Features

### ✅ Automatic Background Sync

- Runs once every 24 hours
- Only on WiFi to save mobile data
- Only when battery is not low
- Continues even after app is closed

### ✅ Multiple Free Sources

- **Unsplash**: 50 requests/hour (demo), apply for production tier
- **Pexels**: 200 requests/hour, no attribution required

### ✅ Smart Categorization

- **AMOLED**: Pure black wallpapers
- **DARK**: Dark aesthetic wallpapers
- **MINIMAL**: Minimalist designs
- **NEW**: Recently synced wallpapers

### ✅ Deduplication

- Prevents duplicate wallpapers from being stored
- Uses source ID to identify unique wallpapers

### ✅ Graceful Fallback

- Works if only one API key is configured
- Shows empty state with manual sync button
- Retry with exponential backoff on failure

### ✅ Attribution

- Displays photographer name
- Links to source page (Unsplash/Pexels)
- Tracks downloads for Unsplash (required by their API terms)

## Files Created

### API Layer

- `data/remote/api/UnsplashApi.kt` - Unsplash Retrofit interface
- `data/remote/api/PexelsApi.kt` - Pexels Retrofit interface
- `data/remote/model/UnsplashModels.kt` - Data classes for Unsplash responses
- `data/remote/model/PexelsModels.kt` - Data classes for Pexels responses

### Data Layer

- `data/model/SyncedWallpaper.kt` - Room entity for synced wallpapers
- `data/local/SyncedWallpaperDao.kt` - DAO for database operations
- `data/repository/WallpaperSyncRepository.kt` - Main sync logic

### Sync Layer

- `sync/WallpaperSyncWorker.kt` - WorkManager worker for background sync

### DI Layer

- `di/NetworkModule.kt` - Retrofit and OkHttp providers
- `di/SyncModule.kt` - Sync repository provider

### UI Layer

- `ui/sync/SyncViewModel.kt` - ViewModel for sync screen
- `ui/sync/SyncScreen.kt` - Main sync wallpapers screen
- `ui/sync/SyncedWallpaperDetailScreen.kt` - Wallpaper detail/preview screen

## Setup API Keys

1. Get free API keys:
   - **Unsplash**: https://unsplash.com/developers
   - **Pexels**: https://www.pexels.com/api/

2. Add to `local.properties`:

```properties
UNSPLASH_ACCESS_KEY=your_unsplash_access_key
PEXELS_API_KEY=your_pexels_api_key
```

3. Keys are accessed via `BuildConfig` and never exposed in the app.

## API Response Examples

### Unsplash Search Response

```json
{
  "total": 10000,
  "total_pages": 334,
  "results": [
    {
      "id": "LBI7cgq3pbM",
      "width": 5000,
      "height": 3333,
      "color": "#0d0c0c",
      "blur_hash": "LSC%a}-;M{RP~qxaxaof4nNGj[j@",
      "alt_description": "Dark minimal abstract",
      "urls": {
        "raw": "https://images.unsplash.com/photo-abc123?ixid=...",
        "full": "https://images.unsplash.com/photo-abc123?q=80",
        "regular": "https://images.unsplash.com/photo-abc123?w=1080",
        "small": "https://images.unsplash.com/photo-abc123?w=400",
        "thumb": "https://images.unsplash.com/photo-abc123?w=200"
      },
      "user": {
        "id": "pXhwzz1JtQU",
        "username": "photographer123",
        "name": "John Doe"
      },
      "likes": 1234
    }
  ]
}
```

### Pexels Search Response

```json
{
  "page": 1,
  "per_page": 30,
  "total_results": 8000,
  "photos": [
    {
      "id": 1234567,
      "width": 3648,
      "height": 5472,
      "url": "https://www.pexels.com/photo/1234567/",
      "photographer": "Jane Smith",
      "photographer_url": "https://www.pexels.com/@janesmith",
      "avg_color": "#0A0A0A",
      "src": {
        "original": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg",
        "large2x": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?w=940",
        "portrait": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?fit=crop&h=1200&w=800"
      },
      "alt": "Dark abstract wallpaper"
    }
  ]
}
```

## Sync Logic

1. **Initialization**
   - Create sync status record if doesn't exist
   - Mark sync as in-progress

2. **For each category (AMOLED, DARK, MINIMAL, NEW)**:
   - Pick random search term from category
   - Query Unsplash API
   - Query Pexels API
   - Rate limiting delay between requests

3. **Deduplication**
   - Get existing source IDs from database
   - Filter out already stored wallpapers
   - Use source ID + source type as unique key

4. **Storage**
   - Insert new wallpapers to Room database
   - Update sync status (timestamp, count)

5. **Cleanup**
   - Delete wallpapers older than 30 days (if not cached)

## Rate Limits

| API      | Free Tier  | Requests/Hour | Requests/Month |
| -------- | ---------- | ------------- | -------------- |
| Unsplash | Demo       | 50            | -              |
| Unsplash | Production | 5,000         | -              |
| Pexels   | Free       | 200           | 20,000         |

## Compliance

### Unsplash

- ✅ Attribution displayed (photographer name + "on Unsplash")
- ✅ Download tracking when user sets wallpaper
- ✅ Link to source photo page

### Pexels

- ✅ No attribution required (but appreciated)
- ✅ Free for commercial use
- ✅ Link to source photo page

### Google Play Policy

- ✅ No scraping of websites
- ✅ No copyrighted content
- ✅ All images are free and legally licensed
- ✅ API keys stored securely in BuildConfig
