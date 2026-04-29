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
import android.widget.EditText
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import android.widget.Toast
import com.elitedarkkaiser.redmagic.storage.AppPrefs

class MainActivity : Activity() {

    private var useFahrenheit = true
    private val firstInstallPermissionsPromptedKey = "first_install_permissions_prompted"

    private fun isUseFahrenheitSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.TEMP_UNIT_FAHRENHEIT, true)
    }

    private fun saveUseFahrenheit(useF: Boolean) {
        prefs().edit().putBoolean(AppPrefs.TEMP_UNIT_FAHRENHEIT, useF).apply()
    }

    private fun formatDisplayTempFromF(tempF: Float?): String {
        return TempFormat.formatDisplayTempFromF(tempF, useFahrenheit)
    }

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
    private lateinit var rootChip: TextView
    private lateinit var fanChip: TextView
    private lateinit var rpmChip: TextView
    private lateinit var tempChip: TextView

    private lateinit var homeTab: LinearLayout
    private lateinit var coolingTab: LinearLayout
    private lateinit var controlsTab: LinearLayout
    private lateinit var lightingTab: LinearLayout
    private var magicKeyStatusLabelRef: TextView? = null
    private var dialogRefreshPump: (() -> Unit)? = null
    private var dialogRefreshShoulderLed: (() -> Unit)? = null
    private var dialogRefreshLogoLed: (() -> Unit)? = null
    private var dialogRefreshFanLed: (() -> Unit)? = null
    private var gameModeAppsTextRef: TextView? = null

    private var smartPumpStatusView: TextView? = null
    private var smartPumpSpeedView: TextView? = null

    private lateinit var homeNav: LinearLayout
    private lateinit var coolingNav: LinearLayout
    private lateinit var controlsNav: LinearLayout
    private lateinit var lightingNav: LinearLayout

    private var selectedCurve = "balanced"
    private var autoFanCurveEnabled = false
    private var realTimePreviewEnabled = true

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
    private var autoPumpEnabled = false

    private val prefsName = "redmagic_hw_controls_prefs"



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
    private val typeface: Typeface? = Typeface.SANS_SERIF
    private val highlightBorder = Color.parseColor("#7F8EA3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initDefaultTriggerMappings()
        runBackgroundDeviceScan()

        if (!RootShell.hasRoot()) {
            showRootRequiredDialog()
            return
        }

        if (!prefs().getBoolean(firstInstallPermissionsPromptedKey, false)) {
            showFirstInstallPermissionsDialog()
            return
        }

        launchMainUi()
    }


    private fun runBackgroundDeviceScan() {
        Thread {
            val report = DeviceCapabilityScanner.scan()
            prefs().edit()
                .putString("device_scan_model", report.model)
                .putString("device_scan_summary", report.summary)
                .putBoolean("device_scan_supported_model", report.isKnownRedmagic11Pro)
                .putBoolean("device_scan_fan_available", report.fanAvailable)
                .putBoolean("device_scan_pump_available", report.pumpAvailable)
                .putBoolean("device_scan_led_available", report.ledAvailable)
                .putBoolean("device_scan_triggers_available", report.triggersAvailable)
                .putLong("device_scan_last_run", System.currentTimeMillis())
                .apply()
        }.start()
    }

    private fun showFirstInstallPermissionsDialog() {
        AlertDialog.Builder(this)
            .setTitle("App permissions setup")
            .setMessage(
                "RedMagic Control needs notification permission for foreground services and Usage Access for Game Mode detection. " +
                    "These are requested now so permission prompts do not appear later throughout the UI. " +
                    "\n\nAfter granting Usage Access, return to the app."
            )
            .setCancelable(false)
            .setPositiveButton("Start setup") { _, _ ->
                prefs().edit().putBoolean(firstInstallPermissionsPromptedKey, true).apply()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        4101
                    )
                }

                openUsageStatsAccessSettings()

                launchMainUi()
            }
            .show()
    }

    private fun prefs() = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(android.app.AppOpsManager::class.java)
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageStatsAccessSettings() {
        startActivity(
            android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }


    private fun startGameModeService() {
        if (!hasUsageStatsPermission()) {
            android.widget.Toast.makeText(
                this,
                "Grant Usage Access to enable Game Mode",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        startService(Intent(this, GameModeService::class.java))
    }


    private fun setSelectedCurveSaved(value: String) {
        prefs().edit().putString(AppPrefs.SELECTED_CURVE, value).apply()
    }

    private fun getSelectedCurveSaved(): String {
        return prefs().getString(AppPrefs.SELECTED_CURVE, "balanced") ?: "balanced"
    }


    private fun isAutoFanEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.AUTO_FAN_ENABLED, false)
    }

    private fun setAutoFanEnabledSaved(enabled: Boolean) {
        prefs().edit().putBoolean(AppPrefs.AUTO_FAN_ENABLED, enabled).apply()
    }

    private fun isRealTimePreviewEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.REALTIME_PREVIEW_ENABLED, true)
    }

    private fun saveRealTimePreviewEnabled(enabled: Boolean) {
        prefs().edit().putBoolean(AppPrefs.REALTIME_PREVIEW_ENABLED, enabled).apply()
    }

    private fun applyLaunchAppMagicKeyMode(
        pkg: String,
        label: String,
        statusLabel: TextView,
        sliderButton: Button
    ) {
        val ok = HardwareController.setSliderLaunchApp(pkg)
        if (ok) {
            saveMagicKeyAppPackageStorage(this, pkg)
            sliderButton.text = "MAGIC KEY APP: $label"
            statusLabel.text = "Current: Launch App"
            refreshStatus()
            Toast.makeText(this, "Magic Key set to launch $label", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to set Magic Key app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableMagicKeyMode(statusLabel: TextView, sliderButton: Button? = null) {
        val ok = HardwareController.disableSliderSystemHandling()
        if (ok) {
            saveMagicKeyAppPackageStorage(this, null)
            sliderButton?.text = "MAGIC KEY APP: Choose App"
            statusLabel.text = "Current: Disabled"
            refreshStatus()
            Toast.makeText(this, "Magic Key disabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to disable Magic Key", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMagicKeyAppPicker(targetButton: Button) {
        MagicKeyAppPickerDialog.show(
            activity = this,
            targetButton = targetButton,
            statusLabel = magicKeyStatusLabelRef,
            applyLaunchAppMagicKeyMode = { pkg, label, statusLabel, sliderButton ->
                applyLaunchAppMagicKeyMode(
                    pkg = pkg,
                    label = label,
                    statusLabel = statusLabel,
                    sliderButton = sliderButton
                )
            },
            deps = MagicKeyAppPickerDialog.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) }
            )
        )
    }

    private fun isFanLedEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.FAN_LED_ENABLED, false)
    }

    private fun savedFanLedEffect(): String {
        return prefs().getString(AppPrefs.FAN_LED_EFFECT, "steady") ?: "steady"
    }

    private fun savedFanLedColor(): Int {
        return prefs().getInt(AppPrefs.FAN_LED_COLOR, 5)
    }

    private fun applyFanLedSelection(effect: String, color: Int) {
        if (effect.startsWith("preset:")) {
            applyFanPreset(effect.removePrefix("preset:"))
        } else {
            HardwareController.setFanLedEffect(effect, color)
        }
    }

    private fun saveFanLedState() {
        prefs().edit()
            .putBoolean(AppPrefs.FAN_LED_ENABLED, fanLedEnabled)
            .putString(AppPrefs.FAN_LED_EFFECT, fanLedEffect)
            .putInt(AppPrefs.FAN_LED_COLOR, fanLedColor)
            .commit()
    }

    private fun applySavedFanLedStateOnLaunch() {
        fanLedEnabled = isFanLedEnabledSaved()
        fanLedEffect = savedFanLedEffect()
        fanLedColor = savedFanLedColor()
    }

    private fun isLogoLedEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.LOGO_LED_ENABLED, true)
    }

    private fun savedLogoLedEffect(): String {
        return prefs().getString(AppPrefs.LOGO_LED_EFFECT, "steady") ?: "steady"
    }

    private fun savedLogoLedColor(): Int {
        return prefs().getInt(AppPrefs.LOGO_LED_COLOR, 1)
    }

    private fun saveLogoLedState() {
        prefs().edit()
            .putBoolean(AppPrefs.LOGO_LED_ENABLED, logoLedEnabled)
            .putString(AppPrefs.LOGO_LED_EFFECT, logoLedEffect)
            .putInt(AppPrefs.LOGO_LED_COLOR, logoLedColor)
            .commit()
    }

    private fun applySavedLogoLedStateOnLaunch() {
        logoLedEnabled = isLogoLedEnabledSaved()
        logoLedEffect = savedLogoLedEffect()
        logoLedColor = savedLogoLedColor()
    }

    private fun isShoulderLedEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.SHOULDER_LED_ENABLED, true)
    }

    private fun savedShoulderLedEffect(): String {
        return prefs().getString(AppPrefs.SHOULDER_LED_EFFECT, "breathe") ?: "breathe"
    }

    private fun savedShoulderLedColor(): Int {
        return prefs().getInt(AppPrefs.SHOULDER_LED_COLOR, 8)
    }

    private fun saveShoulderLedState() {
        prefs().edit()
            .putBoolean(AppPrefs.SHOULDER_LED_ENABLED, shoulderLedEnabled)
            .putString(AppPrefs.SHOULDER_LED_EFFECT, shoulderLedEffect)
            .putInt(AppPrefs.SHOULDER_LED_COLOR, shoulderLedColor)
            .commit()
    }

    private fun applySavedShoulderLedStateOnLaunch() {
        shoulderLedEnabled = isShoulderLedEnabledSaved()
        shoulderLedEffect = savedShoulderLedEffect()
        shoulderLedColor = savedShoulderLedColor()
    }

    private fun isPumpEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.PUMP_ENABLED, false)
    }

    private fun savedPumpProfile(): String {
        return prefs().getString(AppPrefs.PUMP_PROFILE, "quick") ?: "quick"
    }

    private fun savePumpState() {
        prefs().edit()
            .putBoolean(AppPrefs.PUMP_ENABLED, pumpEnabled)
            .putString(AppPrefs.PUMP_PROFILE, pumpProfile)
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
        return prefs().getBoolean(AppPrefs.PUMP_EXPERIMENTAL_ACCEPTED, false)
    }

    private fun setPumpExperimentalAccepted(accepted: Boolean) {
        prefs().edit().putBoolean(AppPrefs.PUMP_EXPERIMENTAL_ACCEPTED, accepted).commit()
    }

    private fun isAutoPumpEnabledSaved(): Boolean {
        return prefs().getBoolean(AppPrefs.AUTO_PUMP_ENABLED, false)
    }

    private fun saveAutoPumpState() {
        prefs().edit().putBoolean(AppPrefs.AUTO_PUMP_ENABLED, autoPumpEnabled).commit()
    }

    private fun buildAutoPumpStatusText(): Pair<String, String> {
        val tempF = DashboardSnapshot.readCpuTempF().toFloatOrNull()
        if (tempF == null) {
            return "Pump Mode: AUTO • Unknown temp" to "Speed: ? • Freq: ?"
        }

        val profile = when {
            tempF >= 105f -> "Quick"
            tempF >= 90f -> "Medium"
            else -> "Slow"
        }

        val speed = when (profile) {
            "Quick" -> 80
            "Medium" -> 60
            else -> 40
        }

        return "Pump Mode: AUTO • $profile (${formatDisplayTempFromF(tempF)})" to
            "Speed: $speed • Freq: 4"
    }


    private fun buildCurrentHardwareProfile(name: String): HardwareProfile {
        val triggerPrefs = getSharedPreferences("triggers", MODE_PRIVATE)

        return ProfileStateHelpers.buildCurrentHardwareProfile(
            name = name,
            input = ProfileStateHelpers.ProfileInputs(
                fanEnabled = HardwareController.isFanEnabled(),
                fanLevel = fanSeek.progress,
                autoFanEnabled = autoFanCurveEnabled,
                fanCurveMode = selectedCurve,

                pumpEnabled = pumpEnabled,
                pumpProfile = pumpProfile,
                autoPumpEnabled = autoPumpEnabled,

                fanLedEnabled = fanLedEnabled,
                fanLedEffect = fanLedEffect,
                fanLedColor = fanLedColor,

                logoLedEnabled = logoLedEnabled,
                logoLedEffect = logoLedEffect,
                logoLedColor = logoLedColor,

                shoulderLedEnabled = shoulderLedEnabled,
                shoulderLedEffect = shoulderLedEffect,
                shoulderLedColor = shoulderLedColor,

                triggerEnabled = triggerPrefs.getBoolean("triggers_auto_start", false),
                hapticsEnabled = triggerPrefs.getBoolean("haptics_enabled", true),
                leftTriggerAction = triggerPrefs.getString("left_trigger", "NONE") ?: "NONE",
                rightTriggerAction = triggerPrefs.getString("right_trigger", "NONE") ?: "NONE",
                intentUnlockRightTrigger = triggerPrefs.getBoolean("intent_unlock_right_trigger", true),
                triggersAutoStart = triggerPrefs.getBoolean("triggers_auto_start", false)
            )
        )
    }

    private fun applyProfileToUiState(profile: HardwareProfile) {
        ProfileStateHelpers.applyProfileToUiState(
            profile = profile,

            setAutoFanEnabled = { value -> autoFanCurveEnabled = value },
            setFanCurveMode = { value -> selectedCurve = value },

            setPumpEnabled = { value -> pumpEnabled = value },
            setPumpProfile = { value -> pumpProfile = value },
            setAutoPumpEnabled = { value -> autoPumpEnabled = value },

            setFanLedEnabled = { value -> fanLedEnabled = value },
            setFanLedEffect = { value -> fanLedEffect = value },
            setFanLedColor = { value -> fanLedColor = value },

            setLogoLedEnabled = { value -> logoLedEnabled = value },
            setLogoLedEffect = { value -> logoLedEffect = value },
            setLogoLedColor = { value -> logoLedColor = value },

            setShoulderLedEnabled = { value -> shoulderLedEnabled = value },
            setShoulderLedEffect = { value -> shoulderLedEffect = value },
            setShoulderLedColor = { value -> shoulderLedColor = value },

            setFanLevel = { value -> fanSeek.progress = value },
            saveTriggerPrefs = { applied -> saveTriggerPrefs(applied) },
            enableTriggersIfNeeded = { applied ->
                if (applied.triggersAutoStart) {
                    HardwareController.enableTriggers()
                    startService(Intent(this, TriggerRootService::class.java))
                }
            },
            afterProfileApplied = { applied ->
                ProfileActions.afterProfileApplied(
                    profile = applied,
                    setAutoFanEnabledSaved = { enabled -> setAutoFanEnabledSaved(enabled) },
                    savePumpState = { savePumpState() },
                    saveAutoPumpState = { saveAutoPumpState() },
                    saveFanLedState = { saveFanLedState() },
                    saveLogoLedState = { saveLogoLedState() },
                    saveShoulderLedState = { saveShoulderLedState() },
                    startAutoFanService = { startAutoFanService() },
                    stopAutoFanService = { stopAutoFanService() },
                    startAutoPumpService = { startAutoPumpService() },
                    stopAutoPumpService = { stopAutoPumpService() },
                    refreshStatus = { refreshStatus() },
                    refreshSmartPumpStatusViews = { refreshSmartPumpStatusViews() }
                )
            }
        )
    }

    private fun saveTriggerPrefs(profile: HardwareProfile) {
        getSharedPreferences("triggers", MODE_PRIVATE)
            .edit()
            .putString("left_trigger", profile.leftTriggerAction)
            .putString("right_trigger", profile.rightTriggerAction)
            .putBoolean("haptics_enabled", profile.hapticsEnabled)
            .putBoolean("intent_unlock_right_trigger", profile.intentUnlockRightTrigger)
            .putBoolean("triggers_auto_start", profile.triggersAutoStart)
            .apply()
    }

    private fun showSaveProfileDialog(onSaved: () -> Unit) {
        ProfileDialogs.showStyledSaveProfileDialog(
            context = this,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            panelColor = panelColor,
            borderColor = borderColor,
            typeface = typeface,
            dp = { value -> dp(value) },
            roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
            actionButton = { text, isDanger, onClick ->
                actionButton(text, isDanger = isDanger, onClick = onClick)
            },
            space = { value -> space(value) },
            buildProfile = { name -> buildCurrentHardwareProfile(name) },
            onSaved = onSaved
        )
    }

    private fun showDeleteProfileDialog(profileName: String, onDeleted: () -> Unit) {
        ProfileDialogs.showDeleteProfileDialog(this, profileName) {
            ProfileManager.deleteProfile(this, profileName)
            Toast.makeText(this, "Deleted $profileName", Toast.LENGTH_SHORT).show()
            onDeleted()
        }
    }

    private fun refreshSmartPumpStatusViews() {
        val statusView = smartPumpStatusView ?: return
        val speedView = smartPumpSpeedView ?: return

        if (autoPumpEnabled) {
            val status = buildAutoPumpStatusText()
            statusView.text = status.first
            speedView.text = status.second
        } else {
            val manualLabel = pumpProfile.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            val manualSpeed = when (pumpProfile.lowercase()) {
                "slow" -> 40
                "medium" -> 60
                "quick" -> 80
                "experimental" -> 90
                else -> 60
            }
            statusView.text = "Pump Mode: MANUAL • $manualLabel"
            speedView.text = "Speed: $manualSpeed • Freq: 4"
        }
    }

    private fun applyPumpProfile(profile: String) {
        pumpProfile = profile
        pumpEnabled = true
        autoPumpEnabled = false
        savePumpState()
        saveAutoPumpState()
        stopAutoPumpService()
        HardwareController.setPumpProfile(profile)
        refreshStatus()
        refreshSmartPumpStatusViews()
    }

    private fun confirmExperimentalPumpThenApply() {
        if (isPumpExperimentalAccepted()) {
            applyPumpProfile("experimental")
            return
        }

        ExperimentalPumpDialog.show(
            activity = this,
            onCancel = { },
            onConfirm = {
                setPumpExperimentalAccepted(true)
                applyPumpProfile("experimental")
            },
            deps = ExperimentalPumpDialog.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) }
            )
        )
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
    private fun showRootRequiredDialog() {
        DeviceGateDialogs.showRootRequiredDialog(
            activity = this,
            onClose = { finish() },
            deps = DeviceGateDialogs.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) }
            )
        )
    }
    private fun launchMainUi() {
        MainUiStartup.applySavedHardwareState(
            applySavedFanLedStateOnLaunch = { applySavedFanLedStateOnLaunch() },
            applySavedLogoLedStateOnLaunch = { applySavedLogoLedStateOnLaunch() },
            applySavedShoulderLedStateOnLaunch = { applySavedShoulderLedStateOnLaunch() },
            applySavedPumpStateOnLaunch = { applySavedPumpStateOnLaunch() },
            setRealTimePreviewEnabled = { value -> realTimePreviewEnabled = value },
            isRealTimePreviewEnabledSaved = { isRealTimePreviewEnabledSaved() },
            setUseFahrenheit = { value -> useFahrenheit = value },
            isUseFahrenheitSaved = { isUseFahrenheitSaved() },
            setAutoPumpEnabled = { value -> autoPumpEnabled = value },
            isAutoPumpEnabledSaved = { isAutoPumpEnabledSaved() }
        )

        val result = MainUiLauncher.launch(
            activity = this,
            topInset = getStatusBarHeight(),
            bgColor = bgColor,
            dp = { value -> dp(value) },
            createHomeTab = { createHomeTab() },
            createCoolingTab = { createCoolingTab() },
            createControlsTab = { createControlsTab() },
            createHardwareTab = { createHardwareTab() },
            createLightingTab = { createLightingTab() },
            bottomNavBar = { bottomNavBar() }
        )

        homeTab = result.homeTab
        coolingTab = result.coolingTab
        controlsTab = result.controlsTab
        lightingTab = result.lightingTab

        MainUiStartup.applyLaunchHardware(
            fanLedEnabled = fanLedEnabled,
            fanLedEffect = fanLedEffect,
            fanLedColor = fanLedColor,
            logoLedEnabled = logoLedEnabled,
            logoLedEffect = logoLedEffect,
            logoLedColor = logoLedColor,
            shoulderLedEnabled = shoulderLedEnabled,
            shoulderLedEffect = shoulderLedEffect,
            shoulderLedColor = shoulderLedColor,
            pumpEnabled = pumpEnabled,
            pumpProfile = pumpProfile,
            applyFanLedSelection = { effect, color -> applyFanLedSelection(effect, color) },
            startFanLedService = { startFanLedService() },
            stopFanLedService = { stopFanLedService() }
        )

        if (autoPumpEnabled) {
            startAutoPumpService()
        }

        restoreFanCurveUiState()
        switchTab("home")
        refreshStatus()
        startGameModeService()
        startService(Intent(this, ChargingModeService::class.java))
    }

    private fun restoreFanCurveUiState() {
        selectedCurve = getSelectedCurveSaved()
        autoFanCurveEnabled = isAutoFanEnabledSaved()
        autoCurveCheck.isChecked = autoFanCurveEnabled

        if (autoFanCurveEnabled) {
            curveStatusText.text = "Auto fan curve active • Running in background service"
        } else {
            curveStatusText.text = "Selected curve: $selectedCurve • Manual control"
        }

        when (selectedCurve) {
            "quiet" -> setActiveMode(quietCardRef)
            "turbo" -> setActiveMode(turboCardRef)
            else -> setActiveMode(balancedCardRef)
        }
        updateManualCurveUiState()
    }

    private fun createHomeTab(): LinearLayout {
        val result = com.elitedarkkaiser.redmagic.ui.HomeTabUi.create(
            com.elitedarkkaiser.redmagic.ui.HomeTabDeps(
                scrollTabContainer = { scrollTabContainer() },
                sectionPanel = { sectionPanel() },
                sectionHeader = { icon, text -> sectionHeader(icon, text) },
                subtitleText = { text -> subtitleText(text) },
                bodyText = { text -> bodyText(text) },
                ledTitleText = { text -> ledTitleText(text) },
                infoValue = { infoValue() },
                infoRow = { label, valueView -> infoRow(label, valueView) },
                statusChip = { text -> statusChip(text) },
                actionButton = { text, isDanger, onClick -> actionButton(text, isDanger, onClick) },
                singleRow = { button -> singleRow(button) },
                segmentedChip = { label, selected, onClick -> segmentedChip(label, selected, onClick) },
                space = { width -> space(width) },
                dp = { value -> dp(value) },
                hasUsageStatsPermission = { hasUsageStatsPermission() },
                openUsageStatsAccessSettings = { openUsageStatsAccessSettings() },
                showGamePickerDialog = { showGamePickerDialog() },
                updateGameModeStatusUI = { textView -> updateGameModeStatusUI(textView) },
                openUrl = { url -> openUrl(url) },
                deviceScanSummary = {
                    prefs().getString("device_scan_summary", "Device scan pending…") ?: "Device scan pending…"
                }
            )
        )

        deviceModelValue = result.refs.deviceModelValue
        deviceRomValue = result.refs.deviceRomValue
        deviceCpuValue = result.refs.deviceCpuValue
        deviceRamValue = result.refs.deviceRamValue
        rootChip = result.refs.rootChip
        fanChip = result.refs.fanChip
        rpmChip = result.refs.rpmChip
        tempChip = result.refs.tempChip

        return result.view
    }

    private fun createCoolingTab(): LinearLayout {
        val result = com.elitedarkkaiser.redmagic.ui.CoolingTabUi.create(coolingTabDeps())

        assignCoolingRefs(result.refs)

        return result.view
    }

    private fun coolingTabDeps(): com.elitedarkkaiser.redmagic.ui.CoolingTabDeps {
        return com.elitedarkkaiser.redmagic.ui.CoolingTabDeps(
                scrollTabContainer = { scrollTabContainer() },
                sectionPanel = { sectionPanel() },
                sectionHeader = { icon, text -> sectionHeader(icon, text) },
                subtleLabel = { text -> subtleLabel(text) },
                bodyText = { text -> bodyText(text) },
                segmentedChip = { label, selected, onClick -> segmentedChip(label, selected, onClick) },
                actionButton = { text, isDanger, onClick -> actionButton(text, isDanger, onClick) },
                row = { left, right -> row(left, right) },
                singleRow = { button -> singleRow(button) },
                space = { width -> space(width) },
                spacer = { height -> spacer(height) },
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radiusDp -> roundedBg(fill, stroke, radiusDp) },

                getSelectedCurve = { selectedCurve },
                setSelectedCurve = { value -> selectedCurve = value },
                setSelectedCurveSaved = { value -> setSelectedCurveSaved(value) },

                getAutoFanCurveEnabled = { autoFanCurveEnabled },
                setAutoFanCurveEnabled = { value -> autoFanCurveEnabled = value },
                setAutoFanEnabledSaved = { value -> setAutoFanEnabledSaved(value) },

                getUseFahrenheit = { useFahrenheit },
                setUseFahrenheit = { value -> useFahrenheit = value },
                saveUseFahrenheit = { value -> saveUseFahrenheit(value) },

                getPumpEnabled = { pumpEnabled },
                setPumpEnabled = { value -> pumpEnabled = value },
                getPumpProfile = { pumpProfile },
                setPumpProfileValue = { value -> pumpProfile = value },
                getAutoPumpEnabled = { autoPumpEnabled },
                setAutoPumpEnabled = { value -> autoPumpEnabled = value },

                setSelectedFanProgress = { value -> fanSeek.progress = value },
                startAutoFanService = { startAutoFanService() },
                stopAutoFanService = { stopAutoFanService() },
                startAutoPumpService = { startAutoPumpService() },
                stopAutoPumpService = { stopAutoPumpService() },
                savePumpState = { savePumpState() },
                saveAutoPumpState = { saveAutoPumpState() },
                refreshStatus = { refreshStatus() },
                refreshSmartPumpStatusViews = { refreshSmartPumpStatusViews() },
                buildAutoPumpStatusText = { buildAutoPumpStatusText() },
                applyPumpProfile = { profile -> applyPumpProfile(profile) },
                confirmExperimentalPumpThenApply = { confirmExperimentalPumpThenApply() },
                updateManualCurveUiState = { updateManualCurveUiState() }
            )
    }

    private fun assignCoolingRefs(refs: com.elitedarkkaiser.redmagic.ui.CoolingTabUi.Refs) {
        tempText = refs.tempText
        curveStatusText = refs.curveStatusText
        fanSeek = refs.fanSeek
        autoCurveCheck = refs.autoCurveCheck
        quietCardRef = refs.quietCardRef
        balancedCardRef = refs.balancedCardRef
        turboCardRef = refs.turboCardRef
        smartPumpStatusView = refs.smartPumpStatusView
        smartPumpSpeedView = refs.smartPumpSpeedView
    }

    private fun applyFanLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (fanLedEnabled) {
            applyFanLedSelection(fanLedEffect, fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }
    }

    private fun applyLogoLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (logoLedEnabled) {
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }
    }

    private fun applyShoulderLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }
    }

    private fun createControlsTab(): LinearLayout {
        val result = com.elitedarkkaiser.redmagic.ui.ControlsTabUi.create(
            this,
            com.elitedarkkaiser.redmagic.ui.ControlsTabDeps(
                scrollTabContainer = { scrollTabContainer() },
                sectionPanel = { sectionPanel() },
                sectionHeader = { icon, text -> sectionHeader(icon, text) },
                bodyText = { text -> bodyText(text) },
                subtleLabel = { text -> subtleLabel(text) },
                actionButton = { text, isDanger, onClick -> actionButton(text, isDanger, onClick) },
                smallActionButton = { text, isDanger, onClick -> smallActionButton(text, isDanger, onClick) },
                singleRow = { button -> singleRow(button) },
                row = { left, right -> row(left, right) },
                flowRow = { views -> flowRow(*views) },
                space = { width -> space(width) },
                spacer = { height -> spacer(height) },
                dp = { value -> dp(value) },

                refreshStatus = { refreshStatus() },
                readMagicKeyModeLabel = { MagicKeyActions.readModeLabel() },
                applyStockMagicKeyMode = { label, action, statusLabel, sliderButton ->
                    MagicKeyActions.applyStockMode(
                        activity = this,
                        label = label,
                        applyMode = action,
                        statusLabel = statusLabel,
                        sliderButton = sliderButton,
                        refreshStatus = { refreshStatus() }
                    )
                },
                disableMagicKeyMode = { statusLabel, sliderButton ->
                    disableMagicKeyMode(statusLabel, sliderButton)
                },
                resolveMagicKeyAppLabel = { pkg -> MagicKeyActions.resolveAppLabel(this, pkg) },
                savedMagicKeyAppPackage = { savedMagicKeyAppPackageStorage(this) },
                showMagicKeyAppPicker = { button -> showMagicKeyAppPicker(button) }
            )
        )

        magicKeyStatusLabelRef = result.refs.magicKeyStatusLabel
        return result.view
    }

    private fun createHardwareTab(): LinearLayout {
        return com.elitedarkkaiser.redmagic.ui.HardwareTabUi.create(
            this,
            com.elitedarkkaiser.redmagic.ui.HardwareTabDeps(
                scrollTabContainer = { scrollTabContainer() },
                sectionPanel = { sectionPanel() },
                sectionHeader = { icon, text -> sectionHeader(icon, text) },
                bodyText = { text -> bodyText(text) },
                subtleLabel = { text -> subtleLabel(text) },
                actionButton = { text, isDanger, onClick -> actionButton(text, isDanger, onClick) },
                singleRow = { button -> singleRow(button) },
                row = { left, right -> row(left, right) },
                space = { width -> space(width) },
                dp = { value -> dp(value) },

                showTriggerSetupDialog = { showTriggerSetupDialog() },
                enableTriggersAndService = {
                    HardwareController.enableTriggers()
                    startService(Intent(this, TriggerRootService::class.java))
                    refreshStatus()
                    Toast.makeText(this, "Triggers enabled", Toast.LENGTH_SHORT).show()
                },
                testHaptic = {
                    HardwareController.vibrate(durationMs = 100, gain = 220)
                    Toast.makeText(this, "Haptic test sent", Toast.LENGTH_SHORT).show()
                },

                loadProfiles = { ProfileManager.loadProfiles(this) },
                applyHardwareProfile = { profile -> HardwareController.applyHardwareProfile(profile) },
                applyProfileToUiState = { profile -> applyProfileToUiState(profile) },
                showSaveProfileDialog = { onSaved -> showSaveProfileDialog(onSaved) },
                showDeleteProfileDialog = { profileName, onDeleted -> showDeleteProfileDialog(profileName, onDeleted) }
            )
        )
    }

    private fun createLightingTab(): LinearLayout {
        return com.elitedarkkaiser.redmagic.ui.LightingTabUi.create(
            this,
            com.elitedarkkaiser.redmagic.ui.LightingTabDeps(
                scrollTabContainer = { scrollTabContainer() },
                sectionPanel = { sectionPanel() },
                sectionHeader = { icon, text -> sectionHeader(icon, text) },
                bodyText = { text -> bodyText(text) },
                subtleLabel = { text -> subtleLabel(text) },
                infoRow = { label, valueView -> infoRow(label, valueView) },
                actionButton = { text, isDanger, onClick -> actionButton(text, isDanger, onClick) },
                singleRow = { button -> singleRow(button) },
                row = { left, right -> row(left, right) },
                dp = { value -> dp(value) },

                getRealTimePreviewEnabled = { realTimePreviewEnabled },
                setRealTimePreviewEnabled = { value -> realTimePreviewEnabled = value },
                saveRealTimePreviewEnabled = { value -> saveRealTimePreviewEnabled(value) },

                showFanLedDialog = { showFanLedDialog() },
                showLogoLedDialog = { showLogoLedDialog() },
                showShoulderLedDialog = { showShoulderLedDialog() },
                showGameModeAppPicker = { showGamePickerDialog() },
                showGameModeProfileDialog = { showGameModeProfileDialog() },
                gameModeAppsSummary = { gameModeAppsSummaryStorage(this) },

                getChargingLedEnabled = { ChargingLedState.isEnabled(this) },
                setChargingLedEnabled = { enabled ->
                    prefs().edit().putBoolean(ChargingLedState.ENABLED_KEY, enabled).apply()
                    startService(Intent(this, ChargingModeService::class.java))
                },
                showChargingFanLedDialog = { showChargingFanLedDialog() },
                showChargingLogoLedDialog = { showChargingLogoLedDialog() },
                showChargingShoulderLedDialog = { showChargingShoulderLedDialog() }
            )
        )
    }

    private fun chargingLedDialogDeps(): ChargingLedProfileDialog.Deps {
        return ChargingLedProfileDialog.Deps(
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            panelColor = panelColor,
            borderColor = borderColor,
            panelPressed = panelPressed,
            accent = accent,
            typeface = typeface,
            dp = { value -> dp(value) },
            roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
            roundedFill = { color, radius -> roundedFill(color, radius) },
            space = { value -> space(value) },
            colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
            colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) },
            fanPresetBubble = { h1, h2, h3, h4, value, selected, onClick ->
                fanPresetBubble(h1, h2, h3, h4, presetValue = value, selectedOverride = { selected }, onClick = onClick)
            }
        )
    }

    private fun saveChargingLedProfile(
        enabledKey: String,
        effectKey: String,
        colorKey: String,
        enabled: Boolean,
        effect: String,
        color: Int
    ) {
        val savedColor = if (
            effectKey == ChargingLedState.FAN_EFFECT_KEY &&
            effect.startsWith("preset:")
        ) {
            -1
        } else {
            color
        }

        prefs().edit()
            .putBoolean(enabledKey, enabled)
            .putString(effectKey, effect)
            .putInt(colorKey, savedColor)
            .apply()

        startService(Intent(this, ChargingModeService::class.java))
        if (ChargingLedState.isEnabled(this) && ChargingLedState.isChargingNow(this)) {
            ChargingLedState.setActive(this, true)
            ChargingLedState.applyChargingProfile(this)
        }
    }

    private fun showChargingFanLedDialog() {
        var chargingFanEnabled = prefs().getBoolean(ChargingLedState.FAN_ENABLED_KEY, true)
        var chargingFanEffect = prefs().getString(ChargingLedState.FAN_EFFECT_KEY, "steady") ?: "steady"
        var chargingFanColor = prefs().getInt(ChargingLedState.FAN_COLOR_KEY, 5)
        var chargingFanDialogRefresh: (() -> Unit)? = null

        if (chargingFanEffect.startsWith("preset:")) {
            chargingFanColor = -1
        }

        FanLedDialogUi.showFanLedDialog(
            activity = this,
            originalEnabled = chargingFanEnabled,
            originalEffect = chargingFanEffect,
            originalColor = chargingFanColor,
            currentEnabled = { chargingFanEnabled },
            currentEffect = { chargingFanEffect },
            currentColor = { chargingFanColor },
            setEnabled = { value -> chargingFanEnabled = value },
            setEffect = { value -> chargingFanEffect = value },
            setColor = { value -> chargingFanColor = value },
            applyPreviewIfEnabled = {
                if (ChargingLedState.isEnabled(this) && ChargingLedState.isChargingNow(this)) {
                    saveChargingLedProfile(
                        ChargingLedState.FAN_ENABLED_KEY,
                        ChargingLedState.FAN_EFFECT_KEY,
                        ChargingLedState.FAN_COLOR_KEY,
                        chargingFanEnabled,
                        chargingFanEffect,
                        chargingFanColor
                    )
                }
            },
            applySelection = { effect, color ->
                if (effect.startsWith("preset:")) {
                    HardwareController.setFanLedEnabled(true)
                    HardwareController.setFanLedStockPreset(effect.removePrefix("preset:"))
                } else {
                    HardwareController.setFanLedEffect(effect, color)
                }
            },
            disableLed = { HardwareController.setFanLedEnabled(false) },
            saveState = {
                saveChargingLedProfile(
                    ChargingLedState.FAN_ENABLED_KEY,
                    ChargingLedState.FAN_EFFECT_KEY,
                    ChargingLedState.FAN_COLOR_KEY,
                    chargingFanEnabled,
                    chargingFanEffect,
                    chargingFanColor
                )
            },
            startFanLedService = { startService(Intent(this, ChargingModeService::class.java)) },
            stopFanLedService = { startService(Intent(this, ChargingModeService::class.java)) },
            anyLedEnabled = { ChargingLedState.isEnabled(this) },
            applyFanPreset = { value ->
                chargingFanEnabled = true
                chargingFanEffect = "preset:$value"
                chargingFanColor = -1
                HardwareController.setFanLedEnabled(true)
                HardwareController.setFanLedStockPreset(value)
                chargingFanDialogRefresh?.invoke()
            },
            setDialogRefresh = { callback -> chargingFanDialogRefresh = callback },
            deps = FanLedDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                colorDot = { colorId, hex, onClick -> colorDot(colorId, hex, onClick) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) },
                fanPresetBubble = { c1, c2, c3, c4, presetValue, onClick ->
                    fanPresetBubble(c1, c2, c3, c4, presetValue = presetValue, selectedOverride = { chargingFanEffect == "preset:$presetValue" }, onClick = onClick)
                }
            ),
            title = "Charging Fan LED",
            subtitle = "Fan LED profile used only while plugged in and charging.",
            enableLabel = "Enable for charging mode"
        )
    }

    private fun showChargingLogoLedDialog() {
        ChargingLedProfileDialog.show(
            activity = this,
            title = "Charging Logo LED",
            subtitle = "Logo LED profile used only while plugged in and charging.",
            originalEnabled = prefs().getBoolean(ChargingLedState.LOGO_ENABLED_KEY, true),
            originalEffect = prefs().getString(ChargingLedState.LOGO_EFFECT_KEY, "steady") ?: "steady",
            originalColor = prefs().getInt(ChargingLedState.LOGO_COLOR_KEY, 1),
            onSave = { enabled, effect, color ->
                saveChargingLedProfile(
                    ChargingLedState.LOGO_ENABLED_KEY,
                    ChargingLedState.LOGO_EFFECT_KEY,
                    ChargingLedState.LOGO_COLOR_KEY,
                    enabled,
                    effect,
                    color
                )
            },
            deps = chargingLedDialogDeps()
        )
    }

    private fun showChargingShoulderLedDialog() {
        ChargingLedProfileDialog.show(
            activity = this,
            title = "Charging Shoulder LEDs",
            subtitle = "Shoulder LED profile used only while plugged in and charging.",
            originalEnabled = prefs().getBoolean(ChargingLedState.SHOULDER_ENABLED_KEY, true),
            originalEffect = prefs().getString(ChargingLedState.SHOULDER_EFFECT_KEY, "breathe") ?: "breathe",
            originalColor = prefs().getInt(ChargingLedState.SHOULDER_COLOR_KEY, 8),
            onSave = { enabled, effect, color ->
                saveChargingLedProfile(
                    ChargingLedState.SHOULDER_ENABLED_KEY,
                    ChargingLedState.SHOULDER_EFFECT_KEY,
                    ChargingLedState.SHOULDER_COLOR_KEY,
                    enabled,
                    effect,
                    color
                )
            },
            deps = chargingLedDialogDeps()
        )
    }

    private fun showTriggerSetupDialog() {
        TriggerSetupDialog.show(
            activity = this,
            deps = TriggerSetupDialog.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) }
            )
        )
    }

    private fun initDefaultTriggerMappings() {
        val p = getSharedPreferences("triggers", MODE_PRIVATE)
        if (!p.contains("left_trigger")) {
            p.edit()
                .putString("left_trigger", "VOL_DOWN")
                .putString("right_trigger", "VOL_UP")
                .apply()
        }
    }

    private fun showGameModeProfileDialog() {
        GameModeUi.showGameModeProfileDialog(
            activity = this,
            current = getSavedGameModeProfileStorage(this),
            deps = GameModeUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                space = { value -> space(value) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) },
                colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
            ),
            onSaveProfile = { profile ->
                saveGameModeProfileStorage(this, profile)
                GameModeActions.applyProfileNow(getSavedGameModeProfileStorage(this), applyFanLed = { effect, color -> applyFanLedSelection(effect, color) })
                startGameModeService()
            }
        )
    }

    private fun showPumpProfileDialog() {
        PumpDialogUi.showPumpProfileDialog(
            activity = this,
            originalEnabled = pumpEnabled,
            originalProfile = pumpProfile,
            currentProfile = { pumpProfile },
            setPumpEnabled = { value -> pumpEnabled = value },
            setPumpProfile = { value -> pumpProfile = value },
            applyHardwareProfile = { value -> HardwareController.setPumpProfile(value) },
            disablePump = { HardwareController.enablePump(false) },
            savePumpState = { savePumpState() },
            confirmExperimentalPumpThenApply = { confirmExperimentalPumpThenApply() },
            setDialogRefreshPump = { callback -> dialogRefreshPump = callback },
            deps = PumpDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                space = { value -> space(value) }
            )
        )
    }

    private fun showShoulderLedDialog() {
        ShoulderLedDialogUi.showShoulderLedDialog(
            activity = this,
            originalEnabled = shoulderLedEnabled,
            originalEffect = shoulderLedEffect,
            originalColor = shoulderLedColor,
            currentEnabled = { shoulderLedEnabled },
            currentEffect = { shoulderLedEffect },
            currentColor = { shoulderLedColor },
            setEnabled = { value -> shoulderLedEnabled = value },
            setEffect = { value -> shoulderLedEffect = value },
            setColor = { value -> shoulderLedColor = value },
            applyPreviewIfEnabled = { applyShoulderLedPreviewIfEnabled() },
            applyEffect = { effect, color -> HardwareController.setShoulderLedEffect(effect, color) },
            disableLed = { HardwareController.setShoulderLedEnabled(false) },
            saveState = { saveShoulderLedState() },
            startFanLedService = { startFanLedService() },
            stopFanLedService = { stopFanLedService() },
            anyLedEnabled = { fanLedEnabled || logoLedEnabled || shoulderLedEnabled },
            setDialogRefresh = { callback -> dialogRefreshShoulderLed = callback },
            deps = ShoulderLedDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) }
            )
        )
    }

    private fun showLogoLedDialog() {
        LogoLedDialogUi.showLogoLedDialog(
            activity = this,
            originalEnabled = logoLedEnabled,
            originalEffect = logoLedEffect,
            originalColor = logoLedColor,
            currentEnabled = { logoLedEnabled },
            currentEffect = { logoLedEffect },
            currentColor = { logoLedColor },
            setEnabled = { value -> logoLedEnabled = value },
            setEffect = { value -> logoLedEffect = value },
            setColor = { value -> logoLedColor = value },
            applyPreviewIfEnabled = { applyLogoLedPreviewIfEnabled() },
            applyEffect = { effect, color -> HardwareController.setLogoLedEffect(effect, color) },
            disableLed = { HardwareController.setLogoLedEnabled(false) },
            saveState = { saveLogoLedState() },
            startFanLedService = { startFanLedService() },
            stopFanLedService = { stopFanLedService() },
            anyLedEnabled = { fanLedEnabled || logoLedEnabled || shoulderLedEnabled },
            setDialogRefresh = { callback -> dialogRefreshLogoLed = callback },
            deps = LogoLedDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) }
            )
        )
    }

    private fun showFanLedDialog() {
        FanLedDialogUi.showFanLedDialog(
            activity = this,
            originalEnabled = fanLedEnabled,
            originalEffect = fanLedEffect,
            originalColor = fanLedColor,
            currentEnabled = { fanLedEnabled },
            currentEffect = { fanLedEffect },
            currentColor = { fanLedColor },
            setEnabled = { value -> fanLedEnabled = value },
            setEffect = { value -> fanLedEffect = value },
            setColor = { value -> fanLedColor = value },
            applyPreviewIfEnabled = { applyFanLedPreviewIfEnabled() },
            applySelection = { effect, color -> applyFanLedSelection(effect, color) },
            disableLed = { HardwareController.setFanLedEnabled(false) },
            saveState = { saveFanLedState() },
            startFanLedService = { startFanLedService() },
            stopFanLedService = { stopFanLedService() },
            anyLedEnabled = { fanLedEnabled || logoLedEnabled || shoulderLedEnabled },
            applyFanPreset = { preset -> applyFanPreset(preset) },
            setDialogRefresh = { callback -> dialogRefreshFanLed = callback },
            deps = FanLedDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                colorDot = { colorId, hex, onClick -> colorDot(colorId, hex, onClick) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) },
                fanPresetBubble = { c1, c2, c3, c4, presetValue, onClick ->
                    fanPresetBubble(c1, c2, c3, c4, presetValue = presetValue, onClick = onClick)
                }
            )
        )
    }

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

    private fun applyFanPreset(effectValue: String) {
        fanLedEnabled = true
        fanLedEffect = "preset:$effectValue"
        fanLedColor = -1

        HardwareController.setFanLedStockPreset(effectValue)
        dialogRefreshFanLed?.invoke()
    }

    private fun fanPresetBubble(
        vararg hexes: String,
        presetValue: String,
        selectedOverride: (() -> Boolean)? = null,
        onClick: () -> Unit
    ): View {
        require(hexes.size == 4) { "fanPresetBubble requires exactly 4 colors" }

        return object : View(this) {
            private val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            private val ringPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = dp(3).toFloat()
            }

            init {
                val size = dp(42)
                layoutParams = LinearLayout.LayoutParams(size, size)
                isClickable = true
                isFocusable = true
                setOnClickListener { onClick() }
            }

            override fun onDraw(canvas: android.graphics.Canvas) {
                super.onDraw(canvas)

                val pad = dp(3).toFloat()
                val rect = android.graphics.RectF(
                    pad,
                    pad,
                    width.toFloat() - pad,
                    height.toFloat() - pad
                )

                val saveCount = canvas.save()
                val clipPath = android.graphics.Path().apply {
                    addOval(rect, android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(clipPath)

                val midX = rect.centerX()
                val midY = rect.centerY()

                fillPaint.color = Color.parseColor(hexes[0])
                canvas.drawRect(rect.left, rect.top, midX, midY, fillPaint)

                fillPaint.color = Color.parseColor(hexes[1])
                canvas.drawRect(midX, rect.top, rect.right, midY, fillPaint)

                fillPaint.color = Color.parseColor(hexes[2])
                canvas.drawRect(rect.left, midY, midX, rect.bottom, fillPaint)

                fillPaint.color = Color.parseColor(hexes[3])
                canvas.drawRect(midX, midY, rect.right, rect.bottom, fillPaint)

                canvas.restoreToCount(saveCount)

                val selected = selectedOverride?.invoke() ?: (fanLedEffect == "preset:$presetValue")
                ringPaint.color = if (selected) {
                    Color.WHITE
                } else {
                    Color.TRANSPARENT
                }
                canvas.drawOval(rect, ringPaint)
            }
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
        val hardwareTab = (controlsTab.parent as ViewGroup).getChildAt(3)
        hardwareTab.visibility = if (tab == "hardware") View.VISIBLE else View.GONE
        lightingTab.visibility = if (tab == "lighting") View.VISIBLE else View.GONE

        setNavSelected(homeNav, tab == "home")
        setNavSelected(coolingNav, tab == "cooling")
        setNavSelected(controlsNav, tab == "controls")
        val hardwareNav = (controlsNav.parent as LinearLayout).getChildAt(3) as LinearLayout
        setNavSelected(hardwareNav, tab == "hardware")
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
        val hardwareNav = navItem("⌥", "Hardware") { switchTab("hardware") }
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
            addView(hardwareNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(lightingNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun navItem(icon: String, label: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 20f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
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
        tempChip.text = if (tempF != null) "TEMP ${formatDisplayTempFromF(tempF)}" else "TEMP --"

        tempText.text = if (tempF != null) "Current temp: ${formatDisplayTempFromF(tempF)}" else "Current temp: --"

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

    private fun ledTitleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(14), 0, 0, 0)
            setShadowLayer(dp(6).toFloat(), 0f, 0f, Color.parseColor("#5AA9FF"))

            val animator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                Color.parseColor("#7CC0FF"),
                Color.parseColor("#E8EEF7"),
                Color.parseColor("#7CC0FF")
            ).apply {
                duration = 1800L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    val c = anim.animatedValue as Int
                    setTextColor(c)
                    setShadowLayer(dp(8).toFloat(), 0f, 0f, c)
                }
            }

            post { animator.start() }
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
        return ModeCardUi.create(
            activity = this,
            title = title,
            subtitle = subtitle,
            onClick = onClick,
            deps = ModeCardUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                applyPressEffect = { view -> applyPressEffect(view) }
            )
        )
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

    private fun smallActionButton(label: String, isDanger: Boolean = false, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(if (isDanger) danger else panelPressed, 14)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setOnClickListener { onClick() }
            minHeight = dp(44)
        }
    }

    private fun flowRow(vararg views: View): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dp(6))
            views.forEachIndexed { index, v ->
                val params = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                if (index > 0) params.marginStart = dp(6)
                addView(v, params)
            }
        }
    }


    private fun showGamePickerDialog() {
        showGamePickerDialogUI(this) {
            gameModeAppsTextRef?.text = gameModeAppsSummaryStorage(this)
        }
    }




    private fun updateGameModeStatusUI(textView: TextView) {
        textView.text = getGameModeStatusTextStorage(this)
    }


}

