promt 1

You are a senior backend architect.

Create a production-ready backend for my Android wallpaper app VIREX.

GOAL:
The mobile app must NOT call external wallpaper APIs directly.
All requests must go through this backend so the app works in Russia without VPN.

TECH STACK:
- Node.js 20
- Express
- Axios
- Redis (optional cache)
- Deploy target: KOYEB
- Use environment variables
- REST API

MAIN FEATURES:

1) /wallpapers/trending
Fetch trending wallpapers from:
- Pexels API
- Unsplash API
Return unified JSON.

2) /wallpapers/search?q=
Search wallpapers from both sources.

3) /wallpapers/categories
Return predefined categories:
Nature
Cars
AMOLED
Space
Cyberpunk
Minimal
Anime

Each category must fetch real images from APIs.

4) Image optimization
Return:
- preview (small)
- full (original)

5) Smart caching
Cache responses for 10 minutes to reduce API usage and increase speed.

6) Region-safe access
The mobile app must call ONLY this backend.
All external API calls must happen server-side.

7) Performance
Use:
- compression
- keep-alive
- timeout handling

8) Security
- Rate limiting
- Helmet
- CORS (allow only my app)

9) Config
Use .env for:
PEXELS_API_KEY
UNSPLASH_ACCESS_KEY

10) Android-ready response format:
{
  id,
  title,
  preview_url,
  full_url,
  source,
  photographer
}

DEPLOYMENT ON KOYEB:

Provide:

1) Dockerfile
2) koyeb.yaml
3) Start command
4) Port config

The app must run on:
process.env.PORT

11) Provide full folder structure

12) Provide curl test examples

13) Provide free tier optimized settings for Koyeb

IMPORTANT:
The backend must be fast, lightweight and cold-start optimized.

OUTPUT:
- Full working code
- Deployment steps for Koyeb
- How to connect Android app to this backend



promt 2
You are a senior Node.js backend engineer.

Create a production-ready backend for my Android wallpaper app VIREX.

IMPORTANT:
Do NOT use Docker.
The project must be deployable on Koyeb using native Node.js build.

STACK:
Node.js 20
Express
Axios
Compression
Helmet
CORS
Rate limit

GOAL:
The Android app must call ONLY this backend.
The backend will fetch wallpapers from external APIs so the app works in Russia without VPN.

=================================

API FEATURES:

1) GET /wallpapers/trending
Fetch from:
- Pexels
- Unsplash

2) GET /wallpapers/search?q=

3) GET /wallpapers/category/:name

Categories:
Nature
Cars
AMOLED
Space
Cyberpunk
Minimal
Anime

Return unified format:

{
  id,
  title,
  preview_url,
  full_url,
  source,
  photographer
}

=================================

PERFORMANCE:

- Response caching (in-memory, no Redis)
- Cache time: 10 minutes
- Axios timeout
- Keep-alive agent

=================================

SECURITY:

- Helmet
- Rate limit
- CORS (configurable)

=================================

ENV VARIABLES:

PEXELS_API_KEY=
UNSPLASH_ACCESS_KEY=
PORT=

=================================

PROJECT STRUCTURE:

Provide full clean structure.

=================================

KOYEB DEPLOY (NO DOCKER):

Provide:

1) package.json with correct start script
2) Node version config
3) How to deploy from GitHub step-by-step
4) Build command
5) Run command

The server must listen on:

process.env.PORT

=================================

ANDROID CONNECTION:

Show how the Android app will call:

https://<koyeb-app>.koyeb.app/wallpapers/trending

=================================

OUTPUT:

Full working source code.
Deployment guide for Koyeb free tier.

promt 3

You are a senior backend architect.

Build a HIGH-PERFORMANCE production-ready backend for my Android wallpaper app called VIREX.

DEPLOY TARGET:
Koyeb (FREE TIER)
NO DOCKER
Node.js native build

========================================

TECH STACK:

Node.js 20
Express
Axios
Compression
Helmet
CORS
express-rate-limit
node-cache (memory cache)
agentkeepalive

========================================

MAIN GOAL:

The Android app must NOT call Pexels or Unsplash directly.

The backend will:

✔ fetch wallpapers
✔ cache responses
✔ merge multiple APIs
✔ work in Russia without VPN
✔ respond ultra fast

========================================

WALLPAPER SOURCES:

PRIMARY:
Pexels
Unsplash

FALLBACK:
Wallhaven (no API key, public JSON)

========================================

API ROUTES:

