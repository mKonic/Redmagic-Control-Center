package com.elitedarkkaiser.redmagic

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.Toast

internal object GameModeActions {

    fun buildProfile(
        fanEnabled: Boolean,
        fanLevel: Int,
        pumpEnabled: Boolean,
        pumpProfile: String,
        fanLedEnabled: Boolean,
        fanLedEffect: String,
        fanLedColor: Int,
        logoLedEnabled: Boolean,
        logoLedEffect: String,
        logoLedColor: Int,
        shoulderLedEnabled: Boolean,
        shoulderLedEffect: String,
        shoulderLedColor: Int
    ): GameModeProfile {
        return GameModeProfile(
            fanEnabled = fanEnabled,
            fanLevel = fanLevel,
            pumpEnabled = pumpEnabled,
            pumpProfile = pumpProfile,
            fanLedEnabled = fanLedEnabled,
            fanLedEffect = fanLedEffect,
            fanLedColor = fanLedColor,
            logoLedEnabled = logoLedEnabled,
            logoLedEffect = logoLedEffect,
            logoLedColor = logoLedColor,
            shoulderLedEnabled = shoulderLedEnabled,
            shoulderLedEffect = shoulderLedEffect,
            shoulderLedColor = shoulderLedColor
        )
    }

    fun saveProfile(
        context: Context,
        profile: GameModeProfile,
        persistProfile: (GameModeProfile) -> Unit,
        onSaved: () -> Unit = {}
    ) {
        persistProfile(profile)
        Toast.makeText(context, "Game profile saved", Toast.LENGTH_SHORT).show()
        onSaved()
    }

    fun refreshPumpButtons(
        selectedProfile: String,
        slowBtn: Button,
        mediumBtn: Button,
        quickBtn: Button,
        roundedFill: (Int, Int) -> Drawable,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        slowBtn.background = roundedFill(
            if (selectedProfile == "slow") selectedColor else unselectedColor,
            999
        )
        mediumBtn.background = roundedFill(
            if (selectedProfile == "medium") selectedColor else unselectedColor,
            999
        )
        quickBtn.background = roundedFill(
            if (selectedProfile == "quick") selectedColor else unselectedColor,
            999
        )
    }

    fun updatePumpProfile(
        value: String,
        onProfileChanged: (String) -> Unit,
        refreshButtons: () -> Unit
    ) {
        onProfileChanged(value)
        refreshButtons()
    }

    fun refreshLedEffectButtons(
        selectedEffect: String,
        steadyBtn: Button,
        breatheBtn: Button,
        flashingBtn: Button,
        roundedFill: (Int, Int) -> Drawable,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        steadyBtn.background = roundedFill(
            if (selectedEffect == "steady") selectedColor else unselectedColor,
            999
        )
        breatheBtn.background = roundedFill(
            if (selectedEffect == "breathe") selectedColor else unselectedColor,
            999
        )
        flashingBtn.background = roundedFill(
            if (selectedEffect == "flashing") selectedColor else unselectedColor,
            999
        )
    }

    fun updateLedEffect(
        value: String,
        onEffectChanged: (String) -> Unit,
        refreshButtons: () -> Unit
    ) {
        onEffectChanged(value)
        refreshButtons()
    }

    fun updateLedColor(
        id: Int,
        currentEffect: String,
        onColorChanged: (Int) -> Unit,
        onEffectChanged: (String) -> Unit,
        refreshColorDots: () -> Unit,
        refreshEffectButtons: () -> Unit
    ) {
        onColorChanged(id)
        if (currentEffect.startsWith("preset:")) {
            onEffectChanged("steady")
        }
        refreshColorDots()
        refreshEffectButtons()
    }

    fun applyLedPreset(
        value: String,
        onEffectChanged: (String) -> Unit,
        onColorChanged: (Int) -> Unit,
        refreshEffectButtons: () -> Unit,
        refreshColorDots: () -> Unit,
        refreshPresetBubbles: () -> Unit
    ) {
        onEffectChanged("preset:$value")
        onColorChanged(-1)
        refreshEffectButtons()
        refreshColorDots()
        refreshPresetBubbles()
    }

    fun updateLogoLedEffect(
        value: String,
        onEffectChanged: (String) -> Unit
    ) {
        onEffectChanged(value)
    }

    fun updateLogoLedColor(
        id: Int,
        onColorChanged: (Int) -> Unit,
        refreshColorDots: () -> Unit
    ) {
        onColorChanged(id)
        refreshColorDots()
    }

    fun refreshShoulderEffectButtons(
        selectedEffect: String,
        steadyBtn: Button,
        breatheBtn: Button,
        flashingBtn: Button,
        roundedFill: (Int, Int) -> Drawable,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        steadyBtn.background = roundedFill(
            if (selectedEffect == "steady") selectedColor else unselectedColor,
            999
        )
        breatheBtn.background = roundedFill(
            if (selectedEffect == "breathe") selectedColor else unselectedColor,
            999
        )
        flashingBtn.background = roundedFill(
            if (selectedEffect == "flashing") selectedColor else unselectedColor,
            999
        )
    }

    fun updateShoulderLedEffect(
        value: String,
        onEffectChanged: (String) -> Unit,
        refreshButtons: () -> Unit
    ) {
        onEffectChanged(value)
        refreshButtons()
    }

    fun updateShoulderLedColor(
        id: Int,
        onColorChanged: (Int) -> Unit,
        refreshColorDots: () -> Unit
    ) {
        onColorChanged(id)
        refreshColorDots()
    }
    fun applyProfileNow(
        profile: GameModeProfile,
        applyFanLed: (String, Int) -> Unit
    ) {
        if (profile.fanEnabled) {
            HardwareController.setFanLevel(profile.fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (profile.pumpEnabled) {
            HardwareController.setPumpProfile(profile.pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (profile.fanLedEnabled) {
            applyFanLed(profile.fanLedEffect, profile.fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (profile.logoLedEnabled) {
            HardwareController.setLogoLedEffect(profile.logoLedEffect, profile.logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (profile.shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(profile.shoulderLedEffect, profile.shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }
    }

}
