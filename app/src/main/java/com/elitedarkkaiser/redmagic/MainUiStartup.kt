package com.elitedarkkaiser.redmagic

internal object MainUiStartup {

    fun applySavedHardwareState(
        applySavedFanLedStateOnLaunch: () -> Unit,
        applySavedLogoLedStateOnLaunch: () -> Unit,
        applySavedShoulderLedStateOnLaunch: () -> Unit,
        applySavedPumpStateOnLaunch: () -> Unit,
        setRealTimePreviewEnabled: (Boolean) -> Unit,
        isRealTimePreviewEnabledSaved: () -> Boolean,
        setUseFahrenheit: (Boolean) -> Unit,
        isUseFahrenheitSaved: () -> Boolean,
        setAutoPumpEnabled: (Boolean) -> Unit,
        isAutoPumpEnabledSaved: () -> Boolean
    ) {
        applySavedFanLedStateOnLaunch()
        applySavedLogoLedStateOnLaunch()
        applySavedShoulderLedStateOnLaunch()
        applySavedPumpStateOnLaunch()

        setRealTimePreviewEnabled(isRealTimePreviewEnabledSaved())
        setUseFahrenheit(isUseFahrenheitSaved())
        setAutoPumpEnabled(isAutoPumpEnabledSaved())
    }

    fun applyLaunchHardware(
        fanLedEnabled: Boolean,
        fanLedEffect: String,
        fanLedColor: Int,
        logoLedEnabled: Boolean,
        logoLedEffect: String,
        logoLedColor: Int,
        shoulderLedEnabled: Boolean,
        shoulderLedEffect: String,
        shoulderLedColor: Int,
        pumpEnabled: Boolean,
        pumpProfile: String,
        applyFanLedSelection: (String, Int) -> Unit,
        startFanLedService: () -> Unit,
        stopFanLedService: () -> Unit
    ) {
        if (fanLedEnabled) {
            applyFanLedSelection(fanLedEffect, fanLedColor)
            startFanLedService()
        } else {
            HardwareController.setFanLedEnabled(false)
            stopFanLedService()
        }

        if (logoLedEnabled) {
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }
    }
}