GET /wallpapers/trending

GET /wallpapers/search?q=

GET /wallpapers/category/:name

CATEGORIES:

Nature
Cars
AMOLED
Space
Cyberpunk
Minimal
Anime

========================================

UNIFIED RESPONSE FORMAT:

{
  id,
  title,
  preview_url,
  full_url,
  source,
  photographer
}

========================================

PERFORMANCE OPTIMIZATION:

HTTP keep-alive agent
Axios timeout
Memory cache (10 minutes)

CACHE KEYS:

trending
search_{query}
category_{name}

========================================

ANTI-SLEEP SYSTEM FOR KOYEB:

Add internal self-ping every 4 minutes:

GET /ping

Use setInterval to call:

https://YOUR-KOYEB-APP.koyeb.app/ping

========================================

SECURITY:

Helmet
Rate limit
CORS (allow all for now)

========================================

ENV:

PEXELS_API_KEY=
UNSPLASH_ACCESS_KEY=
PORT=

========================================

PROJECT STRUCTURE:

/src
  /routes
  /services
  /cache
  /utils

========================================

KOYEB DEPLOYMENT (STEP BY STEP):

Provide:

1) package.json
2) start script
3) Node version
4) Build command
5) Run command

IMPORTANT:

Server must run on:

process.env.PORT

========================================

ANDROID INTEGRATION:

Show example:

https://virex-backend.koyeb.app/wallpapers/trending

========================================

EXTRA:

Add:

GET /health

Return:

{
  status: "ok"
}

========================================

OUTPUT:

Full source code.
Ready to deploy.
Clean architecture.
promt  4 
You are a senior Android architect.

PROJECT:
VIREX Wallpapers

GOAL:
Fix Auto Sync so it NEVER freezes and ALWAYS shows content.
Remove ALL VPN-related logic, UI, messages, and checks from the app.

The app must work normally in Russia without VPN using our backend.

====================================================

❗ PART 1 — REMOVE VPN COMPLETELY

Remove:

- VPN detection
- VPN dialogs
- VPN required messages
- Network blocking logic
- Region restriction checks

Delete all:

isVpnActive()
showVpnWarning()
"Enable VPN" UI texts
VPN notification logic

The app must NOT reference VPN in any way.

====================================================

❗ PART 2 — AUTOSYNC — RUSTORE SAFE MODE

The Auto Sync screen must:

NEVER show infinite loader
NEVER be empty
NEVER block UI

====================================================

DATA FLOW:

Room database = single source of truth.

FLOW:

1) Open screen → instantly load cached wallpapers
2) Start background sync
3) When finished → update UI

====================================================

FIRST INSTALL (IMPORTANT FOR MODERATION):

If database is empty:

Load bundled demo wallpapers from assets:

assets/preload/

Insert into Room.

Show them immediately.

Label:
"Starter collection"

====================================================

SYNC LOGIC:

Use WorkManager.

Constraints:

NetworkType.CONNECTED

Timeout:

15 seconds max.

If success:

Update database
Show:
"Updated just now"

If failed:

Keep cached content
Show:
"Offline mode"

====================================================

UI STATES:

Content (always visible)

Top status chip:

Syncing…
Updated just now
Offline mode
Starter collection

Sync button:

Normal → "Sync now"
Loading → progress inside button

====================================================

PERFORMANCE:

Do NOT block main thread
Use Coroutines
Use Flow
Use Paging if list is large

====================================================

ERROR HANDLING:

If backend not available:

Show cached content
Show retry button

====================================================

VIEWMODEL:

Expose:

StateFlow<AutoSyncUiState>

====================================================

COMPOSE:

No full screen loader.

Use:

LazyVerticalGrid

====================================================

DELIVER:

Full working Kotlin code:

AutoSyncWorker
Repository sync function
Room preload logic
ViewModel
Compose screen

====================================================

RESULT:

Auto Sync opens instantly.
Never freezes.
Always shows wallpapers.
Passes RuStore moderation.

promt 5
You are a principal Android engineer.

PROJECT:
VIREX Wallpapers

ARCHITECTURE:
Clean Architecture
MVVM
Compose
Repository pattern

BACKEND:
Koyeb REST API

====================================================
GOAL

Make the app launch INSTANTLY like Wallcraft.

No loaders on first screen.
Content must appear immediately.

====================================================
DATA STRATEGY

Single source of truth = Room database.

On app start:

1) Load wallpapers from Room instantly
2) Show them in UI
3) Fetch fresh data from backend in background
4) Update Room
5) UI updates automatically via Flow

