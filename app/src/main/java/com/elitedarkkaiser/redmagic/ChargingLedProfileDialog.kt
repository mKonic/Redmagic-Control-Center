package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

internal object ChargingLedProfileDialog {
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
        val space: (Int) -> View,
        val colorDotGeneric: (String, Boolean, () -> Unit) -> View,
        val colorDotDrawable: (String, Boolean) -> Drawable
    )

    fun show(
        activity: MainActivity,
        title: String,
        subtitle: String,
        originalEnabled: Boolean,
        originalEffect: String,
        originalColor: Int,
        onSave: (enabled: Boolean, effect: String, color: Int) -> Unit,
        deps: Deps
    ) {
        var enabled = originalEnabled
        var effect = originalEffect
        var color = originalColor

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(deps.dp(22), deps.dp(18), deps.dp(22), deps.dp(12))
            background = deps.roundedBg(deps.panelColor, deps.borderColor, 22)
        }

        val titleView = TextView(activity).apply {
            text = title
            textSize = 20f
            setTextColor(deps.textPrimary)
            setTypeface(deps.typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(activity).apply {
            text = subtitle
            textSize = 13f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(8), 0, deps.dp(10))
        }

        val enableCheck = CheckBox(activity).apply {
            text = "Enable for charging mode"
            isChecked = enabled
            textSize = 14f
            setTextColor(deps.textPrimary)
            buttonTintList = ColorStateList.valueOf(deps.accent)
            setOnCheckedChangeListener { _, checked -> enabled = checked }
        }

        fun chip(label: String, selected: Boolean, onClick: () -> Unit): Button {
            return Button(activity).apply {
                text = label
                textSize = 12f
                setAllCaps(false)
                setTextColor(deps.textPrimary)
                background = deps.roundedFill(
                    if (selected) deps.panelPressed else Color.parseColor("#1E2633"),
                    999
                )
                setPadding(deps.dp(10), deps.dp(8), deps.dp(10), deps.dp(8))
                setOnClickListener { onClick() }
            }
        }

        lateinit var steadyBtn: Button
        lateinit var breatheBtn: Button
        lateinit var flashingBtn: Button
        lateinit var colorRow: LinearLayout
        lateinit var colorRow2: LinearLayout

        fun refreshButtons() {
            steadyBtn.background = deps.roundedFill(if (effect == "steady") deps.panelPressed else Color.parseColor("#1E2633"), 999)
            breatheBtn.background = deps.roundedFill(if (effect == "breathe") deps.panelPressed else Color.parseColor("#1E2633"), 999)
            flashingBtn.background = deps.roundedFill(if (effect == "flashing") deps.panelPressed else Color.parseColor("#1E2633"), 999)

            if (::colorRow.isInitialized) {
                colorRow.getChildAt(0).background = deps.colorDotDrawable("#FF0000", color == 1)
                colorRow.getChildAt(2).background = deps.colorDotDrawable("#FF8C00", color == 3)
                colorRow.getChildAt(4).background = deps.colorDotDrawable("#FFD600", color == 4)
                colorRow.getChildAt(6).background = deps.colorDotDrawable("#00E676", color == 5)

                colorRow2.getChildAt(0).background = deps.colorDotDrawable("#00E5FF", color == 6)
                colorRow2.getChildAt(2).background = deps.colorDotDrawable("#1565FF", color == 7)
                colorRow2.getChildAt(4).background = deps.colorDotDrawable("#A020F0", color == 8)
                colorRow2.getChildAt(6).background = deps.colorDotDrawable("#FF69B4", color == 9)
            }
        }

        val effectRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        steadyBtn = chip("Steady", effect == "steady") {
            effect = "steady"
            refreshButtons()
        }
        breatheBtn = chip("Breathe", effect == "breathe") {
            effect = "breathe"
            refreshButtons()
        }
        flashingBtn = chip("Flash", effect == "flashing") {
            effect = "flashing"
            refreshButtons()
        }

        effectRow.addView(steadyBtn, LinearLayout.LayoutParams(0, deps.dp(42), 1f))
        effectRow.addView(deps.space(deps.dp(6)))
        effectRow.addView(breatheBtn, LinearLayout.LayoutParams(0, deps.dp(42), 1f))
        effectRow.addView(deps.space(deps.dp(6)))
        effectRow.addView(flashingBtn, LinearLayout.LayoutParams(0, deps.dp(42), 1f))

        colorRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, deps.dp(10), 0, 0)
        }

        colorRow2 = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, deps.dp(10), 0, 0)
        }

        val chargingColors = listOf(
            1 to "#FF0000",
            3 to "#FF8C00",
            4 to "#FFD600",
            5 to "#00E676",
            6 to "#00E5FF",
            7 to "#1565FF",
            8 to "#A020F0",
            9 to "#FF69B4"
        )

        fun addColor(row: LinearLayout, colorId: Int, hex: String) {
            row.addView(deps.colorDotGeneric(hex, color == colorId) {
                color = colorId
                refreshButtons()
            })
            row.addView(deps.space(deps.dp(10)))
        }

        chargingColors.take(4).forEach { (colorId, hex) ->
            addColor(colorRow, colorId, hex)
        }

        chargingColors.drop(4).forEach { (colorId, hex) ->
            addColor(colorRow2, colorId, hex)
        }

        val buttonRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, deps.dp(18), 0, 0)
        }

        val cancelBtn = Button(activity).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(deps.textPrimary)
            background = deps.roundedFill(Color.parseColor("#1E2633"), 14)
        }

        val saveBtn = Button(activity).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(deps.textPrimary)
            background = deps.roundedFill(deps.panelPressed, 14)
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(deps.space(deps.dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(TextView(activity).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(14), 0, deps.dp(6))
        })
        container.addView(effectRow)
        container.addView(TextView(activity).apply {
            text = "Color"
            textSize = 12f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(14), 0, 0)
        })
        container.addView(colorRow)
        container.addView(colorRow2)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(activity)
            .setView(container)
            .setCancelable(true)
            .create()

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            onSave(enabled, effect, color)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.65f)
        }

        refreshButtons()
    }
}
