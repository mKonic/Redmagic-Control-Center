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

        restorePersistentHardware(context)

        val prefs = context.getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)

        if (fanLedEnabled) {
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

        HardwareServiceActions.startChargingMode(context)
    }


    private fun restorePersistentHardware(context: Context) {
        val prefs = context.getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val triggerPrefs = context.getSharedPreferences("triggers", Context.MODE_PRIVATE)

        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)
        val fanLedEffect = prefs.getString("fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("fan_led_color", 5)

        val logoLedEnabled = prefs.getBoolean("logo_led_enabled", true)
        val logoLedEffect = prefs.getString("logo_led_effect", "steady") ?: "steady"
        val logoLedColor = prefs.getInt("logo_led_color", 1)

        val shoulderLedEnabled = prefs.getBoolean("shoulder_led_enabled", true)
        val shoulderLedEffect = prefs.getString("shoulder_led_effect", "breathe") ?: "breathe"
        val shoulderLedColor = prefs.getInt("shoulder_led_color", 8)

        val pumpEnabled = prefs.getBoolean("pump_enabled", false)
        val pumpProfile = prefs.getString("pump_profile", "quick") ?: "quick"
        val autoPumpEnabled = prefs.getBoolean("auto_pump_enabled", false)
        val autoFanEnabled = prefs.getBoolean("auto_fan_curve_enabled", false)

        if (fanLedEnabled) {
            HardwareController.setFanLedEnabled(true)
            if (fanLedEffect.startsWith("preset:")) {
                HardwareController.setFanLedStockPreset(fanLedEffect.removePrefix("preset:"))
            } else {
                HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            }
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (logoLedEnabled) {
            HardwareController.setLogoLedEnabled(true)
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEnabled(true)
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }

        if (pumpEnabled || autoPumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (fanLedEnabled || logoLedEnabled || shoulderLedEnabled) {
            HardwareServiceActions.startFanLed(context)
        }

        if (autoPumpEnabled) {
            HardwareServiceActions.startAutoPump(context)
        }

        if (autoFanEnabled) {
            HardwareServiceActions.startAutoFan(context)
        }

        if (triggerPrefs.getBoolean("triggers_auto_start", false)) {
            HardwareController.enableTriggers()
            HardwareServiceActions.startTriggers(context)
        }

        GameModeActions.startServiceSilentlyIfPermitted(context)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (fanLedEnabled) {
                if (fanLedEffect.startsWith("preset:")) {
                    HardwareController.setFanLedStockPreset(fanLedEffect.removePrefix("preset:"))
                } else {
                    HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                }
            }

            if (logoLedEnabled) {
                HardwareController.setLogoLedEnabled(true)
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }

            if (shoulderLedEnabled) {
                HardwareController.setShoulderLedEnabled(true)
                HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            }

            if (pumpEnabled || autoPumpEnabled) {
                HardwareController.setPumpProfile(pumpProfile)
            }

            android.util.Log.i(
                "RedmagicBoot",
                "boot restore reapplied fan=$fanLedEnabled/$fanLedEffect/$fanLedColor logo=$logoLedEnabled/$logoLedEffect/$logoLedColor shoulder=$shoulderLedEnabled/$shoulderLedEffect/$shoulderLedColor pump=$pumpEnabled/$pumpProfile autoPump=$autoPumpEnabled autoFan=$autoFanEnabled"
            )
        }, 10000L)
    }

}
