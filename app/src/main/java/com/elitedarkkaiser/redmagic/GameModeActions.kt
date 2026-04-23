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
}
