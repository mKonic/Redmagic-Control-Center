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


    fun applyProfile(context: Context, profile: MasterProfile) {
        saveFanLedStateStorage(context, profile.hardware.let {
            com.elitedarkkaiser.redmagic.state.LedState(
                enabled = it.fanLedEnabled,
                effect = it.fanLedEffect,
                color = it.fanLedColor
            )
        })
        saveLogoLedStateStorage(context, profile.hardware.let {
            com.elitedarkkaiser.redmagic.state.LedState(
                enabled = it.logoLedEnabled,
                effect = it.logoLedEffect,
                color = it.logoLedColor
            )
        })
        saveShoulderLedStateStorage(context, profile.hardware.let {
            com.elitedarkkaiser.redmagic.state.LedState(
                enabled = it.shoulderLedEnabled,
                effect = it.shoulderLedEffect,
                color = it.shoulderLedColor
            )
        })

        savePumpStateStorage(context, profile.pump.enabled, profile.pump.profile)
        saveAutoPumpStateStorage(context, profile.pump.autoEnabled)

        saveSelectedCurveStorage(context, profile.selectedFanCurve)
        saveAutoFanEnabledStorage(context, profile.autoFanEnabled)
        saveRealTimePreviewEnabledStorage(context, profile.realtimePreviewEnabled)

        saveGameModeProfileStorage(context, profile.gameMode)
        setSavedGamePackagesStorage(context, profile.gamePackages)

        ChargingLedState.setEnabled(context, profile.chargingEnabled)
        ChargingLedState.saveProfile(
            context,
            ChargingLedState.FAN_ENABLED_KEY,
            ChargingLedState.FAN_EFFECT_KEY,
            ChargingLedState.FAN_COLOR_KEY,
            profile.chargingFanLed.enabled,
            profile.chargingFanLed.effect,
            profile.chargingFanLed.color
        )
        ChargingLedState.saveProfile(
            context,
            ChargingLedState.LOGO_ENABLED_KEY,
            ChargingLedState.LOGO_EFFECT_KEY,
            ChargingLedState.LOGO_COLOR_KEY,
            profile.chargingLogoLed.enabled,
            profile.chargingLogoLed.effect,
            profile.chargingLogoLed.color
        )
        ChargingLedState.saveProfile(
            context,
            ChargingLedState.SHOULDER_ENABLED_KEY,
            ChargingLedState.SHOULDER_EFFECT_KEY,
            ChargingLedState.SHOULDER_COLOR_KEY,
            profile.chargingShoulderLed.enabled,
            profile.chargingShoulderLed.effect,
            profile.chargingShoulderLed.color
        )

        saveTriggerPrefsStorage(context, profile.hardware)

        applyHardware(context, profile)
    }

    private fun applyHardware(context: Context, profile: MasterProfile) {
        val hardware = profile.hardware

        if (hardware.fanEnabled) {
            HardwareController.setFanLevel(hardware.fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (profile.autoFanEnabled) {
            HardwareServiceActions.startAutoFan(context)
        } else {
            HardwareServiceActions.stopAutoFan(context)
        }

        if (profile.pump.enabled || profile.pump.autoEnabled) {
            HardwareController.setPumpProfile(profile.pump.profile)
        } else {
            HardwareController.enablePump(false)
        }

        if (profile.pump.autoEnabled) {
            HardwareServiceActions.startAutoPump(context)
        } else {
            HardwareServiceActions.stopAutoPump(context)
        }

        if (ChargingLedState.isEnabled(context) && ChargingLedState.isChargingNow(context)) {
            ChargingLedState.setActive(context, true)
            ChargingLedState.applyChargingProfile(context)
        } else {
            ChargingLedState.setActive(context, false)
            HardwareServiceActions.startFanLed(context)
        }

        if (hardware.triggersAutoStart) {
            HardwareController.enableTriggers()
            HardwareServiceActions.startTriggers(context)
        } else {
            HardwareController.disableTriggers()
        }

        GameModeActions.startServiceSilentlyIfPermitted(context)
        HardwareServiceActions.startChargingMode(context)
    }

    private fun ChargingLedState.Profile.toLedState(): com.elitedarkkaiser.redmagic.state.LedState {
        return com.elitedarkkaiser.redmagic.state.LedState(
            enabled = enabled,
            effect = effect,
            color = color
        )
    }
}
