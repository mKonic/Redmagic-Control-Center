package com.elitedarkkaiser.redmagic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val prefs = context.getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)

        if (!fanLedEnabled) return

        val request = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(20, TimeUnit.SECONDS)
            .addTag("fan_led_boot_restore")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "fan_led_boot_restore",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
