package com.elitedarkkaiser.redmagic.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.elitedarkkaiser.redmagic.HardwareController

object CoolingTabUi {
    data class Refs(
        val tempText: TextView,
        val curveStatusText: TextView,
        val fanSeek: SeekBar,
        val autoCurveCheck: CheckBox,
        val quietCardRef: LinearLayout,
        val balancedCardRef: LinearLayout,
        val turboCardRef: LinearLayout,
        val smartPumpStatusView: TextView,
        val smartPumpSpeedView: TextView
    )

    data class Result(
        val view: LinearLayout,
        val refs: Refs
    )

    fun create(deps: CoolingTabDeps): Result {
        val container = deps.scrollTabContainer()

        val tempText = TextView(container.context).apply {
            text = "Current temp: --"
            textSize = 13f
            setTextColor(AppTheme.textSecondary)
            setPadding(0, deps.dp(6), 0, deps.dp(4))
        }

        lateinit var curveStatusText: TextView
        lateinit var autoCurveCheck: CheckBox

        val fanSeek = SeekBar(container.context).apply {
            max = 5
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && !deps.getAutoFanCurveEnabled()) {
                        HardwareController.setFanLevel(progress)
                        deps.refreshStatus()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }

        val fanOnBtn = deps.actionButton("FAN ON", false) {
            HardwareController.enableFan(true)
            deps.refreshStatus()
        }

        val fanOffBtn = deps.actionButton("FAN OFF", true) {
            HardwareController.enableFan(false)
            deps.refreshStatus()
        }

        val rpmBtn = deps.actionButton("READ RPM", false) {
            deps.refreshStatus()
        }

        val quietCardRef = LinearLayout(container.context)
        val balancedCardRef = LinearLayout(container.context)
        val turboCardRef = LinearLayout(container.context)

        val modeRow = LinearLayout(container.context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val quietChip = deps.segmentedChip("Quiet", deps.getSelectedCurve() == "quiet") {
            if (deps.getAutoFanCurveEnabled()) return@segmentedChip
            deps.setSelectedCurve("quiet")
            deps.setSelectedCurveSaved("quiet")
            val level = HardwareController.applyFanCurve("quiet")
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Quiet • Applied immediately"
        }

        val balancedChip = deps.segmentedChip("Balanced", deps.getSelectedCurve() == "balanced") {
            if (deps.getAutoFanCurveEnabled()) return@segmentedChip
            deps.setSelectedCurve("balanced")
            deps.setSelectedCurveSaved("balanced")
            val level = HardwareController.applyFanCurve("balanced")
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Balanced • Applied immediately"
        }

        val turboChip = deps.segmentedChip("Turbo", deps.getSelectedCurve() == "turbo") {
            if (deps.getAutoFanCurveEnabled()) return@segmentedChip
            deps.setSelectedCurve("turbo")
            deps.setSelectedCurveSaved("turbo")
            val level = HardwareController.applyFanCurve("turbo")
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Turbo • Applied immediately"
        }

        modeRow.addView(quietChip)
        modeRow.addView(deps.space(deps.dp(8)))
        modeRow.addView(balancedChip)
        modeRow.addView(deps.space(deps.dp(8)))
        modeRow.addView(turboChip)

        curveStatusText = TextView(container.context).apply {
            text = "Selected curve: ${deps.getSelectedCurve().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            textSize = 13f
            setTextColor(AppTheme.textSecondary)
            setPadding(0, deps.dp(6), 0, deps.dp(4))
        }

        autoCurveCheck = CheckBox(container.context).apply {
            text = "Automatic fan control based on temperature"
            setTextColor(AppTheme.textPrimary)
            textSize = 13f
            setPadding(0, deps.dp(6), 0, deps.dp(4))
            setOnCheckedChangeListener { _, checked ->
                deps.setAutoFanCurveEnabled(checked)
                deps.setAutoFanEnabledSaved(checked)
                deps.updateManualCurveUiState()

                if (checked) {
                    deps.startAutoFanService()
                    curveStatusText.text = "Auto fan curve active • Running in background service"
                } else {
                    deps.stopAutoFanService()
                    curveStatusText.text = "Selected curve: ${deps.getSelectedCurve()} • Manual control"
                }

                deps.refreshStatus()
            }
        }

        lateinit var smartPumpStatusView: TextView
        lateinit var smartPumpSpeedView: TextView

        val coolingCard = deps.sectionPanel().apply {
            addView(deps.sectionHeader("❄", "COOLING"))
            addView(tempText)

            val tempUnitRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, deps.dp(8), 0, deps.dp(4))
            }

            val tempUnitLabel = TextView(context).apply {
                text = "Use Fahrenheit"
                textSize = 13f
                setTextColor(AppTheme.textPrimary)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tempUnitSwitch = android.widget.Switch(context).apply {
                isChecked = deps.getUseFahrenheit()
                setOnCheckedChangeListener { _, checked ->
                    deps.setUseFahrenheit(checked)
                    deps.saveUseFahrenheit(checked)
                    deps.refreshStatus()
                }
            }

            tempUnitRow.addView(tempUnitLabel)
            tempUnitRow.addView(tempUnitSwitch)

            addView(tempUnitRow)
            addView(deps.subtleLabel("Fan level"))
            addView(fanSeek)
            addView(deps.row(fanOnBtn, fanOffBtn))
            addView(deps.singleRow(rpmBtn))
            addView(deps.spacer(deps.dp(16)))
            addView(deps.sectionHeader("◉", "PUMP"))

            val pumpSection = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(deps.dp(14), deps.dp(14), deps.dp(14), deps.dp(14))
                background = deps.roundedBg(Color.parseColor("#161D28"), Color.parseColor("#253041"), 18)
            }

            val pumpTitle = TextView(context).apply {
                text = "Liquid cooling circulation"
                textSize = 15f
                setTextColor(AppTheme.textPrimary)
                typeface = Typeface.create(AppTheme.appTypeface, Typeface.BOLD)
            }

            val pumpSubtitle = TextView(context).apply {
                text = "Enable or disable the micropump"
                textSize = 12f
                setTextColor(AppTheme.textSecondary)
                setPadding(0, deps.dp(4), 0, deps.dp(12))
            }

            val circulationSwitch = android.widget.Switch(context).apply {
                isChecked = deps.getPumpEnabled()
                setOnCheckedChangeListener { _, checked ->
                    deps.setPumpEnabled(checked)
                    deps.savePumpState()
                    if (checked) {
                        HardwareController.setPumpProfile(deps.getPumpProfile())
                    } else {
                        HardwareController.enablePump(false)
                    }
                    deps.refreshStatus()
                    deps.refreshSmartPumpStatusViews()
                }
            }

            val circulationRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(pumpTitle)
                    addView(pumpSubtitle)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(circulationSwitch)
            }

