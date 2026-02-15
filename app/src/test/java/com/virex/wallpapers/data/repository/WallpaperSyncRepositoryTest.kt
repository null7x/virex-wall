package com.virex.wallpapers.data.repository

import com.virex.wallpapers.BuildConfig
import com.virex.wallpapers.data.local.SyncedWallpaperDao
import com.virex.wallpapers.data.model.SyncCategory
import com.virex.wallpapers.data.model.SyncStatus
import com.virex.wallpapers.data.model.SyncedWallpaper
import com.virex.wallpapers.data.model.WallpaperSource
import com.virex.wallpapers.data.remote.api.PexelsApi
import com.virex.wallpapers.data.remote.api.PicsumApi
import com.virex.wallpapers.data.remote.api.UnsplashApi
import com.virex.wallpapers.data.remote.api.UnsplashDownloadResponse
import com.virex.wallpapers.data.remote.api.WallhavenApi
import com.virex.wallpapers.data.remote.model.PexelsPhoto
import com.virex.wallpapers.data.remote.model.PexelsPhotoSrc
import com.virex.wallpapers.data.remote.model.PexelsSearchResponse
import com.virex.wallpapers.data.remote.model.PicsumPhoto
import com.virex.wallpapers.data.remote.model.UnsplashLinks
import com.virex.wallpapers.data.remote.model.UnsplashPhoto
import com.virex.wallpapers.data.remote.model.UnsplashSearchResponse
import com.virex.wallpapers.data.remote.model.UnsplashUrls
import com.virex.wallpapers.data.remote.model.UnsplashUser
import com.virex.wallpapers.data.remote.model.WallhavenSearchResponse
import com.virex.wallpapers.data.remote.model.WallhavenWallpaper
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
class WallpaperSyncRepositoryTest {

