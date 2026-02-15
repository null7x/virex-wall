# ============================================================================
# VIREX Wallpapers - Seed Wallpacks to Firebase
# ============================================================================
# This script reads wallpapers from assets/Wallpacks and uploads metadata
# to Firebase Firestore. Images are referenced via wallhaven.cc CDN URLs
# or raw GitHub URLs (for rose-pine collection).
#
# Usage: .\scripts\seed-wallpacks.ps1
# ============================================================================

$projectId = "virex-wallpapers"
$apiKey = "AIzaSyBj61px2TYjSp9SbuiTuGZbA3c2k7ofs_g"
$baseUrl = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
$wallpacksDir = Join-Path (Join-Path (Join-Path $PSScriptRoot "..") "assets") "Wallpacks"

# ── Firestore helpers ──────────────────────────────────────────────────────

function ConvertTo-FirestoreValue {
    param($value)
    if ($null -eq $value) { return @{ "nullValue" = $null } }
    elseif ($value -is [bool]) { return @{ "booleanValue" = $value } }
    elseif ($value -is [int] -or $value -is [long] -or $value -is [double]) { return @{ "integerValue" = $value.ToString() } }
    elseif ($value -is [array]) {
        $arrayValues = @()
        foreach ($item in $value) { $arrayValues += ConvertTo-FirestoreValue $item }
        return @{ "arrayValue" = @{ "values" = $arrayValues } }
    }
    else { return @{ "stringValue" = $value.ToString() } }
}

function ConvertTo-FirestoreDocument {
    param($data)
    $fields = @{}
    foreach ($key in $data.Keys) { $fields[$key] = ConvertTo-FirestoreValue $data[$key] }
    return @{ "fields" = $fields }
}

