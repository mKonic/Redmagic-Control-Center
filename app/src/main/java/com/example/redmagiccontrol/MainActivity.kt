package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var rootChip: TextView
    private lateinit var fanChip: TextView
    private lateinit var rpmChip: TextView
    private lateinit var tempChip: TextView

    private lateinit var tempText: TextView
    private lateinit var curveStatusText: TextView
    private lateinit var fanSeek: SeekBar
    private lateinit var autoCurveCheck: CheckBox

    private lateinit var quietCardRef: LinearLayout
    private lateinit var balancedCardRef: LinearLayout
    private lateinit var turboCardRef: LinearLayout

    private lateinit var deviceModelValue: TextView
    private lateinit var deviceRomValue: TextView
    private lateinit var deviceCpuValue: TextView
    private lateinit var deviceRamValue: TextView

    private lateinit var homeTab: LinearLayout
    private lateinit var coolingTab: LinearLayout
    private lateinit var controlsTab: LinearLayout
    private lateinit var lightingTab: LinearLayout

    private lateinit var homeNav: LinearLayout
    private lateinit var coolingNav: LinearLayout
    private lateinit var controlsNav: LinearLayout
    private lateinit var lightingNav: LinearLayout

    private var selectedCurve = "balanced"
    private var autoFanCurveEnabled = false

    private val prefsName = "redmagic_hw_controls_prefs"
    private val skipSupportedDialogKey = "skip_supported_dialog"
    private val autoFanEnabledKey = "auto_fan_enabled"

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
    private val highlightBorder = Color.parseColor("#7F8EA3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isSupportedDevice()) {
            showUnsupportedDeviceDialog()
            return
        }

        if (shouldSkipSupportedDialog()) {
            launchMainUi()
        } else {
            showSupportedDeviceDialog()
        }
    }

    private fun prefs() = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private fun shouldSkipSupportedDialog(): Boolean {
        return prefs().getBoolean(skipSupportedDialogKey, false)
    }

    private fun setSkipSupportedDialog(skip: Boolean) {
        prefs().edit().putBoolean(skipSupportedDialogKey, skip).apply()
    }

    private fun isAutoFanEnabledSaved(): Boolean {
        return prefs().getBoolean(autoFanEnabledKey, false)
    }

    private fun setAutoFanEnabledSaved(enabled: Boolean) {
        prefs().edit().putBoolean(autoFanEnabledKey, enabled).apply()
    }

    private fun startAutoFanService() {
        val intent = Intent(this, AutoFanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAutoFanService() {
        stopService(Intent(this, AutoFanService::class.java))
    }

    private fun showSupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
        }

        val info = TextView(this).apply {
            text =
                "Model check passed.\n\n" +
                    "Required model: NX809J\n" +
                    "Detected Build.MODEL: $buildModel\n" +
                    "Detected ro.product.model: $propModel\n\n" +
                    "Tap OK to continue launching Redmagic HW Controls."
            setTextColor(textPrimary)
            textSize = 14f
        }

        val neverShowAgain = CheckBox(this).apply {
            text = "Never show again"
            setTextColor(textPrimary)
            textSize = 14f
            setPadding(0, dp(14), 0, 0)
        }

        container.addView(info)
        container.addView(neverShowAgain)

        AlertDialog.Builder(this)
            .setTitle("Supported Device Detected")
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                setSkipSupportedDialog(neverShowAgain.isChecked)
                launchMainUi()
            }
            .show()
    }

    private fun showUnsupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        AlertDialog.Builder(this)
            .setTitle("Unsupported Device")
            .setMessage(
                "This app only supports model NX809J.\n\n" +
                    "Detected Build.MODEL: $buildModel\n" +
                    "Detected ro.product.model: $propModel"
            )
            .setCancelable(false)
            .setPositiveButton("Close App") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    private fun isSupportedDevice(): Boolean {
        val required = "NX809J"

        val buildModel = Build.MODEL ?: ""
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: ""
        val propVendorModel = RootShell.execForOutput("getprop ro.product.vendor.model")?.trim() ?: ""
        val propMarketName = RootShell.execForOutput("getprop ro.product.marketname")?.trim() ?: ""

        return buildModel.contains(required, ignoreCase = true) ||
            propModel.contains(required, ignoreCase = true) ||
            propVendorModel.contains(required, ignoreCase = true) ||
            propMarketName.contains(required, ignoreCase = true)
    }

    private fun launchMainUi() {
        val topInset = getStatusBarHeight()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val contentFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), topInset + dp(10), dp(16), dp(96))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        homeTab = createHomeTab()
        coolingTab = createCoolingTab()
        controlsTab = createControlsTab()
        lightingTab = createLightingTab()

        contentFrame.addView(homeTab)
        contentFrame.addView(coolingTab)
        contentFrame.addView(controlsTab)
        contentFrame.addView(lightingTab)

        val navWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), 0, dp(18), dp(18))
            addView(bottomNavBar())
        }

        root.addView(contentFrame)
        root.addView(navWrap)

        setContentView(root)

        autoFanCurveEnabled = isAutoFanEnabledSaved()
        autoCurveCheck.isChecked = autoFanCurveEnabled

        if (autoFanCurveEnabled) {
            curveStatusText.text = "Auto fan curve active • Running in background service"
        } else {
            curveStatusText.text = "Selected curve: $selectedCurve • Manual control"
        }

        setActiveMode(balancedCardRef)
        updateManualCurveUiState()
        switchTab("home")
        refreshStatus()
    }

    private fun createHomeTab(): LinearLayout {
        val container = scrollTabContainer()

        val welcomeCard = sectionPanel().apply {
            addView(titleText("REDMAGIC HW CONTROLS"))
            addView(subtitleText("Cooling, lighting, triggers and hardware controls for Redmagic 11 Pro"))
        }

        val summaryCard = sectionPanel().apply {
            addView(sectionHeader("⌂", "WELCOME"))
            addView(bodyText("Root-powered hardware control utility for Redmagic 11 Pro."))
            addView(bodyText("Controls fan behavior, pump, lighting, triggers, slider actions, and haptics."))
        }

        val linksCard = sectionPanel().apply {
            addView(sectionHeader("↗", "GITHUB & REFERENCE"))

            val githubBtn = actionButton("OPEN GITHUB") {
                openUrl("https://github.com/austineyoung2000/Red")
            }

            val referenceBtn = actionButton("OPEN REFERENCE") {
                openUrl("https://www.reddit.com/r/RedMagic/comments/1rtoako/red_magic_11_pro_hardware_control_guide_for/")
            }

            addView(singleRow(githubBtn))
            addView(singleRow(referenceBtn))
        }

        deviceModelValue = infoValue()
        deviceRomValue = infoValue()
        deviceCpuValue = infoValue()
        deviceRamValue = infoValue()

        val infoCard = sectionPanel().apply {
            addView(sectionHeader("ⓘ", "DEVICE INFO"))
            addView(infoRow("Model", deviceModelValue))
            addView(infoRow("ROM", deviceRomValue))
            addView(infoRow("CPU", deviceCpuValue))
            addView(infoRow("RAM", deviceRamValue))
        }

        rootChip = statusChip("ROOT --")
        fanChip = statusChip("FAN --")
        rpmChip = statusChip("RPM --")
        tempChip = statusChip("TEMP --")

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            addView(rootChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(fanChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(rpmChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(tempChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }

        val statusScroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(statusRow)
        }

        val statusCard = sectionPanel().apply {
            addView(sectionHeader("◎", "LIVE STATUS"))
            addView(statusScroller)
        }

        container.addView(welcomeCard)
        container.addView(summaryCard)
        container.addView(linksCard)
        container.addView(infoCard)
        container.addView(statusCard)

        return container
    }

    private fun createCoolingTab(): LinearLayout {
        val container = scrollTabContainer()

        tempText = TextView(this).apply {
            text = "Current temp: --°F"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        fanSeek = SeekBar(this).apply {
            max = 5
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && !autoFanCurveEnabled) {
                        HardwareController.setFanLevel(progress)
                        refreshStatus()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
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

        quietCardRef = modeCard("Quiet", "Nearly silent • fan 0-1") {
            if (autoFanCurveEnabled) return@modeCard
            selectedCurve = "quiet"
            setActiveMode(quietCardRef)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Quiet • Applied immediately"
            refreshStatus()
        }

        balancedCardRef = modeCard("Balanced", "Everyday use • fan 2-3") {
            if (autoFanCurveEnabled) return@modeCard
            selectedCurve = "balanced"
            setActiveMode(balancedCardRef)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Balanced • Applied immediately"
            refreshStatus()
        }

        turboCardRef = modeCard("Turbo", "Max cooling • fan 4-5") {
            if (autoFanCurveEnabled) return@modeCard
            selectedCurve = "turbo"
            setActiveMode(turboCardRef)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Turbo • Applied immediately"
            refreshStatus()
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

        curveStatusText = TextView(this).apply {
            text = "Selected curve: Balanced"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        autoCurveCheck = CheckBox(this).apply {
            text = "Automatic fan control based on temperature"
            setTextColor(textPrimary)
            textSize = 13f
            setPadding(0, dp(6), 0, dp(4))
            setOnCheckedChangeListener { _, checked ->
                autoFanCurveEnabled = checked
                setAutoFanEnabledSaved(checked)
                updateManualCurveUiState()

                if (checked) {
                    startAutoFanService()
                    curveStatusText.text = "Auto fan curve active • Running in background service"
                } else {
                    stopAutoFanService()
                    curveStatusText.text = "Selected curve: $selectedCurve • Manual control"
                }

                refreshStatus()
            }
        }

        val coolingCard = sectionPanel().apply {
            addView(sectionHeader("❄", "COOLING"))
            addView(tempText)
            addView(subtleLabel("Fan level"))
            addView(fanSeek)
            addView(row(fanOnBtn, fanOffBtn))
            addView(singleRow(rpmBtn))
            addView(spacer(dp(16)))
            addView(sectionHeader("▦", "FAN CURVE"))
            addView(autoCurveCheck)
            addView(curveStatusText)
            addView(modeScroller)
            addView(subtleLabel("Auto mode ramps fan by temperature and disables manual curve cards"))
            addView(subtleLabel("Quiet → low noise, stays between fan 0-1"))
            addView(subtleLabel("Balanced → moderate cooling, stays between fan 2-3"))
            addView(subtleLabel("Turbo → max cooling and sound, stays between fan 4-5"))
        }

        container.addView(coolingCard)
        return container
    }

    private fun createControlsTab(): LinearLayout {
        val container = scrollTabContainer()

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

        val systemCard = sectionPanel().apply {
            addView(sectionHeader("⚙", "SYSTEM"))
            addView(row(rootCheckBtn, refreshBtn))
        }

        val pumpOnBtn = actionButton("PUMP ON") {
            HardwareController.enablePump(true)
            refreshStatus()
        }

        val pumpOffBtn = actionButton("PUMP OFF", isDanger = true) {
            HardwareController.enablePump(false)
            refreshStatus()
        }

        val pumpCard = sectionPanel().apply {
            addView(sectionHeader("◉", "PUMP"))
            addView(row(pumpOnBtn, pumpOffBtn))
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

        val controlsCard = sectionPanel().apply {
            addView(sectionHeader("⌘", "TRIGGERS & SLIDER"))
            addView(singleRow(trigEnableBtn))
            addView(singleRow(sliderAppBtn))
            addView(singleRow(sliderRawBtn))
        }

        val vibrateBtn = actionButton("TEST HAPTIC") {
            HardwareController.vibrate(durationMs = 100, gain = 220)
        }

        val hapticsCard = sectionPanel().apply {
            addView(sectionHeader("≈", "HAPTICS"))
            addView(singleRow(vibrateBtn))
        }

        container.addView(systemCard)
        container.addView(pumpCard)
        container.addView(controlsCard)
        container.addView(hapticsCard)

        return container
    }

    private fun createLightingTab(): LinearLayout {
        val container = scrollTabContainer()

        val ledPurpleBtn = actionButton("PURPLE BREATHING") {
            HardwareController.setAllLeds(mode = 3, color = 8)
        }

        val ledRedBtn = actionButton("LOGO RED STATIC") {
            HardwareController.setLed(zone = 1, mode = 2, color = 1)
        }

        val ledOffBtn = actionButton("LEDS OFF", isDanger = true) {
            HardwareController.turnOffAllLeds()
        }

        val lightingCard = sectionPanel().apply {
            addView(sectionHeader("✦", "LIGHTING"))
            addView(row(ledPurpleBtn, ledRedBtn))
            addView(singleRow(ledOffBtn))
        }

        container.addView(lightingCard)
        return container
    }

    private fun switchTab(tab: String) {
        homeTab.visibility = if (tab == "home") View.VISIBLE else View.GONE
        coolingTab.visibility = if (tab == "cooling") View.VISIBLE else View.GONE
        controlsTab.visibility = if (tab == "controls") View.VISIBLE else View.GONE
        lightingTab.visibility = if (tab == "lighting") View.VISIBLE else View.GONE

        setNavSelected(homeNav, tab == "home")
        setNavSelected(coolingNav, tab == "cooling")
        setNavSelected(controlsNav, tab == "controls")
        setNavSelected(lightingNav, tab == "lighting")
    }

    private fun setNavSelected(nav: LinearLayout, selected: Boolean) {
        nav.background = roundedFill(
            if (selected) panelPressed else Color.TRANSPARENT,
            18
        )
        nav.alpha = if (selected) 1f else 0.72f
    }

    private fun bottomNavBar(): LinearLayout {
        homeNav = navItem("⌂", "Home") { switchTab("home") }
        coolingNav = navItem("❄", "Cooling") { switchTab("cooling") }
        controlsNav = navItem("⌘", "Controls") { switchTab("controls") }
        lightingNav = navItem("✦", "Lighting") { switchTab("lighting") }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = roundedTopBar()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            addView(homeNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(coolingNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(controlsNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(lightingNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun navItem(icon: String, label: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 15f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 10f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, 0)
            })
        }
    }

    private fun roundedTopBar(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#11161F"))
            cornerRadius = dp(24).toFloat()
            setStroke(dp(1), borderColor)
        }
    }

    private fun updateManualCurveUiState() {
        val alpha = if (autoFanCurveEnabled) 0.40f else 1f

        quietCardRef.alpha = alpha
        balancedCardRef.alpha = alpha
        turboCardRef.alpha = alpha

        quietCardRef.isEnabled = !autoFanCurveEnabled
        balancedCardRef.isEnabled = !autoFanCurveEnabled
        turboCardRef.isEnabled = !autoFanCurveEnabled

        quietCardRef.isClickable = !autoFanCurveEnabled
        balancedCardRef.isClickable = !autoFanCurveEnabled
        turboCardRef.isClickable = !autoFanCurveEnabled
    }

    private fun refreshStatus() {
        val rooted = RootShell.hasRoot()
        val fanEnabled = HardwareController.isFanEnabled()
        val rpm = HardwareController.readFanRpm()
        val tempF = HardwareController.readTemperatureF()

        deviceModelValue.text = Build.MODEL ?: "Unknown"
        deviceRomValue.text = HardwareController.readShortRomFingerprint()
        deviceCpuValue.text = HardwareController.readCpuModel()
        deviceRamValue.text = HardwareController.readRamInfo()

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

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Throwable) {
        }
    }

    private fun scrollTabContainer(): LinearLayout {
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addView(ScrollView(this@MainActivity).apply {
                setBackgroundColor(bgColor)
                addView(inner)
            })

            tag = inner
        }.also { outer ->
            val scroll = outer.getChildAt(0) as ScrollView
            val child = scroll.getChildAt(0) as LinearLayout
            outer.removeAllViews()
            outer.addView(scroll.apply { removeAllViews(); addView(child) })
        }
    }

    private fun titleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 22f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.04f
        }
    }

    private fun subtitleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun bodyText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun infoRow(label: String, valueView: TextView): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(0, 0, 0, dp(6))

            val labelView = TextView(this@MainActivity).apply {
                text = label
                textSize = 13f
                setTextColor(textSecondary)
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(64), ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            valueView.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )

            addView(labelView)
            addView(valueView)
        }
    }

    private fun infoValue(): TextView {
        return TextView(this).apply {
            text = "--"
            textSize = 13f
            setTextColor(textPrimary)
            setLineSpacing(0f, 1.1f)
            isSingleLine = false
            maxLines = 4
        }
    }

    private fun sectionHeader(icon: String, text: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(12))

            val iconView = TextView(this@MainActivity).apply {
                this.text = icon
                textSize = 12f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
                background = roundedFill(Color.parseColor("#1E2633"), 10)
                setPadding(dp(8), dp(6), dp(8), dp(6))
            }

            val labelView = TextView(this@MainActivity).apply {
                this.text = text
                textSize = 12f
                setTextColor(accent)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.08f
                setPadding(dp(10), 0, 0, 0)
            }

            addView(iconView)
            addView(labelView)
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
            ).apply {
                bottomMargin = dp(12)
            }
        }
    }

    private fun statusChip(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textPrimary)
            textSize = 10f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            isSingleLine = true
            minHeight = dp(40)
            minimumWidth = dp(80)
            includeFontPadding = false
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = roundedFill(chipOn, 999)
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

        val chooseBtn = Button(this).apply {
            text = "CHOOSE"
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
        card.addView(chooseBtn)

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
