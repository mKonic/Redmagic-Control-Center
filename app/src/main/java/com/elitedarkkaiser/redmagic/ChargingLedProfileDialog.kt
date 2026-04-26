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
        val space: (Int) -> View
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
        val colorButtons = mutableListOf<Button>()

        fun refreshButtons() {
            steadyBtn.background = deps.roundedFill(if (effect == "steady") deps.panelPressed else Color.parseColor("#1E2633"), 999)
            breatheBtn.background = deps.roundedFill(if (effect == "breathe") deps.panelPressed else Color.parseColor("#1E2633"), 999)
            flashingBtn.background = deps.roundedFill(if (effect == "flashing") deps.panelPressed else Color.parseColor("#1E2633"), 999)

            colorButtons.forEachIndexed { index, btn ->
                val value = index + 1
                btn.background = deps.roundedFill(if (color == value) deps.panelPressed else Color.parseColor("#1E2633"), 999)
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

        val colorRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, deps.dp(10), 0, 0)
        }

        for (i in 1..8) {
            val btn = chip(i.toString(), color == i) {
                color = i
                refreshButtons()
            }
            colorButtons.add(btn)
            colorRow.addView(btn, LinearLayout.LayoutParams(0, deps.dp(40), 1f))
            if (i != 8) colorRow.addView(deps.space(deps.dp(4)))
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
