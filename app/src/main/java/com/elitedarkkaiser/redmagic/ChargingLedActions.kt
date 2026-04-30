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
    fun showLogoDialog(
        activity: MainActivity,
        deps: ChargingLedProfileDialog.Deps
    ) {
        val profile = ChargingLedState.readProfile(
            activity,
            ChargingLedState.LOGO_ENABLED_KEY,
            ChargingLedState.LOGO_EFFECT_KEY,
            ChargingLedState.LOGO_COLOR_KEY,
            defaultEnabled = true,
            defaultEffect = "steady",
            defaultColor = 1
        )

        ChargingLedProfileDialog.show(
            activity = activity,
            title = "Charging Logo LED",
            subtitle = "Logo LED profile used only while plugged in and charging.",
            originalEnabled = profile.enabled,
            originalEffect = profile.effect,
            originalColor = profile.color,
            onSave = { enabled, effect, color ->
                saveProfileAndApplyIfCharging(
                    activity,
                    ChargingLedState.LOGO_ENABLED_KEY,
                    ChargingLedState.LOGO_EFFECT_KEY,
                    ChargingLedState.LOGO_COLOR_KEY,
                    enabled,
                    effect,
                    color
                )
            },
            deps = deps
        )
    }

    fun showShoulderDialog(
        activity: MainActivity,
        deps: ChargingLedProfileDialog.Deps
    ) {
        val profile = ChargingLedState.readProfile(
            activity,
            ChargingLedState.SHOULDER_ENABLED_KEY,
            ChargingLedState.SHOULDER_EFFECT_KEY,
            ChargingLedState.SHOULDER_COLOR_KEY,
            defaultEnabled = true,
            defaultEffect = "breathe",
            defaultColor = 8
        )

        ChargingLedProfileDialog.show(
            activity = activity,
            title = "Charging Shoulder LEDs",
            subtitle = "Shoulder LED profile used only while plugged in and charging.",
            originalEnabled = profile.enabled,
            originalEffect = profile.effect,
            originalColor = profile.color,
            onSave = { enabled, effect, color ->
                saveProfileAndApplyIfCharging(
                    activity,
                    ChargingLedState.SHOULDER_ENABLED_KEY,
                    ChargingLedState.SHOULDER_EFFECT_KEY,
                    ChargingLedState.SHOULDER_COLOR_KEY,
                    enabled,
                    effect,
                    color
                )
            },
            deps = deps
        )
    }

}
