package com.elitedarkkaiser.redmagic

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
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
import android.widget.Toast

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

    private var fanLedEnabled = true
    private var fanLedEffect = "steady"
    private var fanLedColor = 1

    private var logoLedEnabled = true
    private var logoLedEffect = "steady"
    private var logoLedColor = 1

    private var shoulderLedEnabled = true
    private var shoulderLedEffect = "breathe"
    private var shoulderLedColor = 8

    private var pumpEnabled = false
    private var pumpProfile = "quick"

    private val prefsName = "redmagic_hw_controls_prefs"
    private val skipSupportedDialogKey = "skip_supported_dialog"
    private val autoFanEnabledKey = "auto_fan_enabled"
    private val fanLedEnabledKey = "fan_led_enabled"
    private val fanLedEffectKey = "fan_led_effect"
    private val fanLedColorKey = "fan_led_color"

    private val logoLedEnabledKey = "logo_led_enabled"
    private val logoLedEffectKey = "logo_led_effect"
    private val logoLedColorKey = "logo_led_color"

    private val shoulderLedEnabledKey = "shoulder_led_enabled"
    private val shoulderLedEffectKey = "shoulder_led_effect"
    private val shoulderLedColorKey = "shoulder_led_color"

    private val pumpEnabledKey = "pump_enabled"
    private val pumpProfileKey = "pump_profile"
    private val pumpExperimentalAcceptedKey = "pump_experimental_accepted"

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

        if (!RootShell.hasRoot()) {
            showRootRequiredDialog()
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

    private fun isFanLedEnabledSaved(): Boolean {
        return prefs().getBoolean(fanLedEnabledKey, false)
    }

    private fun savedFanLedEffect(): String {
        return prefs().getString(fanLedEffectKey, "steady") ?: "steady"
    }

    private fun savedFanLedColor(): Int {
        return prefs().getInt(fanLedColorKey, 5)
    }

    private fun saveFanLedState() {
        prefs().edit()
            .putBoolean(fanLedEnabledKey, fanLedEnabled)
            .putString(fanLedEffectKey, fanLedEffect)
            .putInt(fanLedColorKey, fanLedColor)
            .commit()
    }

    private fun applySavedFanLedStateOnLaunch() {
        fanLedEnabled = isFanLedEnabledSaved()
        fanLedEffect = savedFanLedEffect()
        fanLedColor = savedFanLedColor()
    }

    private fun isLogoLedEnabledSaved(): Boolean {
        return prefs().getBoolean(logoLedEnabledKey, true)
    }

    private fun savedLogoLedEffect(): String {
        return prefs().getString(logoLedEffectKey, "steady") ?: "steady"
    }

    private fun savedLogoLedColor(): Int {
        return prefs().getInt(logoLedColorKey, 1)
    }

    private fun saveLogoLedState() {
        prefs().edit()
            .putBoolean(logoLedEnabledKey, logoLedEnabled)
            .putString(logoLedEffectKey, logoLedEffect)
            .putInt(logoLedColorKey, logoLedColor)
            .commit()
    }

    private fun applySavedLogoLedStateOnLaunch() {
        logoLedEnabled = isLogoLedEnabledSaved()
        logoLedEffect = savedLogoLedEffect()
        logoLedColor = savedLogoLedColor()
    }

    private fun isShoulderLedEnabledSaved(): Boolean {
        return prefs().getBoolean(shoulderLedEnabledKey, true)
    }

    private fun savedShoulderLedEffect(): String {
        return prefs().getString(shoulderLedEffectKey, "breathe") ?: "breathe"
    }

    private fun savedShoulderLedColor(): Int {
        return prefs().getInt(shoulderLedColorKey, 8)
    }

    private fun saveShoulderLedState() {
        prefs().edit()
            .putBoolean(shoulderLedEnabledKey, shoulderLedEnabled)
            .putString(shoulderLedEffectKey, shoulderLedEffect)
            .putInt(shoulderLedColorKey, shoulderLedColor)
            .commit()
    }

    private fun applySavedShoulderLedStateOnLaunch() {
        shoulderLedEnabled = isShoulderLedEnabledSaved()
        shoulderLedEffect = savedShoulderLedEffect()
        shoulderLedColor = savedShoulderLedColor()
    }

    private fun isPumpEnabledSaved(): Boolean {
        return prefs().getBoolean(pumpEnabledKey, false)
    }

    private fun savedPumpProfile(): String {
        return prefs().getString(pumpProfileKey, "quick") ?: "quick"
    }

    private fun savePumpState() {
        prefs().edit()
            .putBoolean(pumpEnabledKey, pumpEnabled)
            .putString(pumpProfileKey, pumpProfile)
            .commit()
    }

    private fun applySavedPumpStateOnLaunch() {
        pumpEnabled = isPumpEnabledSaved()
        pumpProfile = savedPumpProfile().lowercase()
        if (pumpProfile != "slow" && pumpProfile != "medium" && pumpProfile != "quick" && pumpProfile != "experimental") {
            pumpProfile = "quick"
        }
    }

    private fun isPumpExperimentalAccepted(): Boolean {
        return prefs().getBoolean(pumpExperimentalAcceptedKey, false)
    }

    private fun setPumpExperimentalAccepted(accepted: Boolean) {
        prefs().edit().putBoolean(pumpExperimentalAcceptedKey, accepted).commit()
    }

    private fun applyPumpProfile(profile: String) {
        pumpProfile = profile
        pumpEnabled = true
        savePumpState()
        HardwareController.setPumpProfile(profile)
        refreshStatus()
        switchTab("cooling")
    }

    private fun confirmExperimentalPumpThenApply() {
        if (isPumpExperimentalAccepted()) {
            applyPumpProfile("experimental")
            return
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Experimental Pump Mode"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = "This mode overclocks the liquid cooling pump beyond the standard profiles. It may provide thermal or performance benefits under heavy load, but it can also increase wear, instability, heat, noise, and possible pump failure or reduced lifespan.\n\nUse only if you understand the risks."
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val helperView = TextView(this).apply {
            text = "Recommended only for gaming or sustained high-performance workloads."
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(10), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val acceptBtn = Button(this).apply {
            text = "I Understand"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(acceptBtn)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(helperView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        acceptBtn.setOnClickListener {
            setPumpExperimentalAccepted(true)
            applyPumpProfile("experimental")
            dialog.dismiss()
        }

        dialog.show()
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

    private fun startFanLedService() {
        val intent = Intent(this, FanLedService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFanLedService() {
        stopService(Intent(this, FanLedService::class.java))
    }

    private fun startAutoProfileService() {
        val intent = Intent(this, AutoProfileService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAutoProfileService() {
        stopService(Intent(this, AutoProfileService::class.java))
    }

    private fun startAutoPumpService() {
        val intent = Intent(this, AutoPumpService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAutoPumpService() {
        stopService(Intent(this, AutoPumpService::class.java))
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun enqueueFanLedRestore(delaySeconds: Long = 2) {
        val request = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag("fan_led_manual_restore")
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "fan_led_manual_restore",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun showSupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Supported Device Detected"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = """
                Model check passed.

                Required model: NX809J
                Detected Build.MODEL: $buildModel
                Detected ro.product.model: $propModel

                Tap OK to continue launching Redmagic HW Controls.
            """.trimIndent()
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val neverShowAgain = CheckBox(this).apply {
            text = "Never show again"
            textSize = 14f
            setTextColor(textPrimary)
            setPadding(0, dp(14), 0, 0)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val okButton = Button(this).apply {
            text = "OK"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(okButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(neverShowAgain)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        okButton.setOnClickListener {
            setSkipSupportedDialog(neverShowAgain.isChecked)
            dialog.dismiss()
            launchMainUi()
        }

        dialog.show()
    }


    private fun showRootRequiredDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Root Required"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = "This app requires root access to launch.\n\nGrant root in Magisk, KernelSU, or APatch, then reopen the app."
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val closeButton = Button(this).apply {
            text = "Close App"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(danger, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(closeButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        closeButton.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
    }

    private fun showUnsupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Unsupported Device"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = """
                This app only supports model NX809J.

                Detected Build.MODEL: $buildModel
                Detected ro.product.model: $propModel
            """.trimIndent()
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val closeButton = Button(this).apply {
            text = "Close App"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(danger, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(closeButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        closeButton.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
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

        applySavedFanLedStateOnLaunch()
        applySavedLogoLedStateOnLaunch()
        applySavedShoulderLedStateOnLaunch()
        applySavedPumpStateOnLaunch()

        if (fanLedEnabled) {
            HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            startFanLedService()
        } else {
            HardwareController.setFanLedEnabled(false)
            stopFanLedService()
        }

        if (logoLedEnabled) {
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

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

            addView(bodyText("RedMagic HW Controls is a root-powered control center for RedMagic 11 Pro that brings key hardware features into one place with a cleaner interface than stock tools."))

            addView(bodyText("It lets you manage cooling behavior, fan profiles, micropump control, fan LED effects, logo lighting, shoulder LED strips, trigger tools, slider actions, and haptics directly from the app."))

            addView(bodyText("The app is built around real device paths and behavior confirmed on hardware so the controls feel practical, focused, and close to an OEM-style utility."))

            val linksRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(14), 0, 0)
            }

            val githubBtn = segmentedChip("GitHub", false) {
                openUrl("https://github.com/austineyoung2000/Red")
            }

            val referenceBtn = segmentedChip("Reference", false) {
                openUrl("https://www.reddit.com/r/RedMagic/comments/1rtoako/red_magic_11_pro_hardware_control_guide_for/")
            }

            linksRow.addView(githubBtn)
            linksRow.addView(space(dp(8)))
            linksRow.addView(referenceBtn)

            addView(linksRow)
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
        val dashboardCard = sectionPanel().apply {
            addView(sectionHeader("◈", "LIVE DASHBOARD"))

            val dashboardText = TextView(this@MainActivity).apply {
                text = DashboardSnapshot.buildSummary(this@MainActivity)
                textSize = 13f
                setTextColor(textPrimary)
                setLineSpacing(0f, 1.15f)
                setPadding(0, 0, 0, dp(12))
            }

            val refreshBtn = actionButton("REFRESH DASHBOARD") {
                dashboardText.text = DashboardSnapshot.buildSummary(this@MainActivity)
            }

            addView(dashboardText)
            addView(singleRow(refreshBtn))
        }

        val automationCard = sectionPanel().apply {
            addView(sectionHeader("⚙", "AUTOMATION"))

            val autoProfileEnabled = prefs().getBoolean("auto_profile_enabled", false)
            val autoPumpEnabled = prefs().getBoolean("auto_pump_enabled", false)

            val autoProfileBtn = actionButton(
                if (autoProfileEnabled) "AUTO PROFILES: ON" else "AUTO PROFILES: OFF",
                isDanger = !autoProfileEnabled
            ) {
                val next = !prefs().getBoolean("auto_profile_enabled", false)
                prefs().edit().putBoolean("auto_profile_enabled", next).commit()
                if (next) startAutoProfileService() else stopAutoProfileService()
                switchTab("home")
            }

            val usageAccessBtn = actionButton("OPEN USAGE ACCESS") {
                openUsageAccessSettings()
            }

            val autoPumpBtn = actionButton(
                if (autoPumpEnabled) "AUTO PUMP: ON" else "AUTO PUMP: OFF",
                isDanger = !autoPumpEnabled
            ) {
                val next = !prefs().getBoolean("auto_pump_enabled", false)
                prefs().edit().putBoolean("auto_pump_enabled", next).commit()
                if (next) startAutoPumpService() else stopAutoPumpService()
                switchTab("home")
            }

            addView(bodyText("Auto Profiles applies saved hardware profiles based on the foreground app. Usage Access must be granted in Android settings."))
            addView(bodyText("Auto Pump uses safe temperature rules and automatically shifts between Slow, Medium, and Quick."))
            addView(singleRow(autoProfileBtn))
            addView(singleRow(usageAccessBtn))
            addView(singleRow(autoPumpBtn))
        }

        
        container.addView(summaryCard)
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

        quietCardRef = LinearLayout(this)
        balancedCardRef = LinearLayout(this)
        turboCardRef = LinearLayout(this)

        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val quietChip = segmentedChip("Quiet", selectedCurve == "quiet") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "quiet"
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Quiet • Applied immediately"
            recreate()
        }

        val balancedChip = segmentedChip("Balanced", selectedCurve == "balanced") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "balanced"
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Balanced • Applied immediately"
            recreate()
        }

        val turboChip = segmentedChip("Turbo", selectedCurve == "turbo") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "turbo"
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Turbo • Applied immediately"
            recreate()
        }

        modeRow.addView(quietChip)
        modeRow.addView(space(dp(8)))
        modeRow.addView(balancedChip)
        modeRow.addView(space(dp(8)))
        modeRow.addView(turboChip)

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
            addView(sectionHeader("◉", "PUMP"))

            val autoPumpSection = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(12), 0, dp(12))
            }

            val autoPumpTitle = TextView(this@MainActivity).apply {
                text = "Auto Pump"
                textSize = 15f
                setTextColor(textPrimary)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val autoPumpDesc = TextView(this@MainActivity).apply {
                text = "Automatically adjusts pump speed based on temperature."
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(10))
            }

            val autoPumpSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = autoPumpEnabled
                setOnCheckedChangeListener { _, checked ->
                    autoPumpEnabled = checked
                    saveAutoPumpState()

                    if (checked) {
                        startAutoPumpService()
                    } else {
                        stopAutoPumpService()
                    }
                }
            }

            val autoPumpRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL

                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(autoPumpTitle)
                    addView(autoPumpDesc)
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

                addView(autoPumpSwitch)
            }

            autoPumpSection.addView(autoPumpRow)
            pumpSection.addView(autoPumpSection)



            // AUTO PUMP (SMART CONTROL)
            val autoPumpTitle = TextView(this@MainActivity).apply {
                text = "Auto Pump"
                textSize = 14f
                setTextColor(textPrimary)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(10), 0, dp(4))
            }

            val autoPumpDesc = TextView(this@MainActivity).apply {
                text = "Automatically adjusts pump speed based on temperature (Safe scaling)"
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val autoPumpToggle = actionButton(
                if (autoPumpEnabled) "AUTO PUMP: ON" else "AUTO PUMP: OFF",
                isDanger = !autoPumpEnabled
            ) {
                autoPumpEnabled = !autoPumpEnabled
                saveAutoPumpState()

                if (autoPumpEnabled) {
                    startAutoPumpService()
                } else {
                    stopAutoPumpService()
                }

                switchTab("cooling")
            }

            pumpSection.addView(autoPumpTitle)
            pumpSection.addView(autoPumpDesc)
            pumpSection.addView(autoPumpToggle)



            val pumpSection = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(14), dp(14), dp(14))
                background = roundedBg(Color.parseColor("#161D28"), Color.parseColor("#253041"), 18)
            }

            val pumpTitle = TextView(this@MainActivity).apply {
                text = "Liquid cooling circulation"
                textSize = 15f
                setTextColor(textPrimary)
                setTypeface(typeface, Typeface.BOLD)
            }

            val pumpSubtitle = TextView(this@MainActivity).apply {
                text = "Enable or disable the micropump"
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(12))
            }

            val circulationSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = pumpEnabled
                setOnCheckedChangeListener { _, checked ->
                    pumpEnabled = checked
                    savePumpState()
                    if (pumpEnabled) {
                        HardwareController.setPumpProfile(pumpProfile)
                    } else {
                        HardwareController.enablePump(false)
                    }
                    refreshStatus()
                    switchTab("cooling")
                }
            }

            val circulationRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(pumpTitle)
                    addView(pumpSubtitle)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(circulationSwitch)
            }

            val flowRateLabel = TextView(this@MainActivity).apply {
                text = "Flow rate"
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(8))
            }

            val pumpRateRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val slowBtn = segmentedChip("Slow", pumpProfile == "slow") {
                applyPumpProfile("slow")
            }

            val mediumBtn = segmentedChip("Medium", pumpProfile == "medium") {
                applyPumpProfile("medium")
            }

            val quickBtn = segmentedChip("Quick", pumpProfile == "quick") {
                applyPumpProfile("quick")
            }

            val experimentalBtn = segmentedChip("⚠ Exp", pumpProfile == "experimental") {
                confirmExperimentalPumpThenApply()
            }

            val chipParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            val gapParams = LinearLayout.LayoutParams(dp(6), ViewGroup.LayoutParams.WRAP_CONTENT)

            pumpRateRow.addView(slowBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(mediumBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(quickBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(experimentalBtn, chipParams)

            pumpSection.addView(circulationRow)
            pumpSection.addView(flowRateLabel)
            pumpSection.addView(pumpRateRow)

            addView(pumpSection)
            addView(spacer(dp(16)))
            addView(sectionHeader("▦", "FAN CURVE"))
            addView(autoCurveCheck)
            addView(curveStatusText)
            addView(modeRow)
            addView(subtleLabel("Auto mode ramps fan by temperature and disables manual curve cards"))
            addView(subtleLabel("Quiet → low noise, stays between fan 0-1"))
            addView(subtleLabel("Balanced → moderate cooling, stays between fan 2-3"))
            addView(subtleLabel("Turbo → max cooling and sound, stays between fan 4-5"))
        }

        container.addView(coolingCard)

        val profiles = ProfileManager.loadProfiles(this)

        val profilesCard = sectionPanel().apply {
            addView(sectionHeader("★", "PROFILES"))

            profiles.forEach { profile ->
                addView(actionButton(profile.name) {
                    HardwareController.enableFan(profile.fanEnabled)
                    if (profile.fanEnabled) {
                        HardwareController.setFanLevel(profile.fanLevel)
                    } else {
                        HardwareController.enableFan(false)
                    }

                    if (profile.pumpEnabled) {
                        HardwareController.setPumpProfile(profile.pumpProfile)
                    } else {
                        HardwareController.enablePump(false)
                    }

                    autoFanCurveEnabled = profile.autoFan
                    setAutoFanEnabledSaved(profile.autoFan)
                    if (profile.autoFan) {
                        startAutoFanService()
                    } else {
                        stopAutoFanService()
                    }

                    Toast.makeText(this@MainActivity, "Applied ${profile.name}", Toast.LENGTH_SHORT).show()
                })
            }

            addView(actionButton("Save Current as Profile") {
                val newProfile = Profile(
                    "Custom",
                    true,
                    fanSeek.progress,
                    pumpEnabled,
                    pumpProfile,
                    autoFanCurveEnabled
                )
                val updated = profiles.toMutableList()
                updated.add(newProfile)
                ProfileManager.saveProfiles(this@MainActivity, updated)
                recreate()
            })
        }

        container.addView(profilesCard)

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
        container.addView(controlsCard)
        container.addView(hapticsCard)

        return container
    }

    private fun createLightingTab(): LinearLayout {
        val container = scrollTabContainer()

        val fanLedCard = sectionPanel().apply {
            addView(sectionHeader("✦", "FAN LED"))

            val fanLedSummary = TextView(this@MainActivity).apply {
                text = "Customize fan LED effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeFanBtn = actionButton("CUSTOMIZE FAN LED") {
                showFanLedDialog()
            }

            val fanLedOffBtn = actionButton("FAN LED OFF", isDanger = true) {
                fanLedEnabled = false
                saveFanLedState()
                HardwareController.setFanLedEnabled(false)
                stopFanLedService()
            }

            addView(fanLedSummary)
            addView(singleRow(customizeFanBtn))
            addView(singleRow(fanLedOffBtn))
        }

        val logoCard = sectionPanel().apply {
            addView(sectionHeader("◈", "LOGO LED"))

            val logoSummary = TextView(this@MainActivity).apply {
                text = "Customize logo LED effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeLogoBtn = actionButton("CUSTOMIZE LOGO LED") {
                showLogoLedDialog()
            }

            val logoOffBtn = actionButton("LOGO LED OFF", isDanger = true) {
                logoLedEnabled = false
                saveLogoLedState()
                HardwareController.setLogoLedEnabled(false)
            }

            addView(logoSummary)
            addView(singleRow(customizeLogoBtn))
            addView(singleRow(logoOffBtn))
        }

        val shoulderCard = sectionPanel().apply {
            addView(sectionHeader("⟫", "SHOULDER LEDS"))

            val shoulderSummary = TextView(this@MainActivity).apply {
                text = "Customize shoulder LED strips effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeShoulderBtn = actionButton("CUSTOMIZE SHOULDER LEDS") {
                showShoulderLedDialog()
            }

            val shoulderOffBtn = actionButton("SHOULDER LEDS OFF", isDanger = true) {
                shoulderLedEnabled = false
                saveShoulderLedState()
                HardwareController.setShoulderLedEnabled(false)
            }

            addView(shoulderSummary)
            addView(singleRow(customizeShoulderBtn))
            addView(singleRow(shoulderOffBtn))
        }

        container.addView(fanLedCard)
        container.addView(logoCard)
        container.addView(shoulderCard)
        return container
    }



    private var dialogRefreshLogoLed: (() -> Unit)? = null


    private var dialogRefreshShoulderLed: (() -> Unit)? = null


    private var dialogRefreshPump: (() -> Unit)? = null

    private fun showPumpProfileDialog() {
        val originalEnabled = pumpEnabled
        val originalProfile = pumpProfile

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Liquid Cooling Flow Rate"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Choose a pump profile with instant preview"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val profileLabel = TextView(this).apply {
            text = "Flow rate"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val profilesRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val slowBtn = filterChip("Slow", pumpProfile == "slow") {
            pumpProfile = "slow"
            pumpEnabled = true
            HardwareController.setPumpProfile("slow")
            dialogRefreshPump?.invoke()
        }

        val mediumBtn = filterChip("Medium", pumpProfile == "medium") {
            pumpProfile = "medium"
            pumpEnabled = true
            HardwareController.setPumpProfile("medium")
            dialogRefreshPump?.invoke()
        }

        val quickBtn = filterChip("Quick", pumpProfile == "quick") {
            pumpProfile = "quick"
            pumpEnabled = true
            HardwareController.setPumpProfile("quick")
            dialogRefreshPump?.invoke()
        }

        val experimentalBtn = filterChip("⚠ Exp", pumpProfile == "experimental") {
            confirmExperimentalPumpThenApply()
        }

        val chipParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        val gapParams = LinearLayout.LayoutParams(dp(6), ViewGroup.LayoutParams.WRAP_CONTENT)

        profilesRow.addView(slowBtn, chipParams)
        profilesRow.addView(space(dp(6)), gapParams)
        profilesRow.addView(mediumBtn, chipParams)
        profilesRow.addView(space(dp(6)), gapParams)
        profilesRow.addView(quickBtn, chipParams)
        profilesRow.addView(space(dp(6)), gapParams)
        profilesRow.addView(experimentalBtn, chipParams)

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(profileLabel)
        container.addView(profilesRow)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            pumpEnabled = originalEnabled
            pumpProfile = originalProfile

            if (pumpEnabled) {
                HardwareController.setPumpProfile(pumpProfile)
            } else {
                HardwareController.enablePump(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            savePumpState()
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            pumpEnabled = originalEnabled
            pumpProfile = originalProfile

            if (pumpEnabled) {
                HardwareController.setPumpProfile(pumpProfile)
            } else {
                HardwareController.enablePump(false)
            }
        }

        fun repaint() {
            slowBtn.background = roundedFill(
                if (pumpProfile == "slow") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            mediumBtn.background = roundedFill(
                if (pumpProfile == "medium") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            quickBtn.background = roundedFill(
                if (pumpProfile == "quick") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            experimentalBtn.background = roundedFill(
                if (pumpProfile == "experimental") panelPressed else Color.parseColor("#2A1D1D"),
                999
            )
        }

        dialogRefreshPump = { repaint() }
        repaint()
        dialog.show()
    }

    private fun showShoulderLedDialog() {
        val originalEnabled = shoulderLedEnabled
        val originalEffect = shoulderLedEffect
        val originalColor = shoulderLedColor

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Shoulder LEDs"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Customize shoulder LED strips with instant preview"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val enableCheck = CheckBox(this).apply {
            text = "Enable shoulder LEDs"
            isChecked = shoulderLedEnabled
            textSize = 14f
            setTextColor(textPrimary)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setPadding(0, dp(14), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                shoulderLedEnabled = checked
                if (checked) {
                    HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
                } else {
                    HardwareController.setShoulderLedEnabled(false)
                }
            }
        }

        val effectLabel = TextView(this).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val effectsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val steadyBtn = filterChip("Steady", shoulderLedEffect == "steady") {
            shoulderLedEffect = "steady"
            if (shoulderLedEnabled) HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            dialogRefreshShoulderLed?.invoke()
        }

        val breatheBtn = filterChip("Breathe", shoulderLedEffect == "breathe") {
            shoulderLedEffect = "breathe"
            if (shoulderLedEnabled) HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            dialogRefreshShoulderLed?.invoke()
        }

        effectsRow.addView(steadyBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(breatheBtn)

        val colorLabel = TextView(this).apply {
            text = "Color"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(10))
        }

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(colorDotGeneric("#00E676", shoulderLedColor == 5) {
                shoulderLedColor = 5
                if (shoulderLedEnabled) HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
                dialogRefreshShoulderLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#1565FF", shoulderLedColor == 7) {
                shoulderLedColor = 7
                if (shoulderLedEnabled) HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
                dialogRefreshShoulderLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#E100FF", shoulderLedColor == 8) {
                shoulderLedColor = 8
                if (shoulderLedEnabled) HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
                dialogRefreshShoulderLed?.invoke()
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(effectLabel)
        container.addView(effectsRow)
        container.addView(colorLabel)
        container.addView(colorRow)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            shoulderLedEnabled = originalEnabled
            shoulderLedEffect = originalEffect
            shoulderLedColor = originalColor

            if (shoulderLedEnabled) {
                HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            } else {
                HardwareController.setShoulderLedEnabled(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            saveShoulderLedState()
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            shoulderLedEnabled = originalEnabled
            shoulderLedEffect = originalEffect
            shoulderLedColor = originalColor

            if (shoulderLedEnabled) {
                HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            } else {
                HardwareController.setShoulderLedEnabled(false)
            }
        }

        fun repaint() {
            steadyBtn.background = roundedFill(
                if (shoulderLedEffect == "steady") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            breatheBtn.background = roundedFill(
                if (shoulderLedEffect == "breathe") panelPressed else Color.parseColor("#1E2633"),
                999
            )
        }

        fun updateColorDots() {
            (colorRow.getChildAt(0) as View).background = colorDotDrawable("#00E676", shoulderLedColor == 5)
            (colorRow.getChildAt(2) as View).background = colorDotDrawable("#1565FF", shoulderLedColor == 7)
            (colorRow.getChildAt(4) as View).background = colorDotDrawable("#E100FF", shoulderLedColor == 8)
        }

        fun refreshUi() {
            repaint()
            updateColorDots()
        }

        dialogRefreshShoulderLed = { refreshUi() }

        refreshUi()
        dialog.show()
    }

    private fun showLogoLedDialog() {
        val originalEnabled = logoLedEnabled
        val originalEffect = logoLedEffect
        val originalColor = logoLedColor

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Logo LED"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Customize logo LED with instant preview"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val enableCheck = CheckBox(this).apply {
            text = "Enable logo light"
            isChecked = logoLedEnabled
            textSize = 14f
            setTextColor(textPrimary)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setPadding(0, dp(14), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                logoLedEnabled = checked
                if (checked) {
                    HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
                } else {
                    HardwareController.setLogoLedEnabled(false)
                }
            }
        }

        val effectLabel = TextView(this).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val effectsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val steadyBtn = filterChip("Steady", logoLedEffect == "steady") {
            logoLedEffect = "steady"
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }
            dialogRefreshLogoLed?.invoke()
        }

        val breatheBtn = filterChip("Breathe", logoLedEffect == "breathe") {
            logoLedEffect = "breathe"
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }
            dialogRefreshLogoLed?.invoke()
        }

        effectsRow.addView(steadyBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(breatheBtn)

        val colorLabel = TextView(this).apply {
            text = "Color"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(10))
        }

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(colorDotGeneric("#FF0000", logoLedColor == 1) {
                logoLedColor = 1
                if (logoLedEnabled) {
                    HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
                }
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#E100FF", logoLedColor == 8) {
                logoLedColor = 8
                if (logoLedEnabled) {
                    HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
                }
                dialogRefreshLogoLed?.invoke()
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(effectLabel)
        container.addView(effectsRow)
        container.addView(colorLabel)
        container.addView(colorRow)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            logoLedEnabled = originalEnabled
            logoLedEffect = originalEffect
            logoLedColor = originalColor

            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            saveLogoLedState()
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            logoLedEnabled = originalEnabled
            logoLedEffect = originalEffect
            logoLedColor = originalColor

            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }
        }

        fun repaint() {
            steadyBtn.background = roundedFill(
                if (logoLedEffect == "steady") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            breatheBtn.background = roundedFill(
                if (logoLedEffect == "breathe") panelPressed else Color.parseColor("#1E2633"),
                999
            )
        }

        fun updateColorDots() {
            (colorRow.getChildAt(0) as View).background = colorDotDrawable("#FF0000", logoLedColor == 1)
            (colorRow.getChildAt(2) as View).background = colorDotDrawable("#E100FF", logoLedColor == 8)
        }

        fun refreshUi() {
            repaint()
            updateColorDots()
        }

        dialogRefreshLogoLed = { refreshUi() }

        refreshUi()
        dialog.show()
    }

    private fun showFanLedDialog() {
        val originalEnabled = fanLedEnabled
        val originalEffect = fanLedEffect
        val originalColor = fanLedColor

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Fan LED"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Confirmed working options for fan LED"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val enableCheck = CheckBox(this).apply {
            text = "Enable fan light"
            isChecked = fanLedEnabled
            textSize = 14f
            setTextColor(textPrimary)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setPadding(0, dp(14), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                fanLedEnabled = checked
                if (checked) {
                    HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                } else {
                    HardwareController.setFanLedEnabled(false)
                }
            }
        }

        val effectLabel = TextView(this).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val effectsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val steadyBtn = filterChip("Steady", fanLedEffect == "steady") {
            fanLedEffect = "steady"
            if (fanLedEnabled) HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            dialogRefreshFanLed?.invoke()
        }

        val breatheBtn = filterChip("Breathe", fanLedEffect == "breathe") {
            fanLedEffect = "breathe"
            if (fanLedEnabled) HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            dialogRefreshFanLed?.invoke()
        }

        effectsRow.addView(steadyBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(breatheBtn)

        val colorLabel = TextView(this).apply {
            text = "Color"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(10))
        }

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(colorDot(5, "#00E676") {
                fanLedColor = 5
                if (fanLedEnabled) HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(7, "#1565FF") {
                fanLedColor = 7
                if (fanLedEnabled) HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                dialogRefreshFanLed?.invoke()
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(effectLabel)
        container.addView(effectsRow)
        container.addView(colorLabel)
        container.addView(colorRow)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            fanLedEnabled = originalEnabled
            fanLedEffect = originalEffect
            fanLedColor = originalColor

            if (fanLedEnabled) {
                HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            } else {
                HardwareController.setFanLedEnabled(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            saveFanLedState()
            if (fanLedEnabled) {
                startFanLedService()
            } else {
                stopFanLedService()
            }
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            fanLedEnabled = originalEnabled
            fanLedEffect = originalEffect
            fanLedColor = originalColor

            if (fanLedEnabled) {
                HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
            } else {
                HardwareController.setFanLedEnabled(false)
            }
        }

        fun repaint() {
            steadyBtn.background = roundedFill(
                if (fanLedEffect == "steady") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            breatheBtn.background = roundedFill(
                if (fanLedEffect == "breathe") panelPressed else Color.parseColor("#1E2633"),
                999
            )
        }

        fun updateColorDots() {
            (colorRow.getChildAt(0) as View).background = colorDotDrawable("#00E676", fanLedColor == 5)
            (colorRow.getChildAt(2) as View).background = colorDotDrawable("#1565FF", fanLedColor == 7)
        }

        repaint()
        updateColorDots()

        fun refreshUi() {
            repaint()
            updateColorDots()
        }

        dialogRefreshFanLed = { refreshUi() }

        dialog.show()
    }

    private var dialogRefreshFanLed: (() -> Unit)? = null

    private fun filterChip(label: String, selected: Boolean, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            setAllCaps(false)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            minWidth = 0
            minimumWidth = 0
            setTextColor(textPrimary)
            background = roundedFill(
                if (selected) panelPressed else Color.parseColor("#1E2633"),
                16
            )
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnClickListener { onClick() }
            applyPressEffect(this)
        }
    }

    private fun colorDot(colorId: Int, hex: String, onClick: () -> Unit): View {
        return View(this).apply {
            val size = dp(42)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = colorDotDrawable(hex, fanLedColor == colorId)
            setOnClickListener { onClick() }
        }
    }

    private fun colorDotDrawable(hex: String, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(hex))
            setStroke(dp(3), if (selected) Color.WHITE else Color.TRANSPARENT)
        }
    }

    private fun colorDotGeneric(hex: String, selected: Boolean, onClick: () -> Unit): View {
        return View(this).apply {
            val size = dp(42)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = colorDotDrawable(hex, selected)
            setOnClickListener { onClick() }
        }
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
            setPadding(0, 0, 0, dp(10))

            val iconView = TextView(this@MainActivity).apply {
                this.text = icon
                textSize = 11f
                setTextColor(textSecondary)
                gravity = Gravity.CENTER
                background = roundedFill(Color.parseColor("#1A2230"), 10)
                setPadding(dp(7), dp(5), dp(7), dp(5))
            }

            val labelView = TextView(this@MainActivity).apply {
                this.text = text
                textSize = 11f
                setTextColor(accent)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.06f
                setPadding(dp(8), 0, 0, 0)
            }

            addView(iconView)
            addView(labelView)
        }
    }

    private fun sectionPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(22).toFloat()
                setColor(panelColor)
                setStroke(dp(1), Color.parseColor("#2A3444"))
            }
            elevation = dp(3).toFloat()
            translationZ = dp(1).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
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

    private fun premiumToggleRow(
        title: String,
        subtitle: String,
        checked: Boolean,
        onToggle: (Boolean) -> Unit
    ): LinearLayout {
        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }

        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleView)
            addView(subtitleView)
        }

        val toggle = android.widget.Switch(this).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBg(Color.parseColor("#161D28"), Color.parseColor("#253041"), 18)

            addView(textCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(toggle)
        }
    }

    private fun segmentedChip(
        label: String,
        selected: Boolean,
        onClick: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(
                if (selected) panelPressed else Color.parseColor("#1A2230"),
                999
            )
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setOnClickListener { onClick() }
            applyPressEffect(this)
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
                    v.alpha = 0.96f
                    v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80).start()
                    v.background = when (v) {
                        is Button -> roundedFill(pressedColor, 16)
                        else -> roundedBg(pressedColor, highlightBorder, 18)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1f
                    v.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
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
