package com.virex.wallpapers.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.virex.wallpapers.data.model.Category
import com.virex.wallpapers.data.model.Wallpaper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Data Source
 *
 * Handles all Firebase Firestore and Storage operations. Provides reactive data streams using
 * Kotlin Flow.
 */
@Singleton
class FirebaseDataSource
@Inject
constructor(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) {

    companion object {
        private const val COLLECTION_WALLPAPERS = "wallpapers"
        private const val COLLECTION_CATEGORIES = "categories"
        private const val FIELD_CREATED_AT = "created_at"
        private const val FIELD_DOWNLOADS = "downloads"
        private const val FIELD_LIKES = "likes"
        private const val FIELD_IS_FEATURED = "is_featured"
        private const val FIELD_IS_TRENDING = "is_trending"
        private const val FIELD_IS_PREMIUM = "is_premium"
        private const val FIELD_IS_VISIBLE = "is_visible"
        private const val FIELD_CATEGORY_ID = "category_id"
        private const val FIELD_SORT_ORDER = "sort_order"
    }

    // ==================== WALLPAPERS ====================

    /** Get all wallpapers as reactive Flow */
    fun getAllWallpapers(): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get wallpapers by category */
    fun getWallpapersByCategory(categoryId: String): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .whereEqualTo(FIELD_CATEGORY_ID, categoryId)
                        .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get featured wallpapers */
    fun getFeaturedWallpapers(limit: Int = 10): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .whereEqualTo(FIELD_IS_FEATURED, true)
                        .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get trending wallpapers */
    fun getTrendingWallpapers(limit: Int = 10): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .whereEqualTo(FIELD_IS_TRENDING, true)
                        .orderBy(FIELD_DOWNLOADS, Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get new wallpapers (latest additions) */
    fun getNewWallpapers(limit: Int = 20): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get free wallpapers only */
    fun getFreeWallpapers(): Flow<List<Wallpaper>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_WALLPAPERS)
                        .whereEqualTo(FIELD_IS_PREMIUM, false)
                        .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val wallpapers =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Wallpaper::class.java)
                                    }
                                            ?: emptyList()

                            trySend(wallpapers)
                        }

        awaitClose { listener.remove() }
    }

    /** Get single wallpaper by ID (one-time fetch) */
    suspend fun getWallpaperById(id: String): Wallpaper? {
        return try {
            firestore
                    .collection(COLLECTION_WALLPAPERS)
                    .document(id)
                    .get()
                    .await()
                    .toObject(Wallpaper::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /** Increment download count */
    suspend fun incrementDownloadCount(wallpaperId: String) {
        try {
            val docRef = firestore.collection(COLLECTION_WALLPAPERS).document(wallpaperId)
            firestore
                    .runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentDownloads = snapshot.getLong(FIELD_DOWNLOADS) ?: 0
                        transaction.update(docRef, FIELD_DOWNLOADS, currentDownloads + 1)
                    }
                    .await()
        } catch (e: Exception) {
            // Silently fail - analytics are not critical
        }
    }

    /** Increment like count */
    suspend fun incrementLikeCount(wallpaperId: String) {
        try {
            val docRef = firestore.collection(COLLECTION_WALLPAPERS).document(wallpaperId)
            firestore
                    .runTransaction { transaction ->
                        val snapshot = transaction.get(docRef)
                        val currentLikes = snapshot.getLong(FIELD_LIKES) ?: 0
                        transaction.update(docRef, FIELD_LIKES, currentLikes + 1)
                    }
                    .await()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // ==================== CATEGORIES ====================

    /** Get all categories */
    fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val listener =
                firestore
                        .collection(COLLECTION_CATEGORIES)
                        .whereEqualTo(FIELD_IS_VISIBLE, true)
                        .orderBy(FIELD_SORT_ORDER, Query.Direction.ASCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val categories =
                                    snapshot?.documents?.mapNotNull { doc ->
                                        doc.toObject(Category::class.java)
                                    }
                                            ?: emptyList()

                            trySend(categories)
                        }

        awaitClose { listener.remove() }
    }

    /** Get category by ID */
    suspend fun getCategoryById(id: String): Category? {
        return try {
            firestore
                    .collection(COLLECTION_CATEGORIES)
                    .document(id)
                    .get()
                    .await()
                    .toObject(Category::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== STORAGE ====================

    /** Get download URL for a wallpaper */
    suspend fun getDownloadUrl(storagePath: String): String? {
        return try {
            storage.reference.child(storagePath).downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}
