package com.elitedarkkaiser.redmagic

import android.content.Context
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
}