    @Test
    fun performSync_returnsSuccessWhenSourcesSucceedWithNoNewWallpapers() = runTest {
        val dao = FakeSyncedWallpaperDao()
        val repository =
                WallpaperSyncRepository(
                        unsplashApi = FakeUnsplashApi(),
                        pexelsApi = FakePexelsApi(),
                        wallhavenApi = FakeWallhavenApi(),
                        picsumApi = FakePicsumApi(),
                        syncedWallpaperDao = dao
                )

        val result = repository.performSync()

        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).newCount)
        assertNull(dao.getSyncStatusSync()?.lastError)
    }

    @Test
    fun performSync_returnsErrorWhenAllAvailableSourcesFail() = runTest {
        val dao = FakeSyncedWallpaperDao()
        val repository =
                WallpaperSyncRepository(
                        unsplashApi = FakeUnsplashApi(searchResponse = errorResponse(500)),
                        pexelsApi = FakePexelsApi(searchResponse = errorResponse(500)),
                        wallhavenApi = FakeWallhavenApi(throwOnSearch = true),
                        picsumApi = FakePicsumApi(throwOnGetPhotos = true),
                        syncedWallpaperDao = dao
                )

        val result = repository.performSync()

        assertTrue(result is SyncResult.Error)
        assertTrue((result as SyncResult.Error).message.contains("All sources failed"))
    }

    @Test
    fun performSync_deduplicatesUsingPrefixedUnsplashAndPexelsIds() = runTest {
        assumeTrue(
                BuildConfig.UNSPLASH_ACCESS_KEY.isNotBlank() &&
                        BuildConfig.PEXELS_API_KEY.isNotBlank()
        )

        val dao = FakeSyncedWallpaperDao()
        dao.seed(
                syncedWallpaper(
                        id = "unsplash_u1",
                        sourceId = "u1",
                        source = WallpaperSource.UNSPLASH
                ),
                syncedWallpaper(id = "pexels_7", sourceId = "7", source = WallpaperSource.PEXELS)
        )

        val repository =
                WallpaperSyncRepository(
                        unsplashApi =
                                FakeUnsplashApi(
                                        searchResponse =
                                                Response.success(
                                                        UnsplashSearchResponse(
                                                                total = 1,
                                                                totalPages = 1,
                                                                results = listOf(unsplashPhoto("u1"))
                                                        )
                                                )
                                ),
                        pexelsApi =
                                FakePexelsApi(
                                        searchResponse =
                                                Response.success(
                                                        PexelsSearchResponse(
                                                                page = 1,
                                                                perPage = 30,
                                                                photos = listOf(pexelsPhoto(7)),
                                                                totalResults = 1
                                                        )
                                                )
                                ),
                        wallhavenApi = FakeWallhavenApi(),
                        picsumApi = FakePicsumApi(),
                        syncedWallpaperDao = dao
                )

        val result = repository.performSync()

        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).newCount)
        assertFalse(dao.insertedIds.contains("unsplash_u1"))
        assertFalse(dao.insertedIds.contains("pexels_7"))
    }

    @Test
    fun performSync_countsOnlyActuallyInsertedRows() = runTest {
        assumeTrue(BuildConfig.UNSPLASH_ACCESS_KEY.isNotBlank())

        val dao = FakeSyncedWallpaperDao()
        dao.forcedInsertResults = listOf(-1L)
        val repository =
                WallpaperSyncRepository(
                        unsplashApi =
                                FakeUnsplashApi(
                                        searchResponse =
                                                Response.success(
                                                        UnsplashSearchResponse(
                                                                total = 1,
                                                                totalPages = 1,
                                                                results = listOf(unsplashPhoto("u2"))
                                                        )
                                                )
                                ),
                        pexelsApi = FakePexelsApi(),
                        wallhavenApi = FakeWallhavenApi(),
                        picsumApi = FakePicsumApi(),
                        syncedWallpaperDao = dao
                )

        val result = repository.performSync()

        assertTrue(result is SyncResult.Success)
        assertEquals(0, (result as SyncResult.Success).newCount)
    }

    @Test
    fun performSync_singleFlightPreventsParallelSecondSync() = runTest {
        val dao = FakeSyncedWallpaperDao()
        val wallhavenApi = FakeWallhavenApi(delayMs = 100)
        val repository =
                WallpaperSyncRepository(
                        unsplashApi = FakeUnsplashApi(),
                        pexelsApi = FakePexelsApi(),
                        wallhavenApi = wallhavenApi,
                        picsumApi = FakePicsumApi(),
                        syncedWallpaperDao = dao
                )

        val first = async { repository.performSync() }
        while (wallhavenApi.callCount == 0) {
            delay(10)
        }

        val second = repository.performSync()
        val firstResult = first.await()

        assertTrue(firstResult is SyncResult.Success)
        assertTrue(second is SyncResult.Success)
        assertEquals(0, (second as SyncResult.Success).newCount)
        assertEquals(5, wallhavenApi.callCount)
    }

    private class FakeSyncedWallpaperDao : SyncedWallpaperDao {
        private val wallpapersById = linkedMapOf<String, SyncedWallpaper>()
        private val wallpapersFlow = MutableStateFlow<List<SyncedWallpaper>>(emptyList())
        private val syncStatusFlow = MutableStateFlow<SyncStatus?>(null)

        val insertedIds = mutableListOf<String>()
        var forcedInsertResults: List<Long>? = null

        fun seed(vararg wallpapers: SyncedWallpaper) {
            wallpapers.forEach { wallpapersById[it.id] = it }
            refreshWallpapersFlow()
        }

        override fun getAllSyncedWallpapers(): Flow<List<SyncedWallpaper>> = wallpapersFlow

        override fun getWallpapersByCategory(category: SyncCategory): Flow<List<SyncedWallpaper>> =
                wallpapersFlow.map { list -> list.filter { it.category == category } }

        override fun getNewWallpapers(since: Long, limit: Int): Flow<List<SyncedWallpaper>> =
                wallpapersFlow.map { list -> list.filter { it.syncedAt > since }.take(limit) }

        override fun getWallpapersBySource(source: WallpaperSource): Flow<List<SyncedWallpaper>> =
                wallpapersFlow.map { list -> list.filter { it.source == source } }

        override suspend fun getWallpaperById(id: String): SyncedWallpaper? = wallpapersById[id]

        override suspend fun exists(sourceId: String, source: WallpaperSource): Boolean =
                wallpapersById.values.any { it.sourceId == sourceId && it.source == source }

        override suspend fun getExistingSourceIds(source: WallpaperSource): List<String> =
                wallpapersById.values.filter { it.source == source }.map { it.sourceId }

        override suspend fun getAllWallpaperIds(): List<String> = wallpapersById.keys.toList()

        override suspend fun insertWallpapers(wallpapers: List<SyncedWallpaper>): List<Long> {
            val forced = forcedInsertResults
            forcedInsertResults = null

            val results =
                    wallpapers.mapIndexed { index, wallpaper ->
                        val result =
                                forced?.getOrNull(index)
                                        ?: if (wallpapersById.containsKey(wallpaper.id)) {
                                            -1L
                                        } else {
                                            (wallpapersById.size + 1L)
                                        }
                        if (result != -1L) {
                            wallpapersById[wallpaper.id] = wallpaper
                            insertedIds.add(wallpaper.id)
                        }
                        result
                    }

            refreshWallpapersFlow()
            return results
        }

        override suspend fun insertWallpaper(wallpaper: SyncedWallpaper): Long {
            if (wallpapersById.containsKey(wallpaper.id)) return -1L
            wallpapersById[wallpaper.id] = wallpaper
            insertedIds.add(wallpaper.id)
            refreshWallpapersFlow()
            return wallpapersById.size.toLong()
        }

        override suspend fun updateWallpaper(wallpaper: SyncedWallpaper) {
            wallpapersById[wallpaper.id] = wallpaper
            refreshWallpapersFlow()
        }

        override suspend fun markAsViewed(id: String) {
            wallpapersById[id]?.let { wallpapersById[id] = it.copy(viewed = true) }
            refreshWallpapersFlow()
        }

        override suspend fun updateCachePath(id: String, path: String) {
            wallpapersById[id]?.let {
                wallpapersById[id] = it.copy(localCachePath = path, isCached = true)
            }
            refreshWallpapersFlow()
        }

        override suspend fun deleteWallpaper(id: String) {
            wallpapersById.remove(id)
            refreshWallpapersFlow()
        }

        override suspend fun deleteOldWallpapers(before: Long): Int {
            val toDelete =
                    wallpapersById.values.filter { it.syncedAt < before && !it.isCached }.map { it.id }
            toDelete.forEach { wallpapersById.remove(it) }
            refreshWallpapersFlow()
            return toDelete.size
        }

        override fun getWallpaperCount(): Flow<Int> = wallpapersFlow.map { it.size }

        override suspend fun getCountByCategory(category: SyncCategory): Int =
                wallpapersById.values.count { it.category == category }

        override fun searchWallpapers(query: String): Flow<List<SyncedWallpaper>> =
                wallpapersFlow.map { list ->
                    list.filter {
                        (it.description?.contains(query, ignoreCase = true) == true) ||
                                it.photographerName.contains(query, ignoreCase = true) ||
                                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    }
                }

        override fun getUnviewedCount(): Flow<Int> = wallpapersFlow.map { list -> list.count { !it.viewed } }

        override suspend fun clearAll() {
            wallpapersById.clear()
            refreshWallpapersFlow()
        }

        override fun getSyncStatus(): Flow<SyncStatus?> = syncStatusFlow

        override suspend fun getSyncStatusSync(): SyncStatus? = syncStatusFlow.value

        override suspend fun updateSyncStatus(status: SyncStatus) {
            syncStatusFlow.value = status
        }

        override suspend fun updateLastSync(timestamp: Long, source: String, count: Int) {
            val current = syncStatusFlow.value ?: SyncStatus()
            syncStatusFlow.value =
                    current.copy(
                            lastSyncAt = timestamp,
                            lastSyncSource = source,
                            lastSyncCount = count,
                            isSyncing = false,
                            lastError = null,
                            totalSynced = current.totalSynced + count
                    )
        }

        override suspend fun setSyncing(syncing: Boolean) {
            val current = syncStatusFlow.value ?: SyncStatus()
            syncStatusFlow.update { current.copy(isSyncing = syncing) }
        }

        override suspend fun setSyncError(error: String) {
            val current = syncStatusFlow.value ?: SyncStatus()
            syncStatusFlow.update { current.copy(isSyncing = false, lastError = error) }
        }

        override suspend fun updatePagination(unsplashPage: Int, pexelsPage: Int) {
            val current = syncStatusFlow.value ?: SyncStatus()
            syncStatusFlow.value =
                    current.copy(unsplashPage = unsplashPage, pexelsPage = pexelsPage)
        }

        private fun refreshWallpapersFlow() {
            wallpapersFlow.value = wallpapersById.values.sortedByDescending { it.syncedAt }
        }
    }

    private class FakeWallhavenApi(
            private val throwOnSearch: Boolean = false,
            private val delayMs: Long = 0L,
            private val wallpapers: List<WallhavenWallpaper> = emptyList()
    ) : WallhavenApi {
        var callCount = 0

        override suspend fun searchWallpapers(
                query: String,
                categories: String,
                purity: String,
                sorting: String,
                order: String,
                page: Int,
                ratios: String,
                atleast: String
        ): WallhavenSearchResponse {
            callCount++
            if (delayMs > 0) delay(delayMs)
            if (throwOnSearch) throw IOException("Wallhaven unavailable")
            return WallhavenSearchResponse(data = wallpapers, meta = null)
        }
    }

    private class FakePicsumApi(
            private val throwOnGetPhotos: Boolean = false,
            private val photos: List<PicsumPhoto> = emptyList()
    ) : PicsumApi {
        override suspend fun getPhotos(page: Int, limit: Int): List<PicsumPhoto> {
            if (throwOnGetPhotos) throw IOException("Picsum unavailable")
            return photos
        }
    }

    private class FakeUnsplashApi(
            private val searchResponse: Response<UnsplashSearchResponse> =
                    Response.success(UnsplashSearchResponse(0, 0, emptyList()))
    ) : UnsplashApi {
        override suspend fun getPhotos(
                clientId: String,
                page: Int,
                perPage: Int,
                orderBy: String
        ): Response<List<UnsplashPhoto>> = Response.success(emptyList())

        override suspend fun searchPhotos(
                clientId: String,
                query: String,
                page: Int,
                perPage: Int,
                color: String?,
                orientation: String?
        ): Response<UnsplashSearchResponse> = searchResponse

        override suspend fun getPhoto(clientId: String, photoId: String): Response<UnsplashPhoto> =
                Response.success(
                        UnsplashPhoto(
                                id = photoId,
                                width = 1080,
                                height = 1920,
                                urls =
                                        UnsplashUrls(
                                                raw = "https://images.unsplash.com/raw/$photoId",
                                                full = "https://images.unsplash.com/full/$photoId",
                                                regular =
                                                        "https://images.unsplash.com/regular/$photoId",
                                                small = "https://images.unsplash.com/small/$photoId",
                                                thumb = "https://images.unsplash.com/thumb/$photoId"
                                        ),
                                links =
                                        UnsplashLinks(
                                                self = "https://api.unsplash.com/photos/$photoId",
                                                html = "https://unsplash.com/photos/$photoId",
                                                download =
                                                        "https://unsplash.com/photos/$photoId/download",
                                                downloadLocation =
                                                        "https://api.unsplash.com/photos/$photoId/download"
                                        ),
                                user = UnsplashUser(id = "user", username = "user", name = "User Name")
                        )
                )

        override suspend fun getRandomPhotos(
                clientId: String,
                query: String?,
                count: Int,
                orientation: String
        ): Response<List<UnsplashPhoto>> = Response.success(emptyList())

        override suspend fun trackDownload(
                clientId: String,
                photoId: String
        ): Response<UnsplashDownloadResponse> =
                Response.success(UnsplashDownloadResponse(url = "https://unsplash.com"))
    }

    private class FakePexelsApi(
            private val searchResponse: Response<PexelsSearchResponse> =
                    Response.success(PexelsSearchResponse(1, 30, emptyList(), 0))
    ) : PexelsApi {
        override suspend fun getCuratedPhotos(
                apiKey: String,
                page: Int,
                perPage: Int
        ): Response<PexelsSearchResponse> = Response.success(PexelsSearchResponse(1, 30, emptyList(), 0))

        override suspend fun searchPhotos(
                apiKey: String,
                query: String,
                page: Int,
                perPage: Int,
                orientation: String?,
                color: String?
        ): Response<PexelsSearchResponse> = searchResponse

        override suspend fun getPhoto(apiKey: String, photoId: Int): Response<PexelsPhoto> =
                Response.success(
                        PexelsPhoto(
                                id = photoId.toLong(),
                                width = 1080,
                                height = 1920,
                                url = "https://www.pexels.com/photo/$photoId/",
                                photographer = "Photographer",
                                photographerUrl = "https://www.pexels.com/@photographer",
                                photographerId = 1L,
                                avgColor = "#000000",
                                src =
                                        PexelsPhotoSrc(
                                                original =
                                                        "https://images.pexels.com/original/$photoId.jpg",
                                                large2x =
                                                        "https://images.pexels.com/large2x/$photoId.jpg",
                                                large = "https://images.pexels.com/large/$photoId.jpg",
                                                medium = "https://images.pexels.com/medium/$photoId.jpg",
                                                small = "https://images.pexels.com/small/$photoId.jpg",
                                                portrait =
                                                        "https://images.pexels.com/portrait/$photoId.jpg",
                                                landscape =
                                                        "https://images.pexels.com/landscape/$photoId.jpg",
                                                tiny = "https://images.pexels.com/tiny/$photoId.jpg"
                                        )
                        )
                )
    }

    private fun syncedWallpaper(
            id: String,
            sourceId: String,
            source: WallpaperSource
    ): SyncedWallpaper =
            SyncedWallpaper(
                    id = id,
                    sourceId = sourceId,
                    source = source,
                    thumbnailUrl = "https://example.com/thumb.jpg",
                    fullUrl = "https://example.com/full.jpg",
                    previewUrl = "https://example.com/preview.jpg",
                    originalUrl = "https://example.com/original.jpg",
                    width = 1080,
                    height = 1920,
                    photographerName = "Author",
                    sourceUrl = "https://example.com",
                    category = SyncCategory.NEW
            )

    private fun unsplashPhoto(id: String): UnsplashPhoto =
            UnsplashPhoto(
                    id = id,
                    width = 1080,
                    height = 1920,
                    urls =
                            UnsplashUrls(
                                    raw = "https://images.unsplash.com/raw/$id",
                                    full = "https://images.unsplash.com/full/$id",
                                    regular = "https://images.unsplash.com/regular/$id",
                                    small = "https://images.unsplash.com/small/$id",
                                    thumb = "https://images.unsplash.com/thumb/$id"
                            ),
                    links =
                            UnsplashLinks(
                                    self = "https://api.unsplash.com/photos/$id",
                                    html = "https://unsplash.com/photos/$id",
                                    download = "https://unsplash.com/photos/$id/download",
                                    downloadLocation =
                                            "https://api.unsplash.com/photos/$id/download"
                            ),
                    user =
                            UnsplashUser(
                                    id = "user",
                                    username = "user",
                                    name = "User Name"
                            )
            )

    private fun pexelsPhoto(id: Long): PexelsPhoto =
            PexelsPhoto(
                    id = id,
                    width = 1080,
                    height = 1920,
                    url = "https://www.pexels.com/photo/$id/",
                    photographer = "Photographer",
                    photographerUrl = "https://www.pexels.com/@photographer",
                    photographerId = 1L,
                    avgColor = "#000000",
                    src =
                            PexelsPhotoSrc(
                                    original = "https://images.pexels.com/original/$id.jpg",
                                    large2x = "https://images.pexels.com/large2x/$id.jpg",
                                    large = "https://images.pexels.com/large/$id.jpg",
                                    medium = "https://images.pexels.com/medium/$id.jpg",
                                    small = "https://images.pexels.com/small/$id.jpg",
                                    portrait = "https://images.pexels.com/portrait/$id.jpg",
                                    landscape = "https://images.pexels.com/landscape/$id.jpg",
                                    tiny = "https://images.pexels.com/tiny/$id.jpg"
                            )
            )

    private fun <T> errorResponse(code: Int): Response<T> =
            Response.error(code, "{}".toResponseBody("application/json".toMediaType()))
}