====================================================
PRELOAD FOR FIRST INSTALL

If Room is empty:

Load bundled wallpapers from:

assets/preload.json

Insert into database.

This guarantees:

First launch → content in < 300ms

====================================================
BACKEND CONNECTION

Use Retrofit.

BASE URL:

https://YOUR-KOYEB-APP.koyeb.app/

API:

GET /wallpapers/trending
GET /wallpapers/category/{name}
GET /wallpapers/search?q=

====================================================
PAGING

Use Paging 3 with RemoteMediator.

Local → Room
Remote → Backend

====================================================
PRO / FREE SYSTEM

Backend returns:

isPro: Boolean

Room entity:

isPremium: Boolean

UI:

If NOT PRO user:

Blur premium wallpapers
Lock icon
Open Pro screen on click

If PRO:

Load full resolution.

====================================================
CACHE STRATEGY

Coil image loader:

Enable:

memoryCache
diskCache

Prefetch thumbnails.

====================================================
UI — HOME

LazyVerticalGrid

NO full screen loading.

Top bar:

Chip:

Updated just now
Syncing
Offline mode

====================================================
VIEWMODEL

Expose:

StateFlow<HomeUiState>

HomeUiState:

data class HomeUiState(
    val wallpapers: PagingData<Wallpaper>,
    val syncState: SyncState
)

====================================================
AUTOSYNC INTEGRATION

WorkManager:

Run every 6 hours.

Also manual sync button.

====================================================
ERROR HANDLING

If backend fails:

Show cached content.
Show snackbar:

"Offline mode"

====================================================
PERFORMANCE

No blocking main thread.
Use Dispatchers.IO.

====================================================
DELIVER FULL IMPLEMENTATION:

Room
Retrofit
RemoteMediator
Repository
ViewModel
Compose Home screen

====================================================
RESULT:

App opens instantly.
Content always visible.
Pro system works.
Backend driven.
RuStore ready.

promt 6
You are a senior Android UI/UX engineer.

PROJECT:
VIREX Wallpapers

TECH:
Jetpack Compose
Material 3
Clean Architecture

GOAL:
Create a modern premium UI like Wallcraft / Backdrops.

Dark AMOLED style.
Smooth.
Fast.
Production ready.

====================================================
THEME

Pure black background (#000000)
Neon accent color
Glass / blur effects
Rounded corners
Soft shadows

Typography:

Bold headlines
Clean minimal labels

====================================================
HOME SCREEN

LazyVerticalGrid

Top section:

Greeting:
"Discover"

Horizontal category chips:

Nature
AMOLED
Space
Cars
Cyberpunk
Minimal
Anime

Scrollable.

Selected category = highlighted.

====================================================
WALLPAPER CARD

Rounded 22dp
Parallax effect on scroll
Gradient overlay bottom

Bottom-left:

Wallpaper title

Top-right:

PRO badge if premium

On click:

Open preview screen.

====================================================
PREMIUM LOCK

If not PRO:

Blur image
Lock icon
Click → navigate to Pro screen

====================================================
SHIMMER LOADING

Show skeleton grid while Paging loads next page.

NO full screen loader.

====================================================
SYNC STATUS CHIP

Floating top-right chip:

Syncing…
Updated just now
Offline mode

Animated.

====================================================
SEARCH BAR

Top search:

Glass style
Rounded
Search icon
Voice icon (UI only)

====================================================
BOTTOM NAVIGATION

Home
Categories
Favorites
AI Generate
Settings

Active tab:

Glow indicator.

====================================================
AI GENERATOR BUTTON

Floating button:

Icon: ✨

Label:
"Generate"

Gradient neon background.

====================================================
CATEGORIES SCREEN

Grid with image backgrounds.

====================================================
WALLPAPER PREVIEW SCREEN

Full screen image.

Top:

Back
Download
Favorite
Share

Bottom:

Set as wallpaper
PRO button if locked

====================================================
ANIMATIONS

Use:

AnimatedVisibility
Crossfade
Shared element transitions

====================================================
PERFORMANCE

Stable keys in LazyGrid
rememberLazyGridState
No recomposition issues

====================================================
DELIVER:

Full Compose code:

HomeScreen
WallpaperCard
CategoryChip
PremiumLock
ShimmerGrid
BottomBar
PreviewScreen

====================================================
RESULT:

Modern.
Smooth.
Premium looking.
RuStore screenshots ready.