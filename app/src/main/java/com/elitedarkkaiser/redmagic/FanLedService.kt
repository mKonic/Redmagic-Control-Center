package com.elitedarkkaiser.redmagic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class FanLedService : Service() {

    companion object {
        private const val CHANNEL_ID = "fan_led_service_channel"
        private const val NOTIF_ID = 1102
    }

    private val handler = Handler(Looper.getMainLooper())

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    handler.postDelayed({
                        reapplySavedFanLedState()
                    }, 1500)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Fan LED persistence active"))
        registerFanLedReceiver()
        reapplySavedFanLedState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        reapplySavedFanLedState()
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Throwable) {
        }
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerFanLedReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun reapplySavedFanLedState() {
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("fan_led_enabled", false)
        val effect = prefs.getString("fan_led_effect", "steady") ?: "steady"
        val color = prefs.getInt("fan_led_color", 5)

        if (enabled) {
            HardwareController.setFanLedEffect(effect, color)
            updateNotification("Fan LED active • ${effect.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}")
        } else {
            HardwareController.setFanLedEnabled(false)
            stopSelf()
        }
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("RedMagic Control")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fan LED Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps custom fan LED settings active"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
