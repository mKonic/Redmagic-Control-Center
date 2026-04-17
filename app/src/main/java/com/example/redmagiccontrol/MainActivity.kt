package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
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
    private lateinit var tempChip: TextView
    private lateinit var tempText: TextView
    private lateinit var curveStatusText: TextView

    private lateinit var quietCardRef: LinearLayout
    private lateinit var balancedCardRef: LinearLayout
    private lateinit var turboCardRef: LinearLayout

    private var selectedCurve = "balanced"

    private val bgColor = Color.parseColor("#0A0D12")
    private val panelColor = Color.parseColor("#121720")
    private val panelPressed = Color.parseColor("#1A2230")
    private val borderColor = Color.parseColor("#232C3B")

    private val accent = Color.parseColor("#8FA3BF")
    private val chipOn = Color.parseColor("#202B38")
    private val chipActive = Color.parseColor("#2B3A4F")
    private val danger = Color.parseColor("#5B2C33")
    private val textPrimary = Color.parseColor("#E8EEF7")
    private val textSecondary = Color.parseColor("#9AA8BA")
    private val highlightBorder = Color.parseColor("#C7D2E1")

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
            textSize = 22f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.04f
        }

        val subtitle = TextView(this).apply {
            text = "Cooling, lighting, triggers and hardware controls for Redmagic 11 Pro"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }

        rootChip = statusChip("ROOT --")
        fanChip = statusChip("FAN --")
        rpmChip = statusChip("RPM --")
        tempChip = statusChip("TEMP --")

        val chipRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(14), 0, 0)
            addView(rootChip)
            addView(space(dp(8)))
            addView(fanChip)
            addView(space(dp(8)))
            addView(rpmChip)
            addView(space(dp(8)))
            addView(tempChip)
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

        quietCardRef = modeCard("Quiet", "Fan level 1-5") {
            selectedCurve = "quiet"
            setActiveMode(quietCardRef)
        }

        balancedCardRef = modeCard("Balanced", "Fan level 1-5") {
            selectedCurve = "balanced"
            setActiveMode(balancedCardRef)
        }

        turboCardRef = modeCard("Turbo", "Fan level 2-5") {
            selectedCurve = "turbo"
            setActiveMode(turboCardRef)
        }

        val modeScrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(quietCardRef)
            addView(space(dp(10)))
            addView(balancedCardRef)
            addView(space(dp(10)))
            addView(turboCardRef)
        }

        val modeScroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(modeScrollContent)
        }

        val rootCheckBtn = actionButton("CHECK ROOT") {
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

        val refreshBtn = actionButton("REFRESH STATUS") {
            refreshStatus()
        }

        val systemPanel = sectionPanel().apply {
            addView(sectionLabel("SYSTEM"))
            addView(row(rootCheckBtn, refreshBtn))
        }

        val fanOnBtn = actionButton("FAN ON") {
            HardwareController.enableFan(true)
            refreshStatus()
        }

        val fanOffBtn = actionButton("FAN OFF", isDanger = true) {
            HardwareController.enableFan(false)
            refreshStatus()
        }

        val rpmBtn = actionButton("READ RPM") {
            refreshStatus()
        }

        tempText = TextView(this).apply {
            text = "Current temp: --°F"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        curveStatusText = TextView(this).apply {
            text = "Selected curve: Balanced"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        val applyCurveBtn = actionButton("APPLY CURVE") {
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) {
                fanSeek.progress = level
                curveStatusText.text =
                    "Selected curve: ${selectedCurve.replaceFirstChar { it.uppercase() }} • Applied fan level $level"
            } else {
                curveStatusText.text = "Selected curve: ${selectedCurve.replaceFirstChar { it.uppercase() }} • Temp unavailable"
            }
            refreshStatus()
        }

        val coolingPanel = sectionPanel().apply {
            addView(sectionLabel("COOLING"))
            addView(tempText)
            addView(subtleLabel("Fan level"))
            addView(fanSeek)
            addView(row(fanOnBtn, fanOffBtn))
            addView(singleRow(rpmBtn))
            addView(spacer(dp(12)))
            addView(sectionLabel("SIMPLE FAN CURVE"))
            addView(curveStatusText)
            addView(modeScroller)
            addView(singleRow(applyCurveBtn))
            addView(subtleLabel("Quiet: <90°F=1, <100°F=2, <110°F=3, <118°F=4, else 5"))
            addView(subtleLabel("Balanced: <88°F=1, <97°F=2, <106°F=3, <115°F=4, else 5"))
            addView(subtleLabel("Turbo: <86°F=2, <95°F=3, <104°F=4, else 5"))
        }

        val pumpOnBtn = actionButton("PUMP ON") {
            HardwareController.enablePump(true)
            refreshStatus()
        }

        val pumpOffBtn = actionButton("PUMP OFF", isDanger = true) {
            HardwareController.enablePump(false)
            refreshStatus()
        }

        val pumpPanel = sectionPanel().apply {
            addView(sectionLabel("PUMP"))
            addView(row(pumpOnBtn, pumpOffBtn))
        }

        val ledPurpleBtn = actionButton("PURPLE BREATHING") {
            HardwareController.setAllLeds(mode = 3, color = 8)
        }

        val ledRedBtn = actionButton("LOGO RED STATIC") {
            HardwareController.setLed(zone = 1, mode = 2, color = 1)
        }

        val ledOffBtn = actionButton("LEDS OFF", isDanger = true) {
            HardwareController.turnOffAllLeds()
        }

        val lightingPanel = sectionPanel().apply {
            addView(sectionLabel("LIGHTING"))
            addView(row(ledPurpleBtn, ledRedBtn))
            addView(singleRow(ledOffBtn))
        }

        val trigEnableBtn = actionButton("ENABLE TRIGGERS") {
            HardwareController.enableTriggers()
            refreshStatus()
        }

        val sliderAppBtn = actionButton("SLIDER OPENS APP") {
            HardwareController.setSliderLaunchApp(packageName)
            refreshStatus()
        }

        val sliderRawBtn = actionButton("DISABLE SLIDER ACTION", isDanger = true) {
            HardwareController.disableSliderSystemHandling()
            refreshStatus()
        }

        val controlsPanel = sectionPanel().apply {
            addView(sectionLabel("TRIGGERS & SLIDER"))
            addView(singleRow(trigEnableBtn))
            addView(singleRow(sliderAppBtn))
            addView(singleRow(sliderRawBtn))
        }

        val vibrateBtn = actionButton("TEST HAPTIC") {
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

        setActiveMode(balancedCardRef)
        refreshStatus()
    }

    private fun refreshStatus() {
        val rooted = RootShell.hasRoot()
        val fanEnabled = HardwareController.isFanEnabled()
        val rpm = HardwareController.readFanRpm()
        val tempF = HardwareController.readTemperatureF()

        rootChip.text = if (rooted) "ROOT ON" else "ROOT OFF"
        fanChip.text = if (fanEnabled) "FAN ON" else "FAN OFF"
        rpmChip.text = "RPM ${rpm ?: "--"}"
        tempChip.text = "TEMP ${tempF?.toInt() ?: "--"}°F"

        tempText.text = "Current temp: ${tempF?.toInt() ?: "--"}°F"

        setChipState(rootChip, rooted)
        setChipState(fanChip, fanEnabled)
        setChipState(rpmChip, (rpm ?: 0) > 0)
        setChipState(tempChip, tempF != null)
    }

    private fun heroCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = roundedBg(panelColor, borderColor, 22)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(14) }
        }
    }

    private fun sectionPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = roundedBg(panelColor, borderColor, 20)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(14) }
        }
    }

    private fun statusChip(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textPrimary)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedFill(chipOn, 14)
        }
    }

    private fun setChipState(view: TextView, active: Boolean) {
        view.background = roundedFill(if (active) chipActive else chipOn, 14)
        view.setTextColor(textPrimary)
    }

    private fun modeCard(title: String, subtitle: String, onClick: () -> Unit): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBg(panelColor, borderColor, 18)
            layoutParams = LinearLayout.LayoutParams(dp(170), ViewGroup.LayoutParams.WRAP_CONTENT)
            minimumHeight = dp(108)
            isClickable = true
            isFocusable = true
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, dp(10))
        }

        val applyBtn = Button(this).apply {
            text = "SELECT"
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 12)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener { onClick() }
            applyPressEffect(this)
        }

        card.addView(titleView)
        card.addView(subtitleView)
        card.addView(applyBtn)

        card.setOnClickListener { onClick() }
        applyPressEffect(card)

        return card
    }

    private fun setActiveMode(active: LinearLayout) {
        val normal = roundedBg(panelColor, borderColor, 18)
        val selected = roundedBg(panelPressed, highlightBorder, 18)

        quietCardRef.background = normal
        balancedCardRef.background = normal
        turboCardRef.background = normal

        active.background = selected
        curveStatusText.text = "Selected curve: " + when (active) {
            quietCardRef -> "Quiet"
            balancedCardRef -> "Balanced"
            else -> "Turbo"
        }
    }

    private fun sectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(accent)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.12f
            setPadding(0, 0, 0, dp(12))
        }
    }

    private fun subtleLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun actionButton(text: String, isDanger: Boolean = false, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textPrimary)
            setAllCaps(false)
            background = roundedFill(if (isDanger) danger else panelPressed, 16)
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener { onClick() }
            applyPressEffect(this, if (isDanger) Color.parseColor("#733943") else Color.parseColor("#293447"))
        }
    }

    private fun row(left: Button, right: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }

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
            ).apply { topMargin = dp(8) }

            button.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(button)
        }
    }

    private fun applyPressEffect(view: View, pressedColor: Int = Color.parseColor("#253040")) {
        val normalBg = view.background
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.92f
                    v.background = when (v) {
                        is Button -> roundedFill(pressedColor, 16)
                        else -> roundedBg(pressedColor, highlightBorder, 18)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1f
                    v.background = normalBg
                }
            }
            false
        }
    }

    private fun roundedBg(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }
    }

    private fun roundedFill(fill: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
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
