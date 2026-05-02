package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.elitedarkkaiser.redmagic.state.LedState

internal object CallLightingProfileUi {

    data class Deps(
        val textPrimary: Int,
        val textSecondary: Int,
        val panelColor: Int,
        val borderColor: Int,
        val panelPressed: Int,
        val accent: Int,
        val typeface: Typeface?,
        val dp: (Int) -> Int,
        val roundedBg: (Int, Int, Int) -> Drawable,
        val roundedFill: (Int, Int) -> Drawable,
        val filterChip: (String, Boolean, () -> Unit) -> Button,
        val space: (Int) -> View,
        val colorDotDrawable: (String, Boolean) -> Drawable,
        val colorDotGeneric: (String, Boolean, () -> Unit) -> View
    )

    data class ZoneKeys(
        val enabledKey: String,
        val effectKey: String,
        val colorKey: String,
        val label: String,
        val defaultEffect: String,
        val defaultColor: Int
    )

    fun show(
        activity: MainActivity,
        title: String,
        subtitle: String,
        fanKeys: ZoneKeys,
        logoKeys: ZoneKeys,
        shoulderKeys: ZoneKeys,
        deps: Deps
    ) {
        var fan = CallLightingState.readLed(activity, fanKeys.enabledKey, fanKeys.effectKey, fanKeys.colorKey, true, fanKeys.defaultEffect, fanKeys.defaultColor)
        var logo = CallLightingState.readLed(activity, logoKeys.enabledKey, logoKeys.effectKey, logoKeys.colorKey, true, logoKeys.defaultEffect, logoKeys.defaultColor)
        var shoulder = CallLightingState.readLed(activity, shoulderKeys.enabledKey, shoulderKeys.effectKey, shoulderKeys.colorKey, true, shoulderKeys.defaultEffect, shoulderKeys.defaultColor)

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(deps.dp(22), deps.dp(18), deps.dp(22), deps.dp(12))
            background = deps.roundedBg(deps.panelColor, deps.borderColor, 22)
        }

        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            addView(container, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        container.addView(TextView(activity).apply {
            text = title
            textSize = 20f
            setTextColor(deps.textPrimary)
            setTypeface(deps.typeface, Typeface.BOLD)
        })

        container.addView(TextView(activity).apply {
            text = subtitle
            textSize = 13f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(8), 0, deps.dp(12))
        })

        container.addView(zoneEditor(activity, fanKeys.label, fan, deps) { fan = it })
        container.addView(zoneEditor(activity, logoKeys.label, logo, deps) { logo = it })
        container.addView(zoneEditor(activity, shoulderKeys.label, shoulder, deps) { shoulder = it })

        val dialog = AlertDialog.Builder(activity)
            .setView(scroll)
            .setPositiveButton("Save") { _, _ ->
                CallLightingState.saveLed(activity, fanKeys.enabledKey, fanKeys.effectKey, fanKeys.colorKey, fan)
                CallLightingState.saveLed(activity, logoKeys.enabledKey, logoKeys.effectKey, logoKeys.colorKey, logo)
                CallLightingState.saveLed(activity, shoulderKeys.enabledKey, shoulderKeys.effectKey, shoulderKeys.colorKey, shoulder)

                if (CallLightingState.isEnabled(activity)) {
                    HardwareServiceActions.startCallLighting(activity)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun zoneEditor(
        activity: MainActivity,
        label: String,
        initial: LedState,
        deps: Deps,
        onChanged: (LedState) -> Unit
    ): LinearLayout {
        var enabled = initial.enabled
        var effect = initial.effect
        var color = initial.color

        lateinit var steadyBtn: Button
        lateinit var breatheBtn: Button
        lateinit var flashingBtn: Button
        lateinit var row1: LinearLayout
        lateinit var row2: LinearLayout

        fun publish() {
            onChanged(LedState(enabled, effect, color))
        }

        fun refreshEffects() {
            steadyBtn.background = deps.roundedFill(if (effect == "steady") deps.panelPressed else Color.parseColor("#1E2633"), 18)
            breatheBtn.background = deps.roundedFill(if (effect == "breathe") deps.panelPressed else Color.parseColor("#1E2633"), 18)
            flashingBtn.background = deps.roundedFill(if (effect == "flashing") deps.panelPressed else Color.parseColor("#1E2633"), 18)
        }

        fun refreshColors() {
            row1.getChildAt(0).background = deps.colorDotDrawable("#FF0000", color == 1)
            row1.getChildAt(2).background = deps.colorDotDrawable("#FF8C00", color == 3)
            row1.getChildAt(4).background = deps.colorDotDrawable("#FFD600", color == 4)
            row1.getChildAt(6).background = deps.colorDotDrawable("#00E676", color == 5)
            row2.getChildAt(0).background = deps.colorDotDrawable("#00E5FF", color == 6)
            row2.getChildAt(2).background = deps.colorDotDrawable("#1565FF", color == 7)
            row2.getChildAt(4).background = deps.colorDotDrawable("#A020F0", color == 8)
            row2.getChildAt(6).background = deps.colorDotDrawable("#FF69B4", color == 9)
        }

        fun effectButton(text: String, value: String): Button {
            return deps.filterChip(text, effect == value) {
                effect = value
                refreshEffects()
                publish()
            }
        }

        fun colorDot(id: Int, hex: String): View {
            return deps.colorDotGeneric(hex, color == id) {
                color = id
                if (effect.startsWith("preset:")) effect = "steady"
                refreshEffects()
                refreshColors()
                publish()
            }
        }

        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, deps.dp(10), 0, deps.dp(14))
        }

        card.addView(TextView(activity).apply {
            text = label
            textSize = 13f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(4), 0, deps.dp(6))
        })

        card.addView(CheckBox(activity).apply {
            text = "Enable $label"
            isChecked = enabled
            textSize = 14f
            setTextColor(deps.textPrimary)
            buttonTintList = ColorStateList.valueOf(deps.accent)
            setOnCheckedChangeListener { _, checked ->
                enabled = checked
                publish()
            }
        })

        val effectRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        steadyBtn = effectButton("Steady", "steady")
        breatheBtn = effectButton("Breathe", "breathe")
        flashingBtn = effectButton("Flashing", "flashing")
        effectRow.addView(steadyBtn)
        effectRow.addView(deps.space(deps.dp(8)))
        effectRow.addView(breatheBtn)
        effectRow.addView(deps.space(deps.dp(8)))
        effectRow.addView(flashingBtn)

        row1 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, deps.dp(10), 0, 0)
            addView(colorDot(1, "#FF0000"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(3, "#FF8C00"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(4, "#FFD600"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(5, "#00E676"))
        }

        row2 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, deps.dp(10), 0, 0)
            addView(colorDot(6, "#00E5FF"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(7, "#1565FF"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(8, "#A020F0"))
            addView(deps.space(deps.dp(10)))
            addView(colorDot(9, "#FF69B4"))
        }

        card.addView(effectRow)
        card.addView(row1)
        card.addView(row2)

        refreshEffects()
        refreshColors()
        return card
    }
}
