package com.virex.wallpapers.di

import com.virex.wallpapers.data.local.PreferencesDataStore
import com.virex.wallpapers.data.repository.BillingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for RuStore Billing dependencies
 * 
 * NOTE: Google Play Billing removed for RuStore compliance.
 * Uses RuStoreBillingManager singleton internally.
 */
@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    /**
     * Provide BillingRepository
     * No BillingClient needed - uses RuStoreBillingManager internally
     */
    @Provides
    @Singleton
    fun provideBillingRepository(
        preferencesDataStore: PreferencesDataStore
    ): BillingRepository {
        return BillingRepository(preferencesDataStore)
    }
}