            val flowRateLabel = TextView(context).apply {
                text = "Flow rate"
                textSize = 12f
                setTextColor(AppTheme.textSecondary)
                setPadding(0, deps.dp(4), 0, deps.dp(8))
            }

            val pumpRateRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val slowBtn = deps.segmentedChip("Slow", deps.getPumpProfile() == "slow") {
                deps.applyPumpProfile("slow")
            }

            val mediumBtn = deps.segmentedChip("Medium", deps.getPumpProfile() == "medium") {
                deps.applyPumpProfile("medium")
            }

            val quickBtn = deps.segmentedChip("Quick", deps.getPumpProfile() == "quick") {
                deps.applyPumpProfile("quick")
            }

            val experimentalBtn = deps.segmentedChip("⚠ Exp", deps.getPumpProfile() == "experimental") {
                deps.confirmExperimentalPumpThenApply()
            }

            val chipParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            val gapParams = LinearLayout.LayoutParams(deps.dp(6), ViewGroup.LayoutParams.WRAP_CONTENT)

            pumpRateRow.addView(slowBtn, chipParams)
            pumpRateRow.addView(deps.space(deps.dp(6)), gapParams)
            pumpRateRow.addView(mediumBtn, chipParams)
            pumpRateRow.addView(deps.space(deps.dp(6)), gapParams)
            pumpRateRow.addView(quickBtn, chipParams)
            pumpRateRow.addView(deps.space(deps.dp(6)), gapParams)
            pumpRateRow.addView(experimentalBtn, chipParams)

            val autoPumpTitle = TextView(context).apply {
                text = "Smart Pump Control"
                textSize = 14f
                setTextColor(AppTheme.textPrimary)
                typeface = Typeface.create(AppTheme.appTypeface, Typeface.BOLD)
                setPadding(0, deps.dp(14), 0, deps.dp(4))
            }

            val autoPumpDesc = TextView(context).apply {
                text = "Dynamically adjusts pump speed based on device temperature."
                textSize = 12f
                setTextColor(AppTheme.textSecondary)
                setPadding(0, 0, 0, deps.dp(8))
            }

