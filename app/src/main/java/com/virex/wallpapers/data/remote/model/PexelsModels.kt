package com.virex.wallpapers.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Pexels Photo model
 * 
 * All photos are free to use for personal and commercial purposes
 * No attribution required (but appreciated)
 */
data class PexelsPhoto(
    val id: Long,
    val width: Int,
    val height: Int,
    val url: String,
    val photographer: String,
    @SerializedName("photographer_url")
    val photographerUrl: String,
    @SerializedName("photographer_id")
    val photographerId: Long,
    @SerializedName("avg_color")
    val avgColor: String,
    val src: PexelsPhotoSrc,
    val alt: String? = null,
    val liked: Boolean = false
)

/**
 * Photo source URLs at different resolutions
 * 
 * original: Original size (can be very large)
 * large2x: 1880px width
 * large: 940px width
 * medium: 350px height
 * small: 130px height
 * portrait: 800x1200px (cropped)
 * landscape: 1200x627px (cropped)
 * tiny: 280x200px
 */
data class PexelsPhotoSrc(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)

/**
 * Search/Curated response wrapper
 */
data class PexelsSearchResponse(
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val photos: List<PexelsPhoto>,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("next_page")
    val nextPage: String? = null,
    @SerializedName("prev_page")
    val prevPage: String? = null
)

/**
 * Example API Response:
 * 
 * GET https://api.pexels.com/v1/search?query=dark+black+minimal&per_page=2&orientation=portrait
 * 
 * {
 *   "page": 1,
 *   "per_page": 2,
 *   "total_results": 8000,
 *   "next_page": "https://api.pexels.com/v1/search?query=dark+black+minimal&per_page=2&page=2",
 *   "photos": [
 *     {
 *       "id": 1234567,
 *       "width": 3648,
 *       "height": 5472,
 *       "url": "https://www.pexels.com/photo/1234567/",
 *       "photographer": "Jane Smith",
 *       "photographer_url": "https://www.pexels.com/@janesmith",
 *       "photographer_id": 12345,
 *       "avg_color": "#0A0A0A",
 *       "src": {
 *         "original": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg",
 *         "large2x": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
 *         "large": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&h=650&w=940",
 *         "medium": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&h=350",
 *         "small": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&h=130",
 *         "portrait": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&fit=crop&h=1200&w=800",
 *         "landscape": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&fit=crop&h=627&w=1200",
 *         "tiny": "https://images.pexels.com/photos/1234567/pexels-photo-1234567.jpeg?auto=compress&cs=tinysrgb&dpr=1&fit=crop&h=200&w=280"
 *       },
 *       "alt": "Dark abstract wallpaper"
 *     }
 *   ]
 * }
 * 
 * GET https://api.pexels.com/v1/curated?per_page=2
 * 
 * {
 *   "page": 1,
 *   "per_page": 2,
 *   "total_results": 8000,
 *   "photos": [ ... ]
 * }
 */
