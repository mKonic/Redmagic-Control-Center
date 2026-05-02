package com.elitedarkkaiser.redmagic

import android.app.Activity
import com.elitedarkkaiser.redmagic.state.LedState

object CallLightingActions {

    fun showIncomingProfileDialog(activity: Activity, deps: ChargingLedProfileDialog.Deps) {
        showProfileDialog(
            activity = activity,
            title = "Incoming Call Lighting",
            fanKeys = Triple(
                CallLightingState.INCOMING_FAN_ENABLED_KEY,
                CallLightingState.INCOMING_FAN_EFFECT_KEY,
                CallLightingState.INCOMING_FAN_COLOR_KEY
            ),
            logoKeys = Triple(
                CallLightingState.INCOMING_LOGO_ENABLED_KEY,
                CallLightingState.INCOMING_LOGO_EFFECT_KEY,
                CallLightingState.INCOMING_LOGO_COLOR_KEY
            ),
            shoulderKeys = Triple(
                CallLightingState.INCOMING_SHOULDER_ENABLED_KEY,
                CallLightingState.INCOMING_SHOULDER_EFFECT_KEY,
                CallLightingState.INCOMING_SHOULDER_COLOR_KEY
            ),
            defaultEffect = "flashing",
            deps = deps
        )
    }

    fun showConnectedProfileDialog(activity: Activity, deps: ChargingLedProfileDialog.Deps) {
        showProfileDialog(
            activity = activity,
            title = "Connected Call Lighting",
            fanKeys = Triple(
                CallLightingState.CONNECTED_FAN_ENABLED_KEY,
                CallLightingState.CONNECTED_FAN_EFFECT_KEY,
                CallLightingState.CONNECTED_FAN_COLOR_KEY
            ),
            logoKeys = Triple(
                CallLightingState.CONNECTED_LOGO_ENABLED_KEY,
                CallLightingState.CONNECTED_LOGO_EFFECT_KEY,
                CallLightingState.CONNECTED_LOGO_COLOR_KEY
            ),
            shoulderKeys = Triple(
                CallLightingState.CONNECTED_SHOULDER_ENABLED_KEY,
                CallLightingState.CONNECTED_SHOULDER_EFFECT_KEY,
                CallLightingState.CONNECTED_SHOULDER_COLOR_KEY
            ),
            defaultEffect = "steady",
            deps = deps
        )
    }

    private fun showProfileDialog(
        activity: Activity,
        title: String,
        fanKeys: Triple<String, String, String>,
        logoKeys: Triple<String, String, String>,
        shoulderKeys: Triple<String, String, String>,
        defaultEffect: String,
        deps: ChargingLedProfileDialog.Deps
    ) {
        val fan = CallLightingState.readLed(activity, fanKeys.first, fanKeys.second, fanKeys.third, true, defaultEffect, 5)
        val logo = CallLightingState.readLed(activity, logoKeys.first, logoKeys.second, logoKeys.third, true, defaultEffect, 1)
        val shoulder = CallLightingState.readLed(activity, shoulderKeys.first, shoulderKeys.second, shoulderKeys.third, true, defaultEffect, 8)

        ChargingLedProfileDialog.show(
            activity = activity,
            title = title,
            fan = fan,
            logo = logo,
            shoulder = shoulder,
            deps = deps,
            onSave = { savedFan, savedLogo, savedShoulder ->
                CallLightingState.saveLed(activity, fanKeys.first, fanKeys.second, fanKeys.third, savedFan)
                CallLightingState.saveLed(activity, logoKeys.first, logoKeys.second, logoKeys.third, savedLogo)
                CallLightingState.saveLed(activity, shoulderKeys.first, shoulderKeys.second, shoulderKeys.third, savedShoulder)

                if (CallLightingState.isEnabled(activity)) {
                    HardwareServiceActions.startCallLighting(activity)
                }
            }
        )
    }
}
