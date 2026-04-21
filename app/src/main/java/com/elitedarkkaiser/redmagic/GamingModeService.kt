package com.elitedarkkaiser.redmagic

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder

class GamingModeService : Service() {

    private val handler = Handler()
    private var gamingActive = false

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, 1500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(checkRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()

        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 10000,
            time
        )

        val currentApp = stats.maxByOrNull { it.lastTimeUsed }?.packageName ?: return

        val prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        val games = prefs.getStringSet("gaming_mode_apps", setOf()) ?: setOf()

        if (games.contains(currentApp)) {
            if (!gamingActive) {
                applyGamingMode()
                gamingActive = true
            }
        } else {
            if (gamingActive) {
                restoreNormalMode()
                gamingActive = false
            }
        }
    }

    private fun applyGamingMode() {
        HardwareController.setFanLedStockPreset("0x3002102")
        HardwareController.setLogoLedEffect("steady", 1)
        HardwareController.setShoulderLedEffect("steady", 5)
    }

    private fun restoreNormalMode() {
        HardwareController.turnOffAllLeds()
    }
}
