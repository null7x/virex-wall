# Firebase Seed Script (PowerShell)
# Uploads initial wallpaper data to Firebase Firestore

$projectId = "virex-wallpapers"
$apiKey = "AIzaSyBj61px2TYjSp9SbuiTuGZbA3c2k7ofs_g"
$baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

function ConvertTo-FirestoreValue {
    param($value)
    
    if ($null -eq $value) {
        return @{ "nullValue" = $null }
    }
    elseif ($value -is [bool]) {
        return @{ "booleanValue" = $value }
    }
    elseif ($value -is [int] -or $value -is [long]) {
        return @{ "integerValue" = $value.ToString() }
    }
    elseif ($value -is [array]) {
        $arrayValues = @()
        foreach ($item in $value) {
            $arrayValues += ConvertTo-FirestoreValue $item
        }
        return @{ "arrayValue" = @{ "values" = $arrayValues } }
    }
    else {
        return @{ "stringValue" = $value.ToString() }
    }
}

function ConvertTo-FirestoreDocument {
    param($data)
    
    $fields = @{}
    foreach ($key in $data.Keys) {
        $fields[$key] = ConvertTo-FirestoreValue $data[$key]
    }
    return @{ "fields" = $fields }
}

function Add-Document {
    param(
        [string]$collection,
        [string]$docId,
        [hashtable]$data
    )
    
    $url = "$baseUrl/${collection}?documentId=$docId&key=$apiKey"
    $body = ConvertTo-FirestoreDocument $data | ConvertTo-Json -Depth 10
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
        Write-Host "  + $collection/$docId" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "  x $collection/$docId - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Categories
$categories = @(
    @{
        id              = "amoled"
        name            = "AMOLED"
        description     = "Pure black AMOLED wallpapers"
        thumbnail_url   = "https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=400"
        sort_order      = 1
        is_visible      = $true
        wallpaper_count = 5
    },
    @{
        id              = "dark"
        name            = "Dark"
        description     = "Dark and moody wallpapers"
        thumbnail_url   = "https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=400"
        sort_order      = 2
        is_visible      = $true
        wallpaper_count = 4
    },
    @{
        id              = "minimal"
        name            = "Minimal"
        description     = "Clean and minimal wallpapers"
        thumbnail_url   = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=400"
        sort_order      = 3
        is_visible      = $true
        wallpaper_count = 3
    }
)

# Wallpapers - using free Pexels images
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$wallpapers = @(
    @{
        id            = "wall_001"
        title         = "Galaxy Dark Space"
        description   = "Beautiful dark galaxy"
        thumbnail_url = "https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/2150/sky-space-dark-galaxy.jpg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "amoled"
        category_name = "AMOLED"
        width         = 1920
        height        = 2880
        file_size     = 512000
        downloads     = 0
        likes         = 250
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("amoled", "space", "galaxy", "dark")
        created_at    = $timestamp
        updated_at    = $timestamp
    },
    @{
        id            = "wall_002"
        title         = "Milky Way Stars"
        description   = "Starry night with milky way"
        thumbnail_url = "https://images.pexels.com/photos/956981/milky-way-starry-sky-night-sky-star-956981.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/956981/milky-way-starry-sky-night-sky-star-956981.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "amoled"
        category_name = "AMOLED"
        width         = 1920
        height        = 2880
        file_size     = 620000
        downloads     = 0
        likes         = 380
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("amoled", "stars", "milky way", "night")
        created_at    = $timestamp - 100000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_003"
        title         = "Dark Mountain Silhouette"
        description   = "Mountains at night"
        thumbnail_url = "https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "dark"
        category_name = "Dark"
        width         = 1920
        height        = 2880
        file_size     = 450000
        downloads     = 0
        likes         = 200
        is_premium    = $false
        is_featured   = $true
        is_trending   = $false
        tags          = @("dark", "mountains", "silhouette", "nature")
        created_at    = $timestamp - 200000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_004"
        title         = "Night Stars"
        description   = "Beautiful starry night"
        thumbnail_url = "https://images.pexels.com/photos/1421903/pexels-photo-1421903.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1421903/pexels-photo-1421903.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "amoled"
        category_name = "AMOLED"
        width         = 1920
        height        = 2880
        file_size     = 580000
        downloads     = 0
        likes         = 420
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("amoled", "stars", "night", "sky")
        created_at    = $timestamp - 300000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_005"
        title         = "Minimal Abstract"
        description   = "Clean minimal design"
        thumbnail_url = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1103970/pexels-photo-1103970.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "minimal"
        category_name = "Minimal"
        width         = 1920
        height        = 2880
        file_size     = 320000
        downloads     = 0
        likes         = 180
        is_premium    = $false
        is_featured   = $false
        is_trending   = $true
        tags          = @("minimal", "abstract", "dark", "clean")
        created_at    = $timestamp - 400000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_006"
        title         = "Dark Forest"
        description   = "Mysterious forest at night"
        thumbnail_url = "https://images.pexels.com/photos/1366913/pexels-photo-1366913.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1366913/pexels-photo-1366913.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "dark"
        category_name = "Dark"
        width         = 1920
        height        = 2880
        file_size     = 490000
        downloads     = 0
        likes         = 220
        is_premium    = $false
        is_featured   = $true
        is_trending   = $false
        tags          = @("dark", "forest", "nature", "mysterious")
        created_at    = $timestamp - 500000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_007"
        title         = "Northern Lights"
        description   = "Aurora borealis"
        thumbnail_url = "https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1933316/pexels-photo-1933316.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "amoled"
        category_name = "AMOLED"
        width         = 1920
        height        = 2880
        file_size     = 550000
        downloads     = 0
        likes         = 520
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("amoled", "aurora", "northern lights", "nature")
        created_at    = $timestamp - 600000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_008"
        title         = "Dark Ocean"
        description   = "Moody ocean waves"
        thumbnail_url = "https://images.pexels.com/photos/1295138/pexels-photo-1295138.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1295138/pexels-photo-1295138.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "dark"
        category_name = "Dark"
        width         = 1920
        height        = 2880
        file_size     = 480000
        downloads     = 0
        likes         = 190
        is_premium    = $false
        is_featured   = $true
        is_trending   = $false
        tags          = @("dark", "ocean", "waves", "moody")
        created_at    = $timestamp - 700000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_009"
        title         = "City Night Lights"
        description   = "City skyline at night"
        thumbnail_url = "https://images.pexels.com/photos/466685/pexels-photo-466685.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/466685/pexels-photo-466685.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "dark"
        category_name = "Dark"
        width         = 1920
        height        = 2880
        file_size     = 520000
        downloads     = 0
        likes         = 310
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("dark", "city", "night", "lights")
        created_at    = $timestamp - 800000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_010"
        title         = "Pure Black AMOLED"
        description   = "Pure black background"
        thumbnail_url = "https://images.pexels.com/photos/1939485/pexels-photo-1939485.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1939485/pexels-photo-1939485.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "amoled"
        category_name = "AMOLED"
        width         = 1920
        height        = 2880
        file_size     = 280000
        downloads     = 0
        likes         = 450
        is_premium    = $false
        is_featured   = $true
        is_trending   = $true
        tags          = @("amoled", "pure black", "minimal", "dark")
        created_at    = $timestamp - 900000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_011"
        title         = "Minimal Lines"
        description   = "Simple geometric lines"
        thumbnail_url = "https://images.pexels.com/photos/1699161/pexels-photo-1699161.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1699161/pexels-photo-1699161.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "minimal"
        category_name = "Minimal"
        width         = 1920
        height        = 2880
        file_size     = 350000
        downloads     = 0
        likes         = 165
        is_premium    = $false
        is_featured   = $false
        is_trending   = $true
        tags          = @("minimal", "lines", "geometric", "abstract")
        created_at    = $timestamp - 1000000
        updated_at    = $timestamp
    },
    @{
        id            = "wall_012"
        title         = "Dark Clouds"
        description   = "Stormy dark clouds"
        thumbnail_url = "https://images.pexels.com/photos/1054218/pexels-photo-1054218.jpeg?auto=compress&cs=tinysrgb&w=400"
        full_url      = "https://images.pexels.com/photos/1054218/pexels-photo-1054218.jpeg?auto=compress&cs=tinysrgb&w=1920"
        category_id   = "dark"
        category_name = "Dark"
        width         = 1920
        height        = 2880
        file_size     = 420000
        downloads     = 0
        likes         = 175
        is_premium    = $false
        is_featured   = $false
        is_trending   = $false
        tags          = @("dark", "clouds", "stormy", "sky")
        created_at    = $timestamp - 1100000
        updated_at    = $timestamp
    }
)

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VIREX Wallpapers - Firebase Seed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Upload categories
Write-Host "Uploading categories..." -ForegroundColor Yellow
foreach ($cat in $categories) {
    Add-Document -collection "categories" -docId $cat.id -data $cat | Out-Null
}
Write-Host "  Categories: $($categories.Count) uploaded" -ForegroundColor Green
Write-Host ""

# Upload wallpapers
Write-Host "Uploading wallpapers..." -ForegroundColor Yellow
foreach ($wall in $wallpapers) {
    Add-Document -collection "wallpapers" -docId $wall.id -data $wall | Out-Null
}
Write-Host "  Wallpapers: $($wallpapers.Count) uploaded" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Done! Restart the app to see wallpapers" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
