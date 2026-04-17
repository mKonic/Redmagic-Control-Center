package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var rpmText: TextView
    private lateinit var fanSeek: SeekBar

    private val bgColor = Color.parseColor("#070B11")
    private val cardColor = Color.parseColor("#101722")
    private val accentColor = Color.parseColor("#00D9FF")
    private val accentAlt = Color.parseColor("#7B4DFF")
    private val textPrimary = Color.parseColor("#EAF2FF")
    private val textSecondary = Color.parseColor("#94A3B8")
    private val dangerColor = Color.parseColor("#FF5252")
    private val borderColor = Color.parseColor("#1E2A3A")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topInset = getStatusBarHeight()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bgColor)
            clipToPadding = false
            setPadding(0, topInset, 0, 0)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(28))
        }

        val title = TextView(this).apply {
            text = "REDMAGIC CONTROL"
            textSize = 22f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.06f
        }

        val subtitle = TextView(this).apply {
            text = "Hardware control dashboard"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }

        val headerCard = card().apply {
            setPadding(dp(18), dp(18), dp(18), dp(18))
            addView(title)
            addView(subtitle)
        }

        status = TextView(this).apply {
            text = "Checking root..."
            textSize = 14f
            setTextColor(textPrimary)
        }

        rpmText = TextView(this).apply {
            text = "RPM: unknown"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(10), 0, 0)
        }

        val statusCard = card().apply {
            addView(sectionLabel("STATUS"))
            addView(status)
            addView(rpmText)
        }

        fanSeek = SeekBar(this).apply {
            max = 5
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        HardwareController.setFanLevel(progress)
                        refreshStatus()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val rootCheckBtn = actionButton("CHECK ROOT", accentColor) {
            val ok = RootShell.hasRoot()
            status.text = if (ok) "Root available" else "Root not available"

            AlertDialog.Builder(this)
                .setTitle("Root Status")
                .setMessage(
                    if (ok) "Root access granted\n\nApp is running as root"
                    else "Root access NOT granted\n\nCheck your root manager"
                )
                .setPositiveButton("OK", null)
                .show()
        }

        val refreshBtn = actionButton("REFRESH STATUS", accentAlt) {
            refreshStatus()
        }

        val systemCard = card().apply {
            addView(sectionLabel("SYSTEM"))
            addView(row(rootCheckBtn, refreshBtn))
        }

        val fanOnBtn = actionButton("FAN ON", accentColor) {
            HardwareController.enableFan(true)
            refreshStatus()
        }

        val fanOffBtn = actionButton("FAN OFF", dangerColor) {
            HardwareController.enableFan(false)
            refreshStatus()
        }

        val rpmBtn = actionButton("READ RPM", accentAlt) {
            val rpm = HardwareController.readFanRpm()
            rpmText.text = "RPM: ${rpm ?: "unavailable"}"
            refreshStatus()
        }

        val fanCard = card().apply {
            addView(sectionLabel("COOLING"))
            addView(subtleLabel("Fan level"))
            addView(fanSeek)
            addView(row(fanOnBtn, fanOffBtn))
            addView(singleRow(rpmBtn))
        }

        val pumpOnBtn = actionButton("PUMP ON", accentColor) {
            HardwareController.enablePump(true)
            refreshStatus()
        }

        val pumpOffBtn = actionButton("PUMP OFF", dangerColor) {
            HardwareController.enablePump(false)
            refreshStatus()
        }

        val pumpCard = card().apply {
            addView(sectionLabel("PUMP"))
            addView(row(pumpOnBtn, pumpOffBtn))
        }

        val ledPurpleBtn = actionButton("PURPLE BREATHING", accentAlt) {
            HardwareController.setAllLeds(mode = 3, color = 8)
        }

        val ledRedBtn = actionButton("LOGO RED STATIC", accentColor) {
            HardwareController.setLed(zone = 1, mode = 2, color = 1)
        }

        val ledOffBtn = actionButton("LEDS OFF", dangerColor) {
            HardwareController.turnOffAllLeds()
        }

        val ledCard = card().apply {
            addView(sectionLabel("LIGHTING"))
            addView(row(ledPurpleBtn, ledRedBtn))
            addView(singleRow(ledOffBtn))
        }

        val trigEnableBtn = actionButton("ENABLE TRIGGERS", accentColor) {
            HardwareController.enableTriggers()
            refreshStatus()
        }

        val sliderAppBtn = actionButton("SLIDER OPENS APP", accentAlt) {
            HardwareController.setSliderLaunchApp(packageName)
            refreshStatus()
        }

        val sliderRawBtn = actionButton("DISABLE SLIDER ACTION", dangerColor) {
            HardwareController.disableSliderSystemHandling()
            refreshStatus()
        }

        val controlsCard = card().apply {
            addView(sectionLabel("TRIGGERS & SLIDER"))
            addView(singleRow(trigEnableBtn))
            addView(singleRow(sliderAppBtn))
            addView(singleRow(sliderRawBtn))
        }

        val vibrateBtn = actionButton("TEST HAPTIC", accentColor) {
            HardwareController.vibrate(durationMs = 100, gain = 220)
        }

        val hapticCard = card().apply {
            addView(sectionLabel("HAPTICS"))
            addView(singleRow(vibrateBtn))
        }

        content.addView(headerCard)
        content.addView(statusCard)
        content.addView(systemCard)
        content.addView(fanCard)
        content.addView(pumpCard)
        content.addView(ledCard)
        content.addView(controlsCard)
        content.addView(hapticCard)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)

        refreshStatus()
    }

    private fun refreshStatus() {
        status.text = buildString {
            appendLine(if (RootShell.hasRoot()) "Root available" else "Root not available")
            appendLine("Fan enabled: ${HardwareController.isFanEnabled()}")
            appendLine("Fan RPM: ${HardwareController.readFanRpm() ?: "n/a"}")
            appendLine("Slider state: ${HardwareController.readSliderState() ?: "n/a"}")
        }
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(20).toFloat()
                setColor(cardColor)
                setStroke(dp(1), borderColor)
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }
    }

    private fun sectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(accentColor)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.14f
            setPadding(0, 0, 0, dp(12))
        }
    }

    private fun subtleLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, 0, 0, dp(8))
        }
    }

    private fun actionButton(text: String, color: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.WHITE)
            setAllCaps(false)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(color)
            }
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener { onClick() }
        }
    }

    private fun row(left: Button, right: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }

            left.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(6)
            }

            right.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(6)
            }

            addView(left)
            addView(right)
        }
    }

    private fun singleRow(button: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }

            button.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            addView(button)
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dp(24)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
