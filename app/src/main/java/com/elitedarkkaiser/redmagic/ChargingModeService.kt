package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class ChargingModeService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            evaluateChargingState()
        }
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }

        registerReceiver(receiver, filter)
        evaluateChargingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        evaluateChargingState()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun evaluateChargingState() {
        val enabled = ChargingLedState.isEnabled(this)
        val charging = ChargingLedState.isChargingNow(this)

        if (enabled && charging) {
            ChargingLedState.setActive(this, true)
            ChargingLedState.applyChargingProfile(this)
        } else {
            val wasActive = ChargingLedState.isActive(this)
            ChargingLedState.setActive(this, false)

            if (wasActive) {
                startService(Intent(this, GameModeService::class.java))
                startService(Intent(this, FanLedService::class.java))
            }
        }
    }
}