function Add-Document {
    param([string]$collection, [string]$docId, [hashtable]$data)
    $url = "$baseUrl/${collection}?documentId=$docId&key=$apiKey"
    $body = ConvertTo-FirestoreDocument $data | ConvertTo-Json -Depth 10
    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json"
        return $true
    }
    catch {
        # Check if document already exists (409 Conflict) and try to update
        if ($_.Exception.Response.StatusCode -eq 409) {
            try {
                $patchUrl = "$baseUrl/${collection}/$docId`?key=$apiKey"
                $response = Invoke-RestMethod -Uri $patchUrl -Method Patch -Body $body -ContentType "application/json"
                return $true
            }
            catch {
                Write-Host "    x FAIL $collection/$docId - $($_.Exception.Message)" -ForegroundColor Red
                return $false
            }
        }
        Write-Host "    x FAIL $collection/$docId - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# ── Wallhaven URL helpers ─────────────────────────────────────────────────

function Get-WallhavenThumbnail {
    param([string]$fullUrl)
    # Full:  https://w.wallhaven.cc/full/x1/wallhaven-x1kl5o.png
    # Thumb: https://th.wallhaven.cc/lg/x1/x1kl5o.jpg
    if ($fullUrl -match "wallhaven-([a-z0-9]+)\.\w+$") {
        $code = $Matches[1]
        $prefix = $code.Substring(0, 2)
        return "https://th.wallhaven.cc/lg/$prefix/$code.jpg"
    }
    return $fullUrl
}

function Get-WallhavenId {
    param([string]$url)
    if ($url -match "wallhaven-([a-z0-9]+)\.\w+$") {
        return $Matches[1]
    }
    return $null
}

# ── Category definitions ──────────────────────────────────────────────────

$categoryMeta = @{
    "abstract"   = @{ name = "Abstract"; description = "Abstract art and patterns"; sort = 1; icon = "palette" }
    "animals"    = @{ name = "Animals"; description = "Wildlife and animal wallpapers"; sort = 2; icon = "pets" }
    "anime"      = @{ name = "Anime"; description = "Anime and manga artwork"; sort = 3; icon = "animation" }
    "cars"       = @{ name = "Cars"; description = "Supercars and automotive"; sort = 4; icon = "directions_car" }
    "city"       = @{ name = "City"; description = "Urban landscapes and cityscapes"; sort = 5; icon = "location_city" }
    "cyberpunk"  = @{ name = "Cyberpunk"; description = "Cyberpunk and neon aesthetics"; sort = 6; icon = "electric_bolt" }
    "dark"       = @{ name = "Dark"; description = "Dark and moody wallpapers"; sort = 7; icon = "dark_mode" }
    "fantasy"    = @{ name = "Fantasy"; description = "Fantasy worlds and magical scenes"; sort = 8; icon = "auto_awesome" }
    "games"      = @{ name = "Games"; description = "Video game art and screenshots"; sort = 9; icon = "sports_esports" }
    "general"    = @{ name = "General"; description = "General wallpapers"; sort = 10; icon = "wallpaper" }
    "minimal"    = @{ name = "Minimal"; description = "Clean and minimalist designs"; sort = 11; icon = "crop_square" }
    "mountains"  = @{ name = "Mountains"; description = "Mountain landscapes and peaks"; sort = 12; icon = "terrain" }
    "nature"     = @{ name = "Nature"; description = "Nature and landscapes"; sort = 13; icon = "forest" }
    "people"     = @{ name = "People"; description = "People and portraits"; sort = 14; icon = "person" }
    "space"      = @{ name = "Space"; description = "Space, galaxies and cosmos"; sort = 15; icon = "rocket_launch" }
    "technology" = @{ name = "Technology"; description = "Tech and digital art"; sort = 16; icon = "memory" }
    "rosepine"   = @{ name = "Rosé Pine"; description = "Rosé Pine themed wallpapers"; sort = 17; icon = "local_florist" }
}

# ── Main script ───────────────────────────────────────────────────────────

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  VIREX Wallpapers - Seed Wallpacks to Firebase" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
$totalWallpapers = 0
$totalCategories = 0
$allCategoriesData = @()

# ── Step 1: Scan categories and collect wallpaper data ────────────────────

Write-Host "[1/3] Scanning Wallpacks folder..." -ForegroundColor Yellow
Write-Host ""

$categoryWallpapers = @{}

# Process standard categories (with downloaded_urls.txt)
$categoryDirs = Get-ChildItem -Path $wallpacksDir -Directory | Where-Object { $_.Name -ne "PlaySafe" }

foreach ($catDir in $categoryDirs) {
    $catId = $catDir.Name.ToLower()
    $urlsFile = Join-Path $catDir.FullName "downloaded_urls.txt"
    $urls = @()

    if (Test-Path $urlsFile) {
        $urls = Get-Content $urlsFile | Where-Object { $_.Trim() -ne "" } | ForEach-Object { $_.Trim() }
    }

    # Also count image files
    $imageFiles = Get-ChildItem -Path $catDir.FullName -File | Where-Object {
        $_.Extension -match "\.(jpg|jpeg|png|webp)$"
    }
    $imageCount = $imageFiles.Count

    if ($urls.Count -eq 0 -and $imageCount -eq 0) {
        Write-Host "  [ ] $catId - empty, skipping" -ForegroundColor DarkGray
        continue
    }

    $categoryWallpapers[$catId] = @{
        urls        = $urls
        imageCount  = $imageCount
        isWallhaven = $true
    }

    Write-Host "  [+] $catId - $($urls.Count) URLs, $imageCount images" -ForegroundColor Green
}

# Process rose-pine (special: GitHub-hosted images)
$rosePineDir = Join-Path (Join-Path $wallpacksDir "PlaySafe") "rose-pine"
if (Test-Path $rosePineDir) {
    $rpImages = Get-ChildItem -Path $rosePineDir -File | Where-Object {
        $_.Extension -match "\.(jpg|jpeg|png|webp)$" -and $_.Name -notmatch "^\.git"
    }
    if ($rpImages.Count -gt 0) {
        $rpUrls = @()
        foreach ($img in $rpImages) {
            # GitHub raw URL for the rose-pine/wallpapers repo
            $encodedName = [Uri]::EscapeDataString($img.Name)
            $rpUrls += "https://raw.githubusercontent.com/rose-pine/wallpapers/main/$encodedName"
        }
        $categoryWallpapers["rosepine"] = @{
            urls        = $rpUrls
            imageCount  = $rpImages.Count
            imageNames  = $rpImages | ForEach-Object { $_.Name }
            isWallhaven = $false
        }
        Write-Host "  [+] rosepine - $($rpImages.Count) images (GitHub)" -ForegroundColor Magenta
    }
}

Write-Host ""

# ── Step 2: Upload categories ─────────────────────────────────────────────

Write-Host "[2/3] Uploading categories to Firestore..." -ForegroundColor Yellow

foreach ($catId in $categoryWallpapers.Keys) {
    $meta = $categoryMeta[$catId]
    if (-not $meta) {
        Write-Host "  ? No metadata for $catId, using defaults" -ForegroundColor DarkYellow
        $meta = @{ name = $catId; description = "$catId wallpapers"; sort = 99; icon = "wallpaper" }
    }

    $wallpaperCount = $categoryWallpapers[$catId].urls.Count
    if ($wallpaperCount -eq 0) { $wallpaperCount = $categoryWallpapers[$catId].imageCount }

    # Use first wallpaper URL as category thumbnail
    $coverUrl = ""
    if ($categoryWallpapers[$catId].isWallhaven -and $categoryWallpapers[$catId].urls.Count -gt 0) {
        $coverUrl = Get-WallhavenThumbnail -fullUrl $categoryWallpapers[$catId].urls[0]
    }
    elseif ($categoryWallpapers[$catId].urls.Count -gt 0) {
        $coverUrl = $categoryWallpapers[$catId].urls[0]
    }

    $catData = @{
        id              = $catId
        name            = $meta.name
        description     = $meta.description
        thumbnail_url   = $coverUrl
        cover_url       = $coverUrl
        sort_order      = $meta.sort
        icon            = $meta.icon
        is_visible      = $true
        wallpaper_count = $wallpaperCount
        created_at      = $timestamp
        updated_at      = $timestamp
    }

    $result = Add-Document -collection "categories" -docId $catId -data $catData
    if ($result) {
        Write-Host "  + $($meta.name) ($wallpaperCount wallpapers)" -ForegroundColor Green
        $totalCategories++
    }
}

Write-Host "  => $totalCategories categories uploaded" -ForegroundColor Green
Write-Host ""

# ── Step 3: Upload wallpapers ─────────────────────────────────────────────

Write-Host "[3/3] Uploading wallpapers to Firestore..." -ForegroundColor Yellow
Write-Host "  This may take a while for large collections..." -ForegroundColor DarkGray
Write-Host ""

$globalIndex = 0
$successCount = 0
$failCount = 0
$seenIds = @{}

foreach ($catId in $categoryWallpapers.Keys) {
    $catInfo = $categoryWallpapers[$catId]
    $meta = $categoryMeta[$catId]
    if (-not $meta) { $meta = @{ name = $catId; description = ""; sort = 99 } }

    $urls = $catInfo.urls
    $catSuccess = 0

    Write-Host "  [$($meta.name)]" -ForegroundColor Cyan

    for ($i = 0; $i -lt $urls.Count; $i++) {
        $url = $urls[$i]
        $globalIndex++

        # Generate unique wallpaper ID
        if ($catInfo.isWallhaven) {
            $whId = Get-WallhavenId -url $url
            if ($whId) {
                $wallId = "wh_$whId"
            }
            else {
                $wallId = "${catId}_$($i + 1)"
            }
            $thumbnailUrl = Get-WallhavenThumbnail -fullUrl $url
            $title = "$($meta.name) #$($i + 1)"

            # Extract file extension
            $ext = "jpg"
            if ($url -match "\.(\w+)$") { $ext = $Matches[1] }
        }
        else {
            # rose-pine or other sources
            $fileName = ""
            if ($catInfo.imageNames -and $i -lt $catInfo.imageNames.Count) {
                $fileName = $catInfo.imageNames[$i]
            }
            $safeName = $fileName -replace "\.[^.]+$", "" -replace "[^a-zA-Z0-9_-]", "_"
            $wallId = "rp_$safeName"
            $thumbnailUrl = $url
            $title = ($fileName -replace "\.[^.]+$", "" -replace "[_-]", " ")
            $ext = "png"
            if ($url -match "\.(\w+)$") { $ext = $Matches[1].ToLower() }
        }

        # Skip duplicates (same wallhaven image in multiple categories)
        # But still add to this category
        if ($seenIds.ContainsKey($wallId)) {
            # Create a category-specific variant
            $wallId = "${catId}_${wallId}"
        }
        $seenIds[$wallId] = $true

        # Determine tags based on category
        $tags = @($catId)
        if ($catInfo.isWallhaven) {
            $tags += "wallhaven"
        }
        if ($catId -eq "rosepine") {
            $tags += @("rosepine", "aesthetic", "minimal")
        }

        # Determine if featured/trending (first few in each category)
        $isFeatured = ($i -lt 5)
        $isTrending = ($i -lt 3)

        $wallData = @{
            id            = $wallId
            title         = $title
            description   = "$($meta.name) wallpaper"
            thumbnail_url = $thumbnailUrl
            full_url      = $url
            category_id   = $catId
            category_name = $meta.name
            width         = 1920
            height        = 2880
            file_size     = 500000
            downloads     = 0
            likes         = [int](Get-Random -Minimum 50 -Maximum 500)
            is_premium    = $false
            is_featured   = $isFeatured
            is_trending   = $isTrending
            tags          = $tags
            created_at    = $timestamp - ($i * 60000)
            updated_at    = $timestamp
        }

        $result = Add-Document -collection "wallpapers" -docId $wallId -data $wallData
        if ($result) {
            $catSuccess++
            $successCount++
        }
        else {
            $failCount++
        }

        # Progress indicator every 25 wallpapers
        if ($globalIndex % 25 -eq 0) {
            Write-Host "    ... $globalIndex wallpapers processed" -ForegroundColor DarkGray
        }
    }

    Write-Host "    => $catSuccess/$($urls.Count) uploaded" -ForegroundColor $(if ($catSuccess -eq $urls.Count) { "Green" } else { "Yellow" })
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  SEED COMPLETE!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Categories:  $totalCategories" -ForegroundColor White
Write-Host "  Wallpapers:  $successCount uploaded, $failCount failed" -ForegroundColor White
Write-Host "  Total docs:  $($totalCategories + $successCount)" -ForegroundColor White
Write-Host ""
Write-Host "  Restart the app to see the new wallpapers!" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
