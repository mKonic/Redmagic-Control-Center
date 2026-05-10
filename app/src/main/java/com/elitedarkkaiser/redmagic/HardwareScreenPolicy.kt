package com.elitedarkkaiser.redmagic

import android.content.Context
import android.os.PowerManager

object HardwareScreenPolicy {
    fun isScreenInteractive(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isInteractive
        } catch (_: Throwable) {
            true
        }
    }

    fun blockFanPumpAndNormalLedsWhileScreenOff(context: Context, reason: String): Boolean {
        if (isScreenInteractive(context)) return false

        android.util.Log.i(
            "RedmagicScreenPolicy",
            "Blocked fan/pump/normal-led write while screen is off: $reason"
        )

        HardwareController.enableFan(false)
        HardwareController.enablePump(false)

        if (!ChargingLedState.isEnabled(context) || !ChargingLedState.isChargingNow(context)) {
            HardwareController.turnOffAllLeds()
        }

        return true
    }
}
