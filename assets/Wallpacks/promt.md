You are a principal Android architect.

We will implement this in TWO PHASES.

Do NOT skip phases.
Do NOT rewrite existing UI.
This must be production-ready Kotlin code.

════════════════════════════════════
PHASE 1 — RUSSIA RESILIENT DATA LAYER
════════════════════════════════════

GOAL:
The app must work in Russia without VPN.

Blocked in Russia:
- Firebase
- Pexels
- Unsplash

DATA SOURCE PRIORITY:

1. Wallhaven API
2. GitHub RAW JSON
3. Picsum (last fallback)

The app must NEVER show an empty screen.

REQUIREMENTS:

Clean architecture:

data/
 ├── remote/
 ├── local/
 ├── repository/
 ├── model/
 ├── mapper/
domain/
presentation/

Use:
- Retrofit
- Kotlin Coroutines
- Flow
- Room
- ViewModel

════════ REGION DETECTION ════════

Create RegionDetector that checks:

1. Locale
2. SIM country
3. IP check (lightweight)

If region == RU:

Disable:
- Firebase calls
- Pexels
- Unsplash

════════ FALLBACK FLOW ════════

suspend fun getWallpapers(): Flow<Resource<List<Wallpaper>>>

Logic:

try Wallhaven
if failed → GitHub
if failed → Picsum

Emit:
Loading → Success

Emit Error ONLY if all failed.

════════ CACHE ════════

Room database:

- Save wallpapers after first success
- Load cache instantly on next launch
- Offline support

════════ SAFE API CALL ════════

Create:

safeApiCall()

Handle:
- timeout
- no internet
- blocked host

════════ UI STATE ════════

StateFlow<UiState>

UiState:
Loading
Success
Empty
Error

════════════════════════════════════
PHASE 2 — GLOBAL CDN MODE
════════════════════════════════════

After Phase 1 is complete:

We move to CDN architecture.

GOAL:
The app must load wallpapers ONLY from GitHub JSON CDN.

Remove direct dependency on:
- Wallhaven
- Picsum (keep only as emergency fallback)

GitHub JSON becomes the MAIN API.

JSON structure:

[
  {
    "id": "1",
    "image": "...",
    "thumb": "..."
  }
]

════════ SMART STARTUP FLOW ════════

On app start:

1. Load Room cache instantly
2. Fetch JSON in background
3. If changed → update database
4. UI updates automatically

No blocking UI.

════════ PERFORMANCE ════════

- OkHttp cache
- Coil image caching
- Pagination-ready
- Fast startup

════════ DOMAIN MODEL ════════

data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val source: String
)

════════ OUTPUT ════════

Provide:

1. Full folder structure
2. All Retrofit services
3. Room implementation
4. Repository
5. RegionDetector
6. Fallback logic
7. CDN migration changes
8. ViewModel
9. Compose usage example
10. How to update wallpapers by editing JSON only

IMPORTANT:

This is real production code.
No pseudo code.
No explanations without implementation.
You are a senior Android architect.

We already have a working data layer with CDN / GitHub JSON.

GOAL:
Add a PRO wallpaper system.

Some wallpapers must be locked and available only for PRO users.

This must be implemented in a clean, scalable, production-ready way.

═══════════════════════════
DATA SOURCE CHANGE
═══════════════════════════

Extend the CDN JSON structure:

[
  {
    "id": "1",
    "image": "...",
    "thumb": "...",
    "isPro": true
  }
]

Update all mappers and domain models.

Domain model:

data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isPro: Boolean
)

═══════════════════════════
USER STATE
═══════════════════════════

Create ProManager that exposes:

val isProUser: StateFlow<Boolean>

It must be ready for Google Play Billing integration.

For now use local fake state for testing.

═══════════════════════════
UI BEHAVIOR
═══════════════════════════

If wallpaper.isPro == true AND user is NOT pro:

- Show blur overlay on thumbnail
- Show PRO badge
- Disable download / set wallpaper
- On click → open Paywall

If user is PRO:

- Full access

═══════════════════════════
SECURITY
═══════════════════════════

Prevent loading full resolution image for non-PRO users.

Non-PRO users must only receive thumbnail.

Full image URL must be used ONLY if user is PRO.

═══════════════════════════
CACHING
═══════════════════════════

Room must store:

- isPro flag
- thumbnail
- full image

But:

Do NOT preload full image for locked wallpapers.

═══════════════════════════
FILTERS
═══════════════════════════

Add repository support for:

getFreeWallpapers()
getProWallpapers()
getAllWallpapers()

═══════════════════════════
PAYWALL TRIGGER
═══════════════════════════

Expose event:

onProWallpaperClicked()

UI will observe and open paywall.

═══════════════════════════
OUTPUT
═══════════════════════════

