package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Unsplash Photo model
 * 
 * Represents a photo from Unsplash API
 * All photos are free to use under the Unsplash License
 * Attribution is required when displaying photos
 */
data class UnsplashPhoto(
    val id: String,
    val slug: String? = null,
    val width: Int,
    val height: Int,
    val color: String? = null,
    @SerializedName("blur_hash")
    val blurHash: String? = null,
    val description: String? = null,
    @SerializedName("alt_description")
    val altDescription: String? = null,
    val urls: UnsplashUrls,
    val links: UnsplashLinks,
    val user: UnsplashUser,
    val likes: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val downloads: Int? = null,
    val views: Int? = null,
    val tags: List<UnsplashTag>? = null
)

/**
 * Photo URLs at different resolutions
 * 
 * raw: Original image (very large)
 * full: Full resolution JPEG
 * regular: 1080px width
 * small: 400px width
 * thumb: 200px width
 */
data class UnsplashUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
    @SerializedName("small_s3")
    val smallS3: String? = null
)

/**
 * Photo links for API interactions
 */
data class UnsplashLinks(
    val self: String,
    val html: String,
    val download: String,
    @SerializedName("download_location")
    val downloadLocation: String
)

/**
 * Photographer information
 * 
 * IMPORTANT: Attribution is required - display photographer name and link
 */
data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String,
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("portfolio_url")
    val portfolioUrl: String? = null,
    @SerializedName("profile_image")
    val profileImage: UnsplashProfileImage? = null,
    val links: UnsplashUserLinks? = null
)

data class UnsplashProfileImage(
    val small: String,
    val medium: String,
    val large: String
)

data class UnsplashUserLinks(
    val self: String,
    val html: String,
    val photos: String,
    val likes: String,
    val portfolio: String? = null
)

/**
 * Photo tag
 */
data class UnsplashTag(
    val type: String? = null,
    val title: String? = null,
    val source: UnsplashTagSource? = null
)

data class UnsplashTagSource(
    val ancestry: UnsplashAncestry? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null
)

data class UnsplashAncestry(
    val type: UnsplashAncestryType? = null,
    val category: UnsplashAncestryType? = null,
    val subcategory: UnsplashAncestryType? = null
)

data class UnsplashAncestryType(
    val slug: String? = null,
    val prettySlug: String? = null
)

/**
 * Search response wrapper
 */
data class UnsplashSearchResponse(
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    val results: List<UnsplashPhoto>
)

/**
 * Example API Response:
 * 
 * GET https://api.unsplash.com/photos?per_page=2
 * 
 * [
 *   {
 *     "id": "LBI7cgq3pbM",
 *     "slug": "a-woman-standing-in-the-rain",
 *     "width": 5000,
 *     "height": 3333,
 *     "color": "#0d0c0c",
 *     "blur_hash": "LSC%a}-;M{RP~qxaxaof4nNGj[j@",
 *     "description": "Black wallpaper",
 *     "alt_description": "Dark minimal abstract",
 *     "urls": {
 *       "raw": "https://images.unsplash.com/photo-abc123?ixid=...",
 *       "full": "https://images.unsplash.com/photo-abc123?ixid=...&q=80",
 *       "regular": "https://images.unsplash.com/photo-abc123?ixid=...&w=1080&q=80",
 *       "small": "https://images.unsplash.com/photo-abc123?ixid=...&w=400&q=80",
 *       "thumb": "https://images.unsplash.com/photo-abc123?ixid=...&w=200&q=80"
 *     },
 *     "links": {
 *       "self": "https://api.unsplash.com/photos/LBI7cgq3pbM",
 *       "html": "https://unsplash.com/photos/LBI7cgq3pbM",
 *       "download": "https://unsplash.com/photos/LBI7cgq3pbM/download",
 *       "download_location": "https://api.unsplash.com/photos/LBI7cgq3pbM/download"
 *     },
 *     "user": {
 *       "id": "pXhwzz1JtQU",
 *       "username": "photographer123",
 *       "name": "John Doe",
 *       "links": {
 *         "html": "https://unsplash.com/@photographer123"
 *       }
 *     },
 *     "likes": 1234,
 *     "created_at": "2024-01-15T10:30:00Z",
 *     "tags": [
 *       { "title": "black" },
 *       { "title": "dark" },
 *       { "title": "minimal" }
 *     ]
 *   }
 * ]
 * 
 * GET https://api.unsplash.com/search/photos?query=black+dark+amoled&per_page=2
 * 
 * {
 *   "total": 10000,
 *   "total_pages": 334,
 *   "results": [
 *     { ... photo object ... }
 *   ]
 * }
 */
