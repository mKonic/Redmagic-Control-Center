package com.elitedarkkaiser.redmagic

import android.content.Context

object MasterProfileActions {

    fun captureCurrent(context: Context, name: String): MasterProfile {
        val fanState = savedFanLedStateStorage(context)
        val logoState = savedLogoLedStateStorage(context)
        val shoulderState = savedShoulderLedStateStorage(context)
        val pumpState = savedPumpStateStorage(context)
        val triggerState = readTriggerPrefsSnapshot(context)

        val hardware = HardwareProfile(
            name = name,
            fanEnabled = HardwareController.isFanEnabled(),
            fanLevel = HardwareController.readFanLevel() ?: 0,
            autoFanEnabled = isAutoFanEnabledStorage(context),
            fanCurveMode = selectedCurveStorage(context),
            pumpEnabled = pumpState.enabled,
            pumpProfile = pumpState.profile,
            autoPumpEnabled = pumpState.autoEnabled,
            fanLedEnabled = fanState.enabled,
            fanLedEffect = fanState.effect,
            fanLedColor = fanState.color,
            logoLedEnabled = logoState.enabled,
            logoLedEffect = logoState.effect,
            logoLedColor = logoState.color,
            shoulderLedEnabled = shoulderState.enabled,
            shoulderLedEffect = shoulderState.effect,
            shoulderLedColor = shoulderState.color,
            triggerEnabled = triggerState.triggerEnabled,
            hapticsEnabled = triggerState.hapticsEnabled,
            leftTriggerAction = triggerState.leftTriggerAction,
            rightTriggerAction = triggerState.rightTriggerAction,
            intentUnlockRightTrigger = triggerState.intentUnlockRightTrigger,
            triggersAutoStart = triggerState.triggersAutoStart
        )

        return MasterProfile(
            name = name,
            hardware = hardware,
            gameMode = getSavedGameModeProfileStorage(context),
            gamePackages = getSavedGamePackagesStorage(context),
            chargingEnabled = ChargingLedState.isEnabled(context),
            chargingFanLed = ChargingLedState.readProfile(
                context,
                ChargingLedState.FAN_ENABLED_KEY,
                ChargingLedState.FAN_EFFECT_KEY,
                ChargingLedState.FAN_COLOR_KEY,
                true,
                "steady",
                5
            ).toLedState(),
            chargingLogoLed = ChargingLedState.readProfile(
                context,
                ChargingLedState.LOGO_ENABLED_KEY,
                ChargingLedState.LOGO_EFFECT_KEY,
                ChargingLedState.LOGO_COLOR_KEY,
                true,
                "steady",
                1
            ).toLedState(),
            chargingShoulderLed = ChargingLedState.readProfile(
                context,
                ChargingLedState.SHOULDER_ENABLED_KEY,
                ChargingLedState.SHOULDER_EFFECT_KEY,
                ChargingLedState.SHOULDER_COLOR_KEY,
                true,
                "breathe",
                8
            ).toLedState(),
            pump = pumpState,
            selectedFanCurve = selectedCurveStorage(context),
            autoFanEnabled = isAutoFanEnabledStorage(context),
            realtimePreviewEnabled = isRealTimePreviewEnabledStorage(context),
            triggers = triggerState
        )
    }

    private fun ChargingLedState.Profile.toLedState(): com.elitedarkkaiser.redmagic.state.LedState {
        return com.elitedarkkaiser.redmagic.state.LedState(
            enabled = enabled,
            effect = effect,
            color = color
        )
    }
}
