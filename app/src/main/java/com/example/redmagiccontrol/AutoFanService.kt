package com.example.redmagiccontrol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AutoFanService : Service() {

    companion object {
        private const val CHANNEL_ID = "auto_fan_service_channel"
        private const val NOTIF_ID = 1101
    }

    private val handler = Handler(Looper.getMainLooper())

    private val loop = object : Runnable {
        override fun run() {
            val tempF = HardwareController.readTemperatureF()
            val level = HardwareController.applyAutoFanCurve()
            updateNotification(tempF, level)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Starting automatic fan control..."))
        handler.post(loop)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification(tempF: Float?, level: Int?) {
        val tempText = if (tempF != null) "${tempF.toInt()}°F" else "--°F"
        val levelText = if (level != null) "Level $level" else "Unknown"

        val notification = buildNotification("Auto Fan Curve Active • Temp: $tempText • Fan: $levelText")
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, notification)
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Redmagic HW Controls")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto Fan Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps automatic fan control active in the background"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
