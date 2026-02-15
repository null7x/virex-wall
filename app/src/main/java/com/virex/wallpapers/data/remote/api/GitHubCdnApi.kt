package com.virex.wallpapers.data.remote.api

import com.virex.wallpapers.data.remote.model.GitHubCdnWallpaper
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * GitHub CDN API Service
 *
 * Loads wallpapers from a JSON file hosted on GitHub RAW. This is the PRIMARY data source — works
 * globally including Russia.
 *
 * Primary URL: https://raw.githubusercontent.com/null7x/virex-wallpapers/main/wallpapers.json
 * 
 * Alternative mirrors for Russia (raw.githubusercontent.com may be blocked):
 * - jsdelivr CDN (works in Russia)
 * - GitHub Pages (if configured)
 * - gitcdn.xyz
 */
interface GitHubCdnApi {

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/null7x/virex-wallpapers/main/"
        
        // Alternative CDN mirrors that work in Russia
        val MIRROR_URLS = listOf(
            // Primary - raw.githubusercontent.com
            "https://raw.githubusercontent.com/null7x/virex-wallpapers/main/wallpapers.json",
            // jsdelivr CDN - works in Russia
            "https://cdn.jsdelivr.net/gh/null7x/virex-wallpapers@main/wallpapers.json",
            // Statically CDN - another alternative
            "https://cdn.statically.io/gh/null7x/virex-wallpapers/main/wallpapers.json",
            // GitHub Pages (if configured)
            "https://null7x.github.io/virex-wallpapers/wallpapers.json"
        )
    }

    /** Fetch wallpaper catalog from GitHub CDN */
    @GET("wallpapers.json")
    suspend fun getWallpapers(): List<GitHubCdnWallpaper>
    
    /** Fetch from any URL (for mirror support) */
    @GET
    suspend fun getWallpapersFromUrl(@Url url: String): List<GitHubCdnWallpaper>
}
