package com.virex.wallpapers.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.virex.wallpapers.data.remote.FirebaseDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt Module for Firebase dependencies */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /** Provide Firebase Firestore instance */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()

        // Configure Firestore settings
        val settings =
                FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()

        firestore.firestoreSettings = settings

        return firestore
    }

    /** Provide Firebase Storage instance */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    /** Provide Firebase Data Source */
    @Provides
    @Singleton
    fun provideFirebaseDataSource(
            firestore: FirebaseFirestore,
            storage: FirebaseStorage
    ): FirebaseDataSource {
        return FirebaseDataSource(firestore, storage)
    }
}
