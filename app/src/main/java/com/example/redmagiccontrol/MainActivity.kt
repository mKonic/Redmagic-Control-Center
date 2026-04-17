package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var fanSeek: SeekBar
    private lateinit var rootChip: TextView
    private lateinit var fanChip: TextView
    private lateinit var rpmChip: TextView

    private val bgColor = Color.parseColor("#070B11")
    private val panelColor = Color.parseColor("#101722")
    private val panelAlt = Color.parseColor("#0D1520")
    private val borderColor = Color.parseColor("#1B2A3A")

    private val cyan = Color.parseColor("#00D9FF")
    private val purple = Color.parseColor("#7B4DFF")
    private val red = Color.parseColor("#FF5252")
    private val green = Color.parseColor("#00C853")
    private val amber = Color.parseColor("#FFB300")

    private val textPrimary = Color.parseColor("#EAF2FF")
    private val textSecondary = Color.parseColor("#94A3B8")
    private val textMuted = Color.parseColor("#6B7A90")

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
            text = "REDMAGIC HW CONTROLS"
            textSize = 24f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.05f
        }

        val subtitle = TextView(this).apply {
            text = "Cooling, lighting, triggers, and hardware controls"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }

        rootChip = statusChip("ROOT --", textPrimary, textMuted)
        fanChip = statusChip("FAN --", textPrimary, textMuted)
        rpmChip = statusChip("RPM --", textPrimary, textMuted)

        val chipRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, 0)
            addView(rootChip)
            addView(space(dp(8)))
            addView(fanChip)
            addView(space(dp(8)))
            addView(rpmChip)
        }

        val heroCard = heroCard().apply {
            addView(title)
            addView(subtitle)
            addView(chipRow)
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

        val quietCard = modeCard(
            title = "Quiet",
            subtitle = "Low noise / fan level 1",
            accent = green
        ) {
            HardwareController.setFanLevel(1)
            fanSeek.progress = 1
            refreshStatus()
        }

        val balancedCard = modeCard(
            title = "Balanced",
            subtitle = "Daily use / fan level 3",
            accent = cyan
        ) {
            HardwareController.setFanLevel(3)
            fanSeek.progress = 3
            refreshStatus()
        }

        val turboCard = modeCard(
            title = "Turbo",
            subtitle = "Max cooling / fan level 5",
            accent = purple
        ) {
            HardwareController.setFanLevel(5)
            fanSeek.progress = 5
            refreshStatus()
        }

        val modeScrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(quietCard)
            addView(space(dp(12)))
            addView(balancedCard)
            addView(space(dp(12)))
            addView(turboCard)
        }

        val modeScroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(modeScrollContent)
        }

        val rootCheckBtn = actionButton("CHECK ROOT", cyan) {
            val ok = RootShell.hasRoot()

            AlertDialog.Builder(this)
                .setTitle("Root Status")
                .setMessage(
                    if (ok) "Root access granted\n\nApp is running as root"
                    else "Root access NOT granted\n\nCheck your root manager"
                )
                .setPositiveButton("OK", null)
                .show()

            refreshStatus()
        }

        val refreshBtn = actionButton("REFRESH STATUS", purple) {
            refreshStatus()
        }

        val systemPanel = sectionPanel().apply {
            addView(sectionLabel("SYSTEM"))
            addView(row(rootCheckBtn, refreshBtn))
        }

        val fanOnBtn = actionButton("FAN ON", cyan) {
            HardwareController.enableFan(true)
            refreshStatus()
        }

        val fanOffBtn = actionButton("FAN OFF", red) {
            HardwareController.enableFan(false)
            refreshStatus()
        }

        val rpmBtn = actionButton("READ RPM", purple) {
            refreshStatus()
        }

        val coolingPanel = sectionPanel().apply {
            addView(sectionLabel("COOLING"))
            addView(subtleLabel("Fan level"))
            addView(fanSeek)
            addView(row(fanOnBtn, fanOffBtn))
            addView(singleRow(rpmBtn))
            addView(spacer(dp(12)))
            addView(sectionLabel("PERFORMANCE MODES"))
            addView(modeScroller)
        }

        val pumpOnBtn = actionButton("PUMP ON", cyan) {
            HardwareController.enablePump(true)
            refreshStatus()
        }

        val pumpOffBtn = actionButton("PUMP OFF", red) {
            HardwareController.enablePump(false)
            refreshStatus()
        }

        val pumpPanel = sectionPanel().apply {
            addView(sectionLabel("PUMP"))
            addView(row(pumpOnBtn, pumpOffBtn))
        }

        val ledPurpleBtn = actionButton("PURPLE BREATHING", purple) {
            HardwareController.setAllLeds(mode = 3, color = 8)
        }

        val ledRedBtn = actionButton("LOGO RED STATIC", cyan) {
            HardwareController.setLed(zone = 1, mode = 2, color = 1)
        }

        val ledOffBtn = actionButton("LEDS OFF", red) {
            HardwareController.turnOffAllLeds()
        }

        val lightingPanel = sectionPanel().apply {
            addView(sectionLabel("LIGHTING"))
            addView(row(ledPurpleBtn, ledRedBtn))
            addView(singleRow(ledOffBtn))
        }

        val trigEnableBtn = actionButton("ENABLE TRIGGERS", cyan) {
            HardwareController.enableTriggers()
            refreshStatus()
        }

        val sliderAppBtn = actionButton("SLIDER OPENS APP", purple) {
            HardwareController.setSliderLaunchApp(packageName)
            refreshStatus()
        }

        val sliderRawBtn = actionButton("DISABLE SLIDER ACTION", red) {
            HardwareController.disableSliderSystemHandling()
            refreshStatus()
        }

        val controlsPanel = sectionPanel().apply {
            addView(sectionLabel("TRIGGERS & SLIDER"))
            addView(singleRow(trigEnableBtn))
            addView(singleRow(sliderAppBtn))
            addView(singleRow(sliderRawBtn))
        }

        val vibrateBtn = actionButton("TEST HAPTIC", cyan) {
            HardwareController.vibrate(durationMs = 100, gain = 220)
        }

        val hapticPanel = sectionPanel().apply {
            addView(sectionLabel("HAPTICS"))
            addView(singleRow(vibrateBtn))
        }

        content.addView(heroCard)
        content.addView(systemPanel)
        content.addView(coolingPanel)
        content.addView(pumpPanel)
        content.addView(lightingPanel)
        content.addView(controlsPanel)
        content.addView(hapticPanel)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)

        refreshStatus()
    }

    private fun refreshStatus() {
        val rooted = RootShell.hasRoot()
        val fanEnabled = HardwareController.isFanEnabled()
        val rpm = HardwareController.readFanRpm()

        rootChip.text = if (rooted) "ROOT ON" else "ROOT OFF"
        fanChip.text = if (fanEnabled) "FAN ON" else "FAN OFF"
        rpmChip.text = "RPM ${rpm ?: "--"}"

        setChipColor(rootChip, if (rooted) green else red)
        setChipColor(fanChip, if (fanEnabled) cyan else red)
        setChipColor(rpmChip, if ((rpm ?: 0) > 0) purple else amber)
    }

    private fun heroCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.parseColor("#0E1824"), Color.parseColor("#08111B"))
            ).apply {
                cornerRadius = dp(24).toFloat()
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

    private fun sectionPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(20).toFloat()
                setColor(panelColor)
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

    private fun statusChip(text: String, textColor: Int, bg: Int): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(bg)
            }
        }
    }

    private fun setChipColor(view: TextView, color: Int) {
        view.background = GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(color)
        }
        view.setTextColor(Color.WHITE)
    }

    private fun modeCard(title: String, subtitle: String, accent: Int, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(panelAlt, panelColor)
            ).apply {
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), accent)
            }
            layoutParams = LinearLayout.LayoutParams(dp(220), ViewGroup.LayoutParams.WRAP_CONTENT)

            val titleView = TextView(this@MainActivity).apply {
                text = title
                textSize = 20f
                setTextColor(textPrimary)
                setTypeface(typeface, Typeface.BOLD)
            }

            val subtitleView = TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, dp(6), 0, dp(12))
            }

            val applyBtn = Button(this@MainActivity).apply {
                text = "APPLY"
                setTextColor(Color.WHITE)
                setAllCaps(false)
                background = GradientDrawable().apply {
                    cornerRadius = dp(14).toFloat()
                    setColor(accent)
                }
                setOnClickListener { onClick() }
            }

            addView(titleView)
            addView(subtitleView)
            addView(applyBtn)
        }
    }

    private fun sectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(cyan)
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

    private fun space(width: Int): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, 1)
        }
    }

    private fun spacer(height: Int): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
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