            val autoPumpStatus = TextView(context).apply {
                textSize = 12f
                setTextColor(AppTheme.textSecondary)
                alpha = 0.95f
            }.also {
                smartPumpStatusView = it
            }

            val autoPumpSpeedInfo = TextView(context).apply {
                textSize = 12f
                setTextColor(AppTheme.textSecondary)
                setPadding(0, deps.dp(2), 0, 0)
                alpha = 0.90f
            }.also {
                smartPumpSpeedView = it
            }

            fun setPumpManualControlsEnabled(enabled: Boolean) {
                val alphaValue = if (enabled) 1f else 0.40f

                slowBtn.isEnabled = enabled
                mediumBtn.isEnabled = enabled
                quickBtn.isEnabled = enabled
                experimentalBtn.isEnabled = enabled

                slowBtn.alpha = alphaValue
                mediumBtn.alpha = alphaValue
                quickBtn.alpha = alphaValue
                experimentalBtn.alpha = alphaValue
            }

            fun refreshAutoPumpUi() {
                if (deps.getAutoPumpEnabled()) {
                    val status = deps.buildAutoPumpStatusText()
                    autoPumpStatus.text = status.first
                    autoPumpSpeedInfo.text = status.second
                    autoPumpStatus.visibility = View.VISIBLE
                    autoPumpSpeedInfo.visibility = View.VISIBLE
                    setPumpManualControlsEnabled(false)
                } else {
                    autoPumpStatus.text = "Pump Mode: MANUAL • ${deps.getPumpProfile().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
                    val manualSpeed = when (deps.getPumpProfile().lowercase()) {
                        "slow" -> 40
                        "medium" -> 60
                        "quick" -> 80
                        "experimental" -> 90
                        else -> 60
                    }
                    autoPumpSpeedInfo.text = "Speed: $manualSpeed • Freq: 4"
                    autoPumpStatus.visibility = View.VISIBLE
                    autoPumpSpeedInfo.visibility = View.VISIBLE
                    setPumpManualControlsEnabled(true)
                }
                deps.refreshSmartPumpStatusViews()
            }

            val autoPumpSwitch = android.widget.Switch(context).apply {
                isChecked = deps.getAutoPumpEnabled()
                setOnCheckedChangeListener { _, checked ->
                    deps.setAutoPumpEnabled(checked)
                    deps.saveAutoPumpState()
                    if (checked) {
                        deps.setPumpEnabled(true)
                        deps.savePumpState()
                        HardwareController.setPumpProfile(deps.getPumpProfile())
                        deps.startAutoPumpService()
                    } else {
                        deps.stopAutoPumpService()
                    }
                    refreshAutoPumpUi()
                    deps.refreshStatus()
                    deps.refreshSmartPumpStatusViews()
                }
            }

            val autoPumpRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(autoPumpTitle)
                    addView(autoPumpDesc)
                    addView(autoPumpStatus)
                    addView(autoPumpSpeedInfo)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(autoPumpSwitch)
            }

            refreshAutoPumpUi()

            pumpSection.addView(circulationRow)
            pumpSection.addView(flowRateLabel)
            pumpSection.addView(pumpRateRow)
            pumpSection.addView(autoPumpRow)

            addView(pumpSection)
            addView(deps.spacer(deps.dp(16)))
            addView(deps.sectionHeader("▦", "FAN CURVE"))
            addView(autoCurveCheck)
            addView(curveStatusText)
            addView(modeRow)
            addView(deps.subtleLabel("Auto mode ramps fan by temperature and disables manual curve cards"))
            addView(deps.subtleLabel("Quiet → low noise, stays between fan 0-1"))
            addView(deps.subtleLabel("Balanced → moderate cooling, stays between fan 2-3"))
            addView(deps.subtleLabel("Turbo → max cooling and sound, stays between fan 4-5"))
        }

        container.addView(coolingCard)

        return Result(
            view = container,
            refs = Refs(
                tempText = tempText,
                curveStatusText = curveStatusText,
                fanSeek = fanSeek,
                autoCurveCheck = autoCurveCheck,
                quietCardRef = quietCardRef,
                balancedCardRef = balancedCardRef,
                turboCardRef = turboCardRef,
                smartPumpStatusView = smartPumpStatusView,
                smartPumpSpeedView = smartPumpSpeedView
            )
        )
    }
}
