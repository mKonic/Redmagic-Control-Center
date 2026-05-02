package com.elitedarkkaiser.redmagic

internal object CallLightingActions {

    fun showIncomingFanDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Incoming Call Fan LED",
            subtitle = "Fan LED profile used while an incoming call is ringing.",
            enabledKey = CallLightingState.INCOMING_FAN_ENABLED_KEY,
            effectKey = CallLightingState.INCOMING_FAN_EFFECT_KEY,
            colorKey = CallLightingState.INCOMING_FAN_COLOR_KEY,
            defaultEffect = "flashing",
            defaultColor = 5,
            deps = deps
        )
    }

    fun showIncomingLogoDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Incoming Call Logo LED",
            subtitle = "Logo LED profile used while an incoming call is ringing.",
            enabledKey = CallLightingState.INCOMING_LOGO_ENABLED_KEY,
            effectKey = CallLightingState.INCOMING_LOGO_EFFECT_KEY,
            colorKey = CallLightingState.INCOMING_LOGO_COLOR_KEY,
            defaultEffect = "flashing",
            defaultColor = 1,
            deps = deps
        )
    }

    fun showIncomingShoulderDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Incoming Call Shoulder LEDs",
            subtitle = "Shoulder LED profile used while an incoming call is ringing.",
            enabledKey = CallLightingState.INCOMING_SHOULDER_ENABLED_KEY,
            effectKey = CallLightingState.INCOMING_SHOULDER_EFFECT_KEY,
            colorKey = CallLightingState.INCOMING_SHOULDER_COLOR_KEY,
            defaultEffect = "flashing",
            defaultColor = 8,
            deps = deps
        )
    }

    fun showConnectedFanDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Connected Call Fan LED",
            subtitle = "Fan LED profile used while a call is connected.",
            enabledKey = CallLightingState.CONNECTED_FAN_ENABLED_KEY,
            effectKey = CallLightingState.CONNECTED_FAN_EFFECT_KEY,
            colorKey = CallLightingState.CONNECTED_FAN_COLOR_KEY,
            defaultEffect = "steady",
            defaultColor = 5,
            deps = deps
        )
    }

    fun showConnectedLogoDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Connected Call Logo LED",
            subtitle = "Logo LED profile used while a call is connected.",
            enabledKey = CallLightingState.CONNECTED_LOGO_ENABLED_KEY,
            effectKey = CallLightingState.CONNECTED_LOGO_EFFECT_KEY,
            colorKey = CallLightingState.CONNECTED_LOGO_COLOR_KEY,
            defaultEffect = "steady",
            defaultColor = 1,
            deps = deps
        )
    }

    fun showConnectedShoulderDialog(activity: MainActivity, deps: ChargingLedProfileDialog.Deps) {
        showZoneDialog(
            activity = activity,
            title = "Connected Call Shoulder LEDs",
            subtitle = "Shoulder LED profile used while a call is connected.",
            enabledKey = CallLightingState.CONNECTED_SHOULDER_ENABLED_KEY,
            effectKey = CallLightingState.CONNECTED_SHOULDER_EFFECT_KEY,
            colorKey = CallLightingState.CONNECTED_SHOULDER_COLOR_KEY,
            defaultEffect = "steady",
            defaultColor = 8,
            deps = deps
        )
    }

    private fun showZoneDialog(
        activity: MainActivity,
        title: String,
        subtitle: String,
        enabledKey: String,
        effectKey: String,
        colorKey: String,
        defaultEffect: String,
        defaultColor: Int,
        deps: ChargingLedProfileDialog.Deps
    ) {
        val profile = CallLightingState.readLed(
            activity,
            enabledKey,
            effectKey,
            colorKey,
            defaultEnabled = true,
            defaultEffect = defaultEffect,
            defaultColor = defaultColor
        )

        ChargingLedProfileDialog.show(
            activity = activity,
            title = title,
            subtitle = subtitle,
            originalEnabled = profile.enabled,
            originalEffect = profile.effect,
            originalColor = profile.color,
            deps = deps,
            onSave = { enabled, effect, color ->
                CallLightingState.saveLed(
                    activity,
                    enabledKey,
                    effectKey,
                    colorKey,
                    com.elitedarkkaiser.redmagic.state.LedState(enabled, effect, color)
                )

                if (CallLightingState.isEnabled(activity)) {
                    HardwareServiceActions.startCallLighting(activity)
                }
            }
        )
    }
}
