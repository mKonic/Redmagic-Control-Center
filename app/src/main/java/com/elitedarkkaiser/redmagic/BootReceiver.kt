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
        val action = intent?.action ?: return

        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_USER_UNLOCKED) return

        val prefs = context.getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)

        if (!fanLedEnabled) return

        val shortRequest = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag("fan_led_restore_short")
            .build()

        val longRequest = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(40, TimeUnit.SECONDS)
            .addTag("fan_led_restore_long")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "fan_led_restore_short",
            ExistingWorkPolicy.REPLACE,
            shortRequest
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            "fan_led_restore_long",
            ExistingWorkPolicy.REPLACE,
            longRequest
        )
    }
}