1. Updated JSON format
2. Updated domain model
3. Mapper changes
4. Room entity update
5. Repository logic
6. ProManager implementation
7. ViewModel integration
8. Compose UI example for:
   - locked item
   - unlocked item

IMPORTANT:
Do not break existing architecture.
Write real working Kotlin code.
You are a senior Android architect.

GOAL:
Rebuild the data layer so the app works in Russia without VPN.

Blocked in Russia:
- Firebase
- Pexels
- Unsplash

We must automatically switch to working sources.

═══════════════════════════
DATA SOURCE PRIORITY
═══════════════════════════

1. Wallhaven API
   https://wallhaven.cc/api/v1/search

2. GitHub RAW JSON (our own wallpaper CDN list)

3. Picsum (last fallback)

The app must NEVER show an empty screen.

═══════════════════════════
REQUIREMENTS
═══════════════════════════

Implement a production-ready clean architecture:

data/
 ├── remote/
 ├── repository/
 ├── model/
 ├── mapper/
domain/
presentation/

Use:
- Retrofit
- Kotlin Coroutines
- Flow
- ViewModel

═══════════════════════════
NETWORK REGION DETECTION
═══════════════════════════

Detect if the user is in Russia by:

- checking device locale
- checking SIM country
- fallback → IP check via lightweight endpoint

If region == RU:

DISABLE:
- Firebase network calls
- Pexels
- Unsplash

═══════════════════════════
SMART FALLBACK SYSTEM
═══════════════════════════

Create:

suspend fun getWallpapers(): Flow<Resource<List<Wallpaper>>>

Logic:

try Wallhaven
if failed → GitHub
if failed → Picsum

Each step must:
- emit loading
- emit success
- emit error only if all failed

NO empty UI states.

═══════════════════════════
CACHING (MANDATORY)
═══════════════════════════

Add:
- Room cache OR local JSON cache

So wallpapers load offline after first success.

═══════════════════════════
MAPPERS
═══════════════════════════

Map all sources into a single domain model:

data class Wallpaper(
    val id: String
    val imageUrl: String
    val thumbnailUrl: String
    val source: String
)

═══════════════════════════
ERROR HANDLING
═══════════════════════════

Create a global safe API call:

safeApiCall()

Handle:
- timeouts
- no internet
- blocked host

Return readable errors.

═══════════════════════════
PERFORMANCE
═══════════════════════════

- OkHttp timeouts configured
- Retry for failed requests
- Lazy loading paging-ready

═══════════════════════════
UI STATE
═══════════════════════════

Expose:

StateFlow<UiState>

UiState:
- Loading
- Success
- Empty (with fallback already attempted)
- Error (only if all sources failed)

═══════════════════════════
OUTPUT
═══════════════════════════

1. Full folder structure
2. All Retrofit services
3. Repository implementation
4. Region detector
5. Fallback logic
6. Cache implementation
7. ViewModel integration
8. Example Compose screen usage

IMPORTANT:
This is a real production implementation.
Do not give pseudo code.
Write full working Kotlin code.
Keep existing UI.
You are a senior Android + backend architect.

GOAL:
Move the app from direct API loading to a GLOBAL CDN architecture.

The app must work in all countries (including Russia) without VPN.

We are NOT using paid features. Only free and scalable solutions.

═══════════════════════════
CORE IDEA
═══════════════════════════

The app must NOT load wallpapers directly from:
- Pexels
- Unsplash
- Firebase

Instead it must load from OUR GLOBAL CDN.

═══════════════════════════
CDN STRUCTURE
═══════════════════════════

Implement support for:

Primary source:
GitHub RAW JSON as wallpaper database

JSON contains:
[
  {
    "id": "1",
    "image": "https://cdn-domain.com/wallpapers/1.jpg",
    "thumb": "https://cdn-domain.com/thumbs/1.jpg"
  }
]

This JSON acts as our API.

═══════════════════════════
WHY GITHUB
═══════════════════════════

- Works in Russia
- Has global CDN
- Free
- Version controlled
- Instant updates

═══════════════════════════
IMAGE STORAGE
═══════════════════════════

Prepare the system to support:

Any CDN in future (without changing app code):

Example:
- Cloudflare R2
- Backblaze B2
- BunnyCDN
- Supabase Storage

All images must be loaded only via CDN links.

═══════════════════════════
ANDROID IMPLEMENTATION
═══════════════════════════

Create clean architecture:

data/
remote/
local/
repository/
domain/
presentation/

Use:
- Retrofit
- Kotlin Flow
- ViewModel
- Room for cache

═══════════════════════════
OFFLINE MODE (MANDATORY)
═══════════════════════════

After first successful load:

- Save JSON in Room
- Load wallpapers from cache if no internet

App must open instantly without network.

