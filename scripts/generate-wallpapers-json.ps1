# Generate wallpapers.json for GitHub CDN
# Run this after copying images to virex-wallpapers repo

$baseUrl = "https://cdn.jsdelivr.net/gh/null7x/virex-wallpapers@main/images"
$assetsPath = "..\assets\Wallpacks"
$outputFile = "wallpapers.json"

$categories = @("abstract", "anime", "cars", "city", "cyberpunk", "dark", "fantasy", "games", "minimal", "mountains", "nature", "space", "technology")

$wallpapers = @()
$id = 1

foreach ($category in $categories) {
    $categoryPath = Join-Path $assetsPath $category
    if (Test-Path $categoryPath) {
        $files = Get-ChildItem -Path $categoryPath -Filter "*.jpg" | Select-Object -First 30
        foreach ($file in $files) {
            $isPro = ($id % 5 -eq 0) # Every 5th is PRO
            $wallpapers += @{
                id       = $id.ToString()
                image    = "$baseUrl/$category/$($file.Name)"
                thumb    = "$baseUrl/$category/$($file.Name)"
                category = $category
                isPro    = $isPro
            }
            $id++
        }
    }
}

$json = $wallpapers | ConvertTo-Json -Depth 3
$json | Out-File -FilePath $outputFile -Encoding utf8

Write-Host "Generated $($wallpapers.Count) wallpapers to $outputFile"
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Clone: git clone https://github.com/null7x/virex-wallpapers.git"
Write-Host "2. Create folder: mkdir images"
Write-Host "3. Copy wallpapers: cp -r ../assets/Wallpacks/* images/"
Write-Host "4. Copy JSON: cp wallpapers.json ../virex-wallpapers/"
Write-Host "5. Push: git add . && git commit -m 'Add wallpapers' && git push"
