package com.elitedarkkaiser.redmagic

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class GameModeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var gameModeActiveFor: String? = null

    private val pollRunnable = object : Runnable {
        override fun run() {
            try {
                val currentPkg = getForegroundPackageName()
                val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                val tracked = prefs.getStringSet("game_mode_packages", emptySet()) ?: emptySet()

                if (!currentPkg.isNullOrBlank() && tracked.contains(currentPkg)) {
                    if (gameModeActiveFor != currentPkg) {
                        applyGameModeProfile()
                        gameModeActiveFor = currentPkg
                    }
                } else {
                    if (gameModeActiveFor != null) {
                        restoreNormalProfile()
                        gameModeActiveFor = null
                    }
                }
            } catch (_: Throwable) {
            } finally {
                handler.postDelayed(this, 1500L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        if (gameModeActiveFor != null) {
            restoreNormalProfile()
            gameModeActiveFor = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getForegroundPackageName(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 10_000L
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val recent = stats.maxByOrNull { it.lastTimeUsed } ?: return null
        return recent.packageName
    }

    private fun applyGameModeProfile() {
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("game_mode_fan_enabled", true)
        val fanLevel = prefs.getInt("game_mode_fan_level", 3)
        val pumpEnabled = prefs.getBoolean("game_mode_pump_enabled", false)
        val pumpProfile = prefs.getString("game_mode_pump_profile", "quick") ?: "quick"
        val fanLedEnabled = prefs.getBoolean("game_mode_fan_led_enabled", true)
        val fanLedEffect = prefs.getString("game_mode_fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("game_mode_fan_led_color", 5)

        if (fanEnabled) {
            HardwareController.setFanLevel(fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (fanLedEnabled) {
            HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }
    }

    private fun restoreNormalProfile() {
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("fan_enabled", false)
        val fanLevel = prefs.getInt("fan_level", 0)
        val pumpEnabled = prefs.getBoolean("pump_enabled", false)
        val pumpProfile = prefs.getString("pump_profile", "quick") ?: "quick"
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)
        val fanLedEffect = prefs.getString("fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("fan_led_color", 5)

        if (fanEnabled) {
            HardwareController.setFanLevel(fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (fanLedEnabled) {
            HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }
    }
}
