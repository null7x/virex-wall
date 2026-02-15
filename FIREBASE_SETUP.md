# Firebase Setup Instructions

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: `VIREX Wallpapers`
4. Enable Google Analytics (optional but recommended)
5. Click "Create project"

## Step 2: Add Android App

1. In your Firebase project, click the Android icon
2. Enter package name: `com.virex.wallpapers`
3. Enter app nickname: `VIREX Wallpapers`
4. Enter SHA-1 (for release builds):
   ```bash
   keytool -list -v -keystore your-release.jks -alias your-alias
   ```
5. Click "Register app"
6. Download `google-services.json`
7. Place it in `app/` directory (replace the .example file)

## Step 3: Enable Firestore Database

1. Go to "Build" → "Firestore Database"
2. Click "Create database"
3. Select "Start in production mode"
4. Choose your region (closest to your users)
5. Click "Enable"

## Step 4: Set Up Firestore Structure

Create the following collections:

### Collection: `wallpapers`
Document structure:
```json
{
  "id": "wallpaper_001",
  "title": "Neon City",
  "thumbnail_url": "https://storage.googleapis.com/.../thumbnails/wallpaper_001.jpg",
  "full_url": "https://storage.googleapis.com/.../wallpapers/wallpaper_001.jpg",
  "category_id": "cat_abstract",
  "is_premium": false,
  "is_featured": true,
  "is_trending": true,
  "is_new": true,
  "downloads": 0,
  "created_at": Timestamp
}
```

### Collection: `categories`
Document structure:
```json
{
  "id": "cat_abstract",
  "name": "Abstract",
  "cover_url": "https://storage.googleapis.com/.../categories/abstract_cover.jpg",
  "wallpaper_count": 25,
  "sort_order": 1
}
```

## Step 5: Enable Cloud Storage

1. Go to "Build" → "Storage"
2. Click "Get started"
3. Select "Start in production mode"
4. Choose same region as Firestore
5. Click "Done"

## Step 6: Upload Wallpapers

Create folder structure in Storage:
```
/wallpapers/         # Full resolution wallpapers
/thumbnails/         # Compressed thumbnails (for grid display)
/categories/         # Category cover images
```

Recommended specifications:
- Full wallpapers: 1440x3040px (or 3:6.5 ratio), JPEG quality 90%
- Thumbnails: 400x800px, JPEG quality 80%
- Category covers: 600x400px, JPEG quality 85%

## Step 7: Deploy Security Rules

### Firestore Rules
Go to Firestore → Rules and paste content from `firebase/firestore.rules`

### Storage Rules
Go to Storage → Rules and paste content from `firebase/storage.rules`

## Step 8: Create Composite Indexes

Go to Firestore → Indexes → Composite and create:

1. **Index for featured wallpapers:**
   - Collection: `wallpapers`
   - Fields: `is_featured` (Ascending), `created_at` (Descending)

2. **Index for category wallpapers:**
   - Collection: `wallpapers`
   - Fields: `category_id` (Ascending), `created_at` (Descending)

3. **Index for trending wallpapers:**
   - Collection: `wallpapers`
   - Fields: `is_trending` (Ascending), `downloads` (Descending)

## Step 9: Verify Setup

After completing setup:
1. Build the app: `.\gradlew.bat assembleDebug`
2. Install on device: `.\gradlew.bat installDebug`
3. Open app and verify wallpapers load correctly

## Troubleshooting

### "Default FirebaseApp is not initialized"
- Make sure `google-services.json` is in `app/` folder
- Clean and rebuild: `.\gradlew.bat clean assembleDebug`

### Wallpapers not loading
- Check Firestore rules allow read access
- Verify collection and document structure
- Check Storage rules allow read access
- Verify image URLs are correct

### Network error
- Check internet connection
- Verify Firebase project is on Blaze plan if exceeding free limits
