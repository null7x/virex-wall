package com.virex.wallpapers.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.virex.wallpapers.data.repository.SyncResult
import com.virex.wallpapers.data.repository.WallpaperSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker for background wallpaper sync
 * 
 * Runs periodically (default: once per day) to fetch new wallpapers
 * from Unsplash and Pexels APIs.
 * 
 * Features:
 * - Runs only on WiFi to save mobile data
 * - Runs only when device is not low battery
 * - Automatic retry with exponential backoff
 * - Respects API rate limits
 */
@HiltWorker
class WallpaperSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: WallpaperSyncRepository
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "WallpaperSyncWorker"
        
        // Work names
        const val PERIODIC_SYNC_WORK_NAME = "wallpaper_periodic_sync"
        const val ONE_TIME_SYNC_WORK_NAME = "wallpaper_one_time_sync"
        
        // Default sync interval (hours)
        const val DEFAULT_SYNC_INTERVAL_HOURS = 24L
        
        // Minimum sync interval
        const val MIN_SYNC_INTERVAL_HOURS = 6L
        
        /**
         * Schedule periodic sync work
         * 
         * @param context Application context
         * @param intervalHours Interval between syncs (minimum 6 hours)
         * @param requireWifi Whether to require WiFi connection
         */
        fun schedulePeriodicSync(
            context: Context,
            intervalHours: Long = DEFAULT_SYNC_INTERVAL_HOURS,
            requireWifi: Boolean = true
        ) {
            val interval = maxOf(intervalHours, MIN_SYNC_INTERVAL_HOURS)
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<WallpaperSyncWorker>(
                interval, TimeUnit.HOURS,
                interval / 4, TimeUnit.HOURS // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.MINUTES
                )
                .addTag("sync")
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                syncRequest
            )
            
            Log.d(TAG, "Periodic sync scheduled: every $interval hours")
        }
        
        /**
         * Cancel periodic sync
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
            Log.d(TAG, "Periodic sync cancelled")
        }
        
        /**
         * Trigger immediate one-time sync
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = OneTimeWorkRequestBuilder<WallpaperSyncWorker>()
                .setConstraints(constraints)
                .addTag("sync")
                .addTag("immediate")
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Immediate sync triggered")
        }
        
        /**
         * Check if periodic sync is scheduled
         */
        suspend fun isPeriodicSyncScheduled(context: Context): Boolean {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(PERIODIC_SYNC_WORK_NAME)
                .get()
            
            return workInfos.any { !it.state.isFinished }
        }
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting wallpaper sync work...")
        
        return try {
            val result = syncRepository.performSync()
            
            when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sync completed successfully: ${result.newCount} new wallpapers")
                    Result.success()
                }
                is SyncResult.Error -> {
                    Log.w(TAG, "Sync failed: ${result.message}")
                    // Retry if it's a transient error
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync work failed with exception", e)
            
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
