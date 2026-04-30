package com.elitedarkkaiser.redmagic

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object HardwareServiceActions {
    fun startAutoFan(context: Context) {
        startForegroundCapableService(context, Intent(context, AutoFanService::class.java))
    }

    fun stopAutoFan(context: Context) {
        context.stopService(Intent(context, AutoFanService::class.java))
    }

    fun startFanLed(context: Context) {
        startForegroundCapableService(context, Intent(context, FanLedService::class.java))
    }

    fun stopFanLed(context: Context) {
        context.stopService(Intent(context, FanLedService::class.java))
    }

    fun startAutoPump(context: Context) {
        startForegroundCapableService(context, Intent(context, AutoPumpService::class.java))
    }

    fun stopAutoPump(context: Context) {
        context.stopService(Intent(context, AutoPumpService::class.java))
    }

    fun enqueueFanLedRestore(context: Context, delaySeconds: Long = 2) {
        val request = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag("fan_led_manual_restore")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "fan_led_manual_restore",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun startForegroundCapableService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
