package com.elitedarkkaiser.redmagic

import android.content.Context
import android.content.Intent

object ChargingLedActions {
    fun saveProfileAndApplyIfCharging(
        context: Context,
        enabledKey: String,
        effectKey: String,
        colorKey: String,
        enabled: Boolean,
        effect: String,
        color: Int
    ) {
        ChargingLedState.saveProfile(
            context,
            enabledKey,
            effectKey,
            colorKey,
            enabled,
            effect,
            color
        )

        context.startService(Intent(context, ChargingModeService::class.java))
        if (ChargingLedState.isEnabled(context) && ChargingLedState.isChargingNow(context)) {
            ChargingLedState.setActive(context, true)
            ChargingLedState.applyChargingProfile(context)
        }
    }
}
