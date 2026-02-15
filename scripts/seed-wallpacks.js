/**
 * VIREX Wallpapers - Seed Wallpacks to Firebase
 * 
 * This script reads wallpapers from assets/Wallpacks and:
 * 1. Uploads images to Firebase Storage (optional, with --upload flag)
 * 2. Creates Firestore documents for each wallpaper
 * 
 * Usage:
 *   npm install firebase-admin
 *   set GOOGLE_APPLICATION_CREDENTIALS=path/to/serviceAccountKey.json
 * 
 *   node seed-wallpacks.js              # Seed Firestore only (use wallhaven URLs)
 *   node seed-wallpacks.js --upload     # Upload to Storage + Seed Firestore
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// ── Config ───────────────────────────────────────────────────────────────

const UPLOAD_TO_STORAGE = process.argv.includes('--upload');
const BUCKET_NAME = 'virex-wallpapers.appspot.com';
const WALLPACKS_DIR = path.join(__dirname, '..', 'assets', 'Wallpacks');

// Initialize Firebase Admin
admin.initializeApp({
    projectId: 'virex-wallpapers',
    storageBucket: BUCKET_NAME
});

const db = admin.firestore();
const bucket = admin.storage().bucket();

// ── Category metadata ────────────────────────────────────────────────────

const CATEGORY_META = {
    abstract:   { name: 'Abstract',    description: 'Abstract art and patterns',          sortOrder: 1,  icon: 'palette' },
    animals:    { name: 'Animals',     description: 'Wildlife and animal wallpapers',     sortOrder: 2,  icon: 'pets' },
    anime:      { name: 'Anime',       description: 'Anime and manga artwork',            sortOrder: 3,  icon: 'animation' },
    cars:       { name: 'Cars',        description: 'Supercars and automotive',            sortOrder: 4,  icon: 'directions_car' },
    city:       { name: 'City',        description: 'Urban landscapes and cityscapes',     sortOrder: 5,  icon: 'location_city' },
    cyberpunk:  { name: 'Cyberpunk',   description: 'Cyberpunk and neon aesthetics',       sortOrder: 6,  icon: 'electric_bolt' },
    dark:       { name: 'Dark',        description: 'Dark and moody wallpapers',           sortOrder: 7,  icon: 'dark_mode' },
    fantasy:    { name: 'Fantasy',     description: 'Fantasy worlds and magical scenes',   sortOrder: 8,  icon: 'auto_awesome' },
    games:      { name: 'Games',       description: 'Video game art and screenshots',      sortOrder: 9,  icon: 'sports_esports' },
    general:    { name: 'General',     description: 'General wallpapers',                  sortOrder: 10, icon: 'wallpaper' },
    minimal:    { name: 'Minimal',     description: 'Clean and minimalist designs',        sortOrder: 11, icon: 'crop_square' },
    mountains:  { name: 'Mountains',   description: 'Mountain landscapes and peaks',       sortOrder: 12, icon: 'terrain' },
    nature:     { name: 'Nature',      description: 'Nature and landscapes',               sortOrder: 13, icon: 'forest' },
    people:     { name: 'People',      description: 'People and portraits',                sortOrder: 14, icon: 'person' },
    space:      { name: 'Space',       description: 'Space, galaxies and cosmos',          sortOrder: 15, icon: 'rocket_launch' },
    technology: { name: 'Technology',  description: 'Tech and digital art',                sortOrder: 16, icon: 'memory' },
    rosepine:   { name: 'Rosé Pine',   description: 'Rosé Pine themed wallpapers',        sortOrder: 17, icon: 'local_florist' },
};

// ── Helpers ──────────────────────────────────────────────────────────────

function getWallhavenThumbnail(fullUrl) {
    const match = fullUrl.match(/wallhaven-([a-z0-9]+)\.\w+$/);
    if (match) {
        const code = match[1];
        const prefix = code.substring(0, 2);
        return `https://th.wallhaven.cc/lg/${prefix}/${code}.jpg`;
    }
    return fullUrl;
}

function getWallhavenId(url) {
    const match = url.match(/wallhaven-([a-z0-9]+)\.\w+$/);
    return match ? match[1] : null;
}

function getImageFiles(dir) {
    if (!fs.existsSync(dir)) return [];
    return fs.readdirSync(dir).filter(f => /\.(jpg|jpeg|png|webp)$/i.test(f));
}

function readUrlsFile(dir) {
    const urlsFile = path.join(dir, 'downloaded_urls.txt');
    if (!fs.existsSync(urlsFile)) return [];
    return fs.readFileSync(urlsFile, 'utf8')
        .split('\n')
        .map(l => l.trim())
        .filter(l => l.length > 0);
}

async function uploadToStorage(localPath, storagePath) {
    try {
        await bucket.upload(localPath, {
            destination: storagePath,
            metadata: {
                cacheControl: 'public, max-age=31536000',
                contentType: localPath.endsWith('.png') ? 'image/png' : 'image/jpeg'
            }
        });
        const file = bucket.file(storagePath);
        const [url] = await file.getSignedUrl({
            action: 'read',
            expires: '03-09-2491' // far future
        });
        return url;
    } catch (err) {
        // Use makePublic + public URL as fallback
        try {
            await bucket.file(storagePath).makePublic();
            return `https://storage.googleapis.com/${BUCKET_NAME}/${storagePath}`;
        } catch (e) {
            console.error(`  ✗ Upload failed: ${storagePath}`, e.message);
            return null;
        }
    }
}

// ── Main ─────────────────────────────────────────────────────────────────

async function main() {
    console.log('');
    console.log('================================================================');
    console.log('  VIREX Wallpapers - Seed Wallpacks to Firebase');
    console.log(`  Mode: ${UPLOAD_TO_STORAGE ? 'Upload to Storage + Firestore' : 'Firestore only (external URLs)'}`);
    console.log('================================================================');
    console.log('');

    const timestamp = Date.now();
    const categoryData = {};

    // ── Step 1: Scan categories ──────────────────────────────────────────

    console.log('[1/3] Scanning Wallpacks folder...\n');

    const dirs = fs.readdirSync(WALLPACKS_DIR, { withFileTypes: true })
        .filter(d => d.isDirectory() && d.name !== 'PlaySafe');

    for (const dir of dirs) {
        const catId = dir.name.toLowerCase();
        const catDir = path.join(WALLPACKS_DIR, dir.name);
        const urls = readUrlsFile(catDir);
        const images = getImageFiles(catDir);

        if (urls.length === 0 && images.length === 0) {
            console.log(`  [ ] ${catId} - empty, skipping`);
            continue;
        }

        categoryData[catId] = {
            urls,
            images,
            catDir,
            isWallhaven: true
        };
        console.log(`  [+] ${catId} - ${urls.length} URLs, ${images.length} images`);
    }

    // Process rose-pine
    const rosePineDir = path.join(WALLPACKS_DIR, 'PlaySafe', 'rose-pine');
    if (fs.existsSync(rosePineDir)) {
        const rpImages = getImageFiles(rosePineDir);
        if (rpImages.length > 0) {
            const rpUrls = rpImages.map(img => {
                const encoded = encodeURIComponent(img);
                return `https://raw.githubusercontent.com/rose-pine/wallpapers/main/${encoded}`;
            });
            categoryData['rosepine'] = {
                urls: rpUrls,
                images: rpImages,
                catDir: rosePineDir,
                isWallhaven: false
            };
            console.log(`  [+] rosepine - ${rpImages.length} images (GitHub)`);
        }
    }

    console.log('');

    // ── Step 2: Upload categories ────────────────────────────────────────

    console.log('[2/3] Uploading categories to Firestore...\n');
    let catCount = 0;

    for (const [catId, data] of Object.entries(categoryData)) {
        const meta = CATEGORY_META[catId] || {
            name: catId, description: `${catId} wallpapers`, sortOrder: 99, icon: 'wallpaper'
        };

        let coverUrl = '';
        if (data.isWallhaven && data.urls.length > 0) {
            coverUrl = getWallhavenThumbnail(data.urls[0]);
        } else if (data.urls.length > 0) {
            coverUrl = data.urls[0];
        }

        const catDoc = {
            id: catId,
            name: meta.name,
            description: meta.description,
            thumbnail_url: coverUrl,
            cover_url: coverUrl,
            sort_order: meta.sortOrder,
            icon: meta.icon,
            is_visible: true,
            wallpaper_count: data.urls.length || data.images.length,
            created_at: timestamp,
            updated_at: timestamp
        };

        await db.collection('categories').doc(catId).set(catDoc, { merge: true });
        console.log(`  ✓ ${meta.name} (${catDoc.wallpaper_count} wallpapers)`);
        catCount++;
    }

    console.log(`  → ${catCount} categories uploaded\n`);

    // ── Step 3: Upload wallpapers ────────────────────────────────────────

    console.log('[3/3] Uploading wallpapers to Firestore...');
    if (UPLOAD_TO_STORAGE) {
        console.log('  (Also uploading images to Firebase Storage...)');
    }
    console.log('');

    let successCount = 0;
    let failCount = 0;
    let globalIndex = 0;
    const batch_size = 400; // Firestore batch limit is 500
    let batch = db.batch();
    let batchCount = 0;

    for (const [catId, data] of Object.entries(categoryData)) {
        const meta = CATEGORY_META[catId] || { name: catId };
        console.log(`  [${meta.name}]`);

        for (let i = 0; i < data.urls.length; i++) {
            const url = data.urls[i];
            globalIndex++;

            let wallId, fullUrl, thumbnailUrl, title;

            if (data.isWallhaven) {
                const whId = getWallhavenId(url);
                wallId = whId ? `wh_${whId}` : `${catId}_${i + 1}`;
                fullUrl = url;
                thumbnailUrl = getWallhavenThumbnail(url);
                title = `${meta.name} #${i + 1}`;
            } else {
                const fileName = data.images[i] || `image_${i}`;
                const safeName = fileName.replace(/\.[^.]+$/, '').replace(/[^a-zA-Z0-9_-]/g, '_');
                wallId = `rp_${safeName}`;
                fullUrl = url;
                thumbnailUrl = url;
                title = fileName.replace(/\.[^.]+$/, '').replace(/[_-]/g, ' ');
            }

            // Optional: Upload to Firebase Storage
            if (UPLOAD_TO_STORAGE && data.images[i]) {
                const localFile = path.join(data.catDir, data.images[i]);
                if (fs.existsSync(localFile)) {
                    const storagePath = `wallpapers/${catId}/${data.images[i]}`;
                    const storageUrl = await uploadToStorage(localFile, storagePath);
                    if (storageUrl) {
                        fullUrl = storageUrl;
                        // Create thumbnail path
                        thumbnailUrl = storageUrl; // Could resize separately
                    }
                }
            }

            const tags = [catId];
            if (data.isWallhaven) tags.push('wallhaven');
            if (catId === 'rosepine') tags.push('rosepine', 'aesthetic', 'minimal');

            const wallDoc = {
                id: wallId,
                title,
                description: `${meta.name} wallpaper`,
                thumbnail_url: thumbnailUrl,
                full_url: fullUrl,
                category_id: catId,
                category_name: meta.name,
                width: 1920,
                height: 2880,
                file_size: 500000,
                downloads: 0,
                likes: Math.floor(Math.random() * 450) + 50,
                is_premium: false,
                is_featured: i < 5,
                is_trending: i < 3,
                is_new: true,
                tags,
                source: data.isWallhaven ? 'wallhaven' : 'github',
                created_at: timestamp - (i * 60000),
                updated_at: timestamp
            };

            const docRef = db.collection('wallpapers').doc(wallId);
            batch.set(docRef, wallDoc, { merge: true });
            batchCount++;
            successCount++;

            // Commit batch when it reaches the limit
            if (batchCount >= batch_size) {
                try {
                    await batch.commit();
                    console.log(`    ... ${globalIndex} wallpapers processed (batch committed)`);
                } catch (err) {
                    console.error(`    ✗ Batch commit failed:`, err.message);
                    failCount += batchCount;
                    successCount -= batchCount;
                }
                batch = db.batch();
                batchCount = 0;
            }
        }

        console.log(`    → ${data.urls.length} wallpapers queued for ${meta.name}`);
    }

    // Commit remaining batch
    if (batchCount > 0) {
        try {
            await batch.commit();
            console.log(`    ... final batch committed (${batchCount} docs)`);
        } catch (err) {
            console.error(`    ✗ Final batch commit failed:`, err.message);
            failCount += batchCount;
            successCount -= batchCount;
        }
    }

    console.log('');
    console.log('================================================================');
    console.log('  SEED COMPLETE!');
    console.log('================================================================');
    console.log('');
    console.log(`  Categories:  ${catCount}`);
    console.log(`  Wallpapers:  ${successCount} uploaded, ${failCount} failed`);
    console.log(`  Total docs:  ${catCount + successCount}`);
    console.log('');
    console.log('  Restart the app to see the new wallpapers!');
    console.log('================================================================');
    console.log('');

    process.exit(0);
}

main().catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
});