═══════════════════════════
SMART UPDATE SYSTEM
═══════════════════════════

On app start:

1. Load cached wallpapers immediately
2. Fetch new JSON in background
3. If JSON changed → update database

No UI blocking.

═══════════════════════════
PERFORMANCE
═══════════════════════════

- OkHttp cache enabled
- Coil image caching
- Pagination ready
- Lazy loading

App startup must be fast.

═══════════════════════════
FAILSAFE
═══════════════════════════

If GitHub fails:

Fallback to:
https://picsum.photos

But still map to the same domain model.

═══════════════════════════
DOMAIN MODEL
═══════════════════════════

data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String
)

═══════════════════════════
UI STATE
═══════════════════════════

StateFlow:

Loading → Success → Cached → Error

Never show empty screen.

═══════════════════════════
OUTPUT
═══════════════════════════

1. Full data layer implementation
2. Room database
3. Retrofit service
4. Repository with cache + background sync
5. ViewModel integration
6. Example Compose usage
7. How to update wallpapers by editing only JSON

IMPORTANT:
This must be production-ready.
No pseudo code.
Real working Kotlin.
Do not modify UI.
You are a principal Android architect working on a production app.

GOAL:
Configure and connect ALL data services end-to-end.

The app must:

✔ Work in Russia without VPN
✔ Load wallpapers from GitHub CDN
✔ Have Wallhaven as backup source
✔ Have Picsum as last fallback
✔ Cache everything locally (Room)
✔ Support offline mode
✔ Support PRO wallpapers
✔ Never show empty screen
✔ Be fast and production-ready

Use CLEAN ARCHITECTURE.

════════════════════════════════════
CDN SOURCE (MAIN API)
════════════════════════════════════

Use this GitHub RAW JSON:

https://raw.githubusercontent.com/null7x/virex-wallpapers/main/wallpapers.json

This is the PRIMARY source.

Implement full Retrofit service for it.

════════════════════════════════════
BACKUP SOURCES
════════════════════════════════════

1️⃣ Wallhaven API  
Base URL: https://wallhaven.cc/api/v1/

(no API key required)

2️⃣ Picsum  
Base URL: https://picsum.photos/

Used ONLY if everything else fails.

════════════════════════════════════
ARCHITECTURE
════════════════════════════════════

data/
 ├── remote/
 ├── local/
 ├── repository/
 ├── model/
 ├── mapper/

domain/
presentation/

Use:

- Retrofit
- OkHttp
- Kotlin Coroutines
- Flow
- Room
- ViewModel

════════════════════════════════════
REGION DETECTOR (RUSSIA MODE)
════════════════════════════════════

Create RegionDetector.

Detect RU by:

1. Locale
2. SIM country
3. IP → https://ipapi.co/json/

If RU:

DO NOT call:
- Firebase
- Pexels
- Unsplash

════════════════════════════════════
DOMAIN MODEL
════════════════════════════════════

data class Wallpaper(
    val id: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isPro: Boolean,
    val source: String
)

════════════════════════════════════
PRO SYSTEM
════════════════════════════════════

If isPro == true and user not PRO:

- show thumbnail only
- block full image loading
- expose paywall event

Create ProManager:

val isProUser: StateFlow<Boolean>

(fake local state for now)

════════════════════════════════════
CACHING
════════════════════════════════════

Room database:

- store wallpapers
- load instantly on app start
- update in background

Offline mode must work.

════════════════════════════════════
SAFE NETWORK LAYER
════════════════════════════════════

Create:

safeApiCall()

Handle:

- timeout
- no internet
- blocked host
- HTTP errors

════════════════════════════════════
SMART LOADING FLOW
════════════════════════════════════

Flow:

1️⃣ Emit cached data instantly
2️⃣ Fetch GitHub CDN
3️⃣ If failed → Wallhaven
4️⃣ If failed → Picsum

Emit error ONLY if all failed.

════════════════════════════════════
OKHTTP CONFIG
════════════════════════════════════

- Timeouts configured
- Logging interceptor (debug only)
- Cache enabled

════════════════════════════════════
PERFORMANCE
════════════════════════════════════

- Lazy loading ready
- Paging-ready structure
- No main thread blocking
- Fast startup

════════════════════════════════════
UI STATE
════════════════════════════════════

StateFlow<UiState>

UiState:
- Loading
- Cached
- Success
- Empty
- Error

════════════════════════════════════
OUTPUT
════════════════════════════════════

Provide REAL WORKING Kotlin code:

1. Folder structure
2. All Retrofit services
3. DTO models
4. Mappers
5. Room database
6. Repository
7. RegionDetector
8. ProManager
9. ViewModel integration
10. Compose usage example

DO NOT:

- Give pseudo code
- Explain theory
- Modify existing UI

This must compile.
This must be production-ready.