package com.elitedarkkaiser.redmagic

import android.graphics.drawable.Drawable
import android.widget.Button

internal object PumpActions {

    fun applyPreviewSelection(
        profile: String,
        setPumpProfile: (String) -> Unit,
        setPumpEnabled: (Boolean) -> Unit,
        applyHardwareProfile: (String) -> Unit,
        refreshDialog: () -> Unit
    ) {
        setPumpProfile(profile)
        setPumpEnabled(true)
        applyHardwareProfile(profile)
        refreshDialog()
    }

    fun restoreOriginalState(
        originalEnabled: Boolean,
        originalProfile: String,
        setPumpEnabled: (Boolean) -> Unit,
        setPumpProfile: (String) -> Unit,
        applyHardwareProfile: (String) -> Unit,
        disablePump: () -> Unit
    ) {
        setPumpEnabled(originalEnabled)
        setPumpProfile(originalProfile)

        if (originalEnabled) {
            applyHardwareProfile(originalProfile)
        } else {
            disablePump()
        }
    }

    fun repaintButtons(
        selectedProfile: String,
        slowBtn: Button,
        mediumBtn: Button,
        quickBtn: Button,
        experimentalBtn: Button,
        roundedFill: (Int, Int) -> Drawable,
        selectedColor: Int,
        normalColor: Int,
        experimentalColor: Int
    ) {
        slowBtn.background = roundedFill(
            if (selectedProfile == "slow") selectedColor else normalColor,
            999
        )
        mediumBtn.background = roundedFill(
            if (selectedProfile == "medium") selectedColor else normalColor,
            999
        )
        quickBtn.background = roundedFill(
            if (selectedProfile == "quick") selectedColor else normalColor,
            999
        )
        experimentalBtn.background = roundedFill(
            if (selectedProfile == "experimental") selectedColor else experimentalColor,
            999
        )
    }
}
