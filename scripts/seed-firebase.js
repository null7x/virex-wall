/**
 * Firebase Seed Script
 * 
 * This script populates Firebase Firestore with initial wallpaper data
 * from free sources (Unsplash/Pexels).
 * 
 * Usage:
 * 1. Install Firebase Admin SDK: npm install firebase-admin
 * 2. Download service account key from Firebase Console
 * 3. Set environment variable: GOOGLE_APPLICATION_CREDENTIALS=path/to/serviceAccountKey.json
 * 4. Run: node seed-firebase.js
 */

const admin = require('firebase-admin');

// Initialize Firebase Admin
// Make sure GOOGLE_APPLICATION_CREDENTIALS environment variable is set
admin.initializeApp({
    projectId: 'virex-wallpapers'
});

const db = admin.firestore();

// Sample AMOLED/Dark wallpapers from Pexels (free to use)
const wallpapers = [
    {
        id: 'wall_001',
        title: 'Abstract Dark Waves',
        description: 'Beautiful dark abstract waves pattern',
        thumbnail_url: 'https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'amoled',
        category_name: 'AMOLED',
        width: 1920,
        height: 2560,
        file_size: 512000,
        downloads: 0,
        likes: 150,
        is_premium: false,
        is_featured: true,
        is_trending: true,
        tags: ['amoled', 'dark', 'space', 'galaxy'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_002',
        title: 'Galaxy Nebula',
        description: 'Deep space nebula in dark colors',
        thumbnail_url: 'https://images.pexels.com/photos/956981/milky-way-starry-sky-night-sky-star-956981.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/956981/milky-way-starry-sky-night-sky-star-956981.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'amoled',
        category_name: 'AMOLED',
        width: 1920,
        height: 2560,
        file_size: 620000,
        downloads: 0,
        likes: 280,
        is_premium: false,
        is_featured: true,
        is_trending: true,
        tags: ['amoled', 'galaxy', 'nebula', 'space'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_003',
        title: 'Dark Mountains',
        description: 'Silhouette of mountains at night',
        thumbnail_url: 'https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'dark',
        category_name: 'Dark',
        width: 1920,
        height: 2560,
        file_size: 450000,
        downloads: 0,
        likes: 200,
        is_premium: false,
        is_featured: true,
        is_trending: false,
        tags: ['dark', 'mountains', 'nature', 'silhouette'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_004',
        title: 'Starry Night Sky',
        description: 'Beautiful starry sky with milky way',
        thumbnail_url: 'https://images.pexels.com/photos/1421903/pexels-photo-1421903.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1421903/pexels-photo-1421903.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'amoled',
        category_name: 'AMOLED',
        width: 1920,
        height: 2560,
        file_size: 580000,
        downloads: 0,
        likes: 350,
        is_premium: false,
        is_featured: true,
        is_trending: true,
        tags: ['amoled', 'stars', 'night', 'milky way'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_005',
        title: 'Minimal Dark Gradient',
        description: 'Clean minimal dark gradient',
        thumbnail_url: 'https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'minimal',
        category_name: 'Minimal',
        width: 1920,
        height: 2560,
        file_size: 320000,
        downloads: 0,
        likes: 180,
        is_premium: false,
        is_featured: false,
        is_trending: true,
        tags: ['minimal', 'gradient', 'dark', 'clean'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_006',
        title: 'Dark Forest',
        description: 'Mysterious dark forest at night',
        thumbnail_url: 'https://images.pexels.com/photos/1366913/pexels-photo-1366913.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1366913/pexels-photo-1366913.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'dark',
        category_name: 'Dark',
        width: 1920,
        height: 2560,
        file_size: 490000,
        downloads: 0,
        likes: 220,
        is_premium: false,
        is_featured: true,
        is_trending: false,
        tags: ['dark', 'forest', 'nature', 'mysterious'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_007',
        title: 'Aurora Borealis',
        description: 'Northern lights over dark landscape',
        thumbnail_url: 'https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'amoled',
        category_name: 'AMOLED',
        width: 1920,
        height: 2560,
        file_size: 550000,
        downloads: 0,
        likes: 420,
        is_premium: false,
        is_featured: true,
        is_trending: true,
        tags: ['amoled', 'aurora', 'northern lights', 'nature'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_008',
        title: 'Minimal Lines',
        description: 'Simple minimal lines on dark background',
        thumbnail_url: 'https://images.pexels.com/photos/1939485/pexels-photo-1939485.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1939485/pexels-photo-1939485.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'minimal',
        category_name: 'Minimal',
        width: 1920,
        height: 2560,
        file_size: 280000,
        downloads: 0,
        likes: 165,
        is_premium: false,
        is_featured: false,
        is_trending: true,
        tags: ['minimal', 'lines', 'dark', 'abstract'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_009',
        title: 'Dark Ocean Waves',
        description: 'Dark moody ocean waves',
        thumbnail_url: 'https://images.pexels.com/photos/1295138/pexels-photo-1295138.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/1295138/pexels-photo-1295138.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'dark',
        category_name: 'Dark',
        width: 1920,
        height: 2560,
        file_size: 480000,
        downloads: 0,
        likes: 190,
        is_premium: false,
        is_featured: true,
        is_trending: false,
        tags: ['dark', 'ocean', 'waves', 'moody'],
        created_at: Date.now(),
        updated_at: Date.now()
    },
    {
        id: 'wall_010',
        title: 'City Lights at Night',
        description: 'City skyline with lights at night',
        thumbnail_url: 'https://images.pexels.com/photos/466685/pexels-photo-466685.jpeg?auto=compress&cs=tinysrgb&w=400',
        full_url: 'https://images.pexels.com/photos/466685/pexels-photo-466685.jpeg?auto=compress&cs=tinysrgb&w=1920',
        category_id: 'dark',
        category_name: 'Dark',
        width: 1920,
        height: 2560,
        file_size: 520000,
        downloads: 0,
        likes: 310,
        is_premium: false,
        is_featured: true,
        is_trending: true,
        tags: ['dark', 'city', 'night', 'lights'],
        created_at: Date.now(),
        updated_at: Date.now()
    }
];

// Categories
const categories = [
    {
        id: 'amoled',
        name: 'AMOLED',
        description: 'Pure black AMOLED wallpapers',
        thumbnail_url: 'https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=400',
        sort_order: 1,
        is_visible: true,
        wallpaper_count: 4
    },
    {
        id: 'dark',
        name: 'Dark',
        description: 'Dark and moody wallpapers',
        thumbnail_url: 'https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=400',
        sort_order: 2,
        is_visible: true,
        wallpaper_count: 4
    },
    {
        id: 'minimal',
        name: 'Minimal',
        description: 'Clean and minimal wallpapers',
        thumbnail_url: 'https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=400',
        sort_order: 3,
        is_visible: true,
        wallpaper_count: 2
    }
];

async function seedDatabase() {
    console.log('🚀 Starting Firebase seed...\n');

    try {
        // Seed categories
        console.log('📁 Seeding categories...');
        for (const category of categories) {
            await db.collection('categories').doc(category.id).set(category);
            console.log(`  ✓ Category: ${category.name}`);
        }
        console.log(`  → Added ${categories.length} categories\n`);

        // Seed wallpapers
        console.log('🖼️  Seeding wallpapers...');
        for (const wallpaper of wallpapers) {
            await db.collection('wallpapers').doc(wallpaper.id).set(wallpaper);
            console.log(`  ✓ Wallpaper: ${wallpaper.title}`);
        }
        console.log(`  → Added ${wallpapers.length} wallpapers\n`);

        console.log('✅ Firebase seed completed successfully!');
        console.log('\nYour VIREX Wallpapers app should now show wallpapers on the Home screen.');
        
    } catch (error) {
        console.error('❌ Error seeding database:', error);
        process.exit(1);
    }

    process.exit(0);
}

seedDatabase();
