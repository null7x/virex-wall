package com.virex.wallpapers.sync

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperSyncWorkerTest {

    @Test
    fun triggerImmediateSync_enqueuesSingleUniqueWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        WallpaperSyncWorker.triggerImmediateSync(context)
        WallpaperSyncWorker.triggerImmediateSync(context)

        val infos =
                WorkManager.getInstance(context)
                        .getWorkInfosForUniqueWork(WallpaperSyncWorker.ONE_TIME_SYNC_WORK_NAME)
                        .get()

        assertEquals(1, infos.size)
    }
}
