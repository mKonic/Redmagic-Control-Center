package com.elitedarkkaiser.redmagic

import kotlin.math.abs
import kotlin.math.round
import java.util.concurrent.ConcurrentHashMap

object HardwareController {

    private val recentHardwareWrites = ConcurrentHashMap<String, Long>()
    private const val DUPLICATE_WRITE_SKIP_MS = 2_000L

    private fun execHardwareWrite(key: String, command: String): Boolean {
        val now = System.currentTimeMillis()
        val last = recentHardwareWrites[key]
        if (last != null && (now - last) < DUPLICATE_WRITE_SKIP_MS) {
            android.util.Log.d("HardwareController", "skip duplicate write key=$key")
            return true
        }

        val ok = RootShell.exec(command)
        if (ok) recentHardwareWrites[key] = now
        return ok
    }

    private const val FAN_ENABLE = "/sys/kernel/fan/fan_enable"
    private const val FAN_LEVEL = "/sys/kernel/fan/fan_speed_level"
    private const val FAN_PWM = "/sys/kernel/fan/fan_speed_pwm"
    private const val FAN_RPM = "/sys/kernel/fan/fan_speed_count"

    private const val PUMP_ENABLE = "/proc/driver/micropump/enable"
    private const val PUMP_FREQ = "/proc/driver/micropump/freq"
    private const val PUMP_SPEED = "/proc/driver/micropump/speed"

    private const val LED_EFFECT = "/sys/class/leds/aw22xxx_led/effect"
    private const val LED_CFG = "/sys/class/leds/aw22xxx_led/cfg"

    private const val SAR0_MODE = "/sys/class/leds/sar0/mode_operation"
    private const val SAR1_MODE = "/sys/class/leds/sar1/mode_operation"

    private const val HAPTIC_DURATION = "/sys/class/leds/vibrator/duration"
    private const val HAPTIC_GAIN = "/sys/class/leds/vibrator/gain"
    private const val HAPTIC_ACTIVATE = "/sys/class/leds/vibrator/activate"

    fun enableFan(enabled: Boolean): Boolean {
        return execHardwareWrite("fan_enable:$enabled", "echo ${if (enabled) 1 else 0} > $FAN_ENABLE")
    }

    fun isFanEnabled(): Boolean {
        return RootShell.execForOutput("cat $FAN_ENABLE")?.trim() == "1"
    }

    fun setFanLevel(level: Int): Boolean {
        val safe = level.coerceIn(0, 5)
        val cmds = if (safe == 0) {
            "echo 0 > $FAN_LEVEL; echo 0 > $FAN_ENABLE"
        } else {
            "echo 1 > $FAN_ENABLE; echo $safe > $FAN_LEVEL"
        }
        return execHardwareWrite("fan_level:$safe", cmds)
    }

    fun setFanPwm(value: Int): Boolean {
        val safe = value.coerceIn(0, 255)
        val cmds = if (safe == 0) {
            "echo 0 > $FAN_PWM; echo 0 > $FAN_ENABLE"
        } else {
            "echo 1 > $FAN_ENABLE; echo $safe > $FAN_PWM"
        }
        return execHardwareWrite("fan_pwm:$safe", cmds)
    }

    fun readFanRpm(): Int? {
        return RootShell.execForOutput("cat $FAN_RPM")?.trim()?.toIntOrNull()
    }

    fun readFanLevel(): Int? {
        return RootShell.execForOutput("cat $FAN_LEVEL")?.trim()?.toIntOrNull()?.coerceIn(0, 5)
    }

    fun enablePump(enabled: Boolean): Boolean {
        return execHardwareWrite("pump_enable:$enabled", "echo ${if (enabled) 1 else 0} > $PUMP_ENABLE")
    }

    fun setPumpProfile(profile: String): Boolean {
        val cmd = when (profile.lowercase()) {
            "slow" -> "echo 1 > $PUMP_ENABLE; echo 4 > $PUMP_FREQ; echo 40 > $PUMP_SPEED"
            "medium" -> "echo 1 > $PUMP_ENABLE; echo 4 > $PUMP_FREQ; echo 60 > $PUMP_SPEED"
            "quick" -> "echo 1 > $PUMP_ENABLE; echo 4 > $PUMP_FREQ; echo 80 > $PUMP_SPEED"
            "experimental" -> "echo 1 > $PUMP_ENABLE; echo 4 > $PUMP_FREQ; echo 90 > $PUMP_SPEED"
            "off" -> "echo 0 > $PUMP_ENABLE"
            else -> "echo 1 > $PUMP_ENABLE; echo 4 > $PUMP_FREQ; echo 80 > $PUMP_SPEED"
        }
        return execHardwareWrite("led_effect:$cmd", cmd)
    }

    fun readPumpEnabled(): String? = RootShell.execForOutput("cat $PUMP_ENABLE")
    fun readPumpFreq(): String? = RootShell.execForOutput("cat $PUMP_FREQ")
    fun readPumpSpeed(): String? = RootShell.execForOutput("cat $PUMP_SPEED")

    // LED effect encoding on NX789J: OEM LightOldData protocol.
    // Kernel only accepts ≤3-digit hex values; rejects 8-digit zone-prefixed values.
    // Only the FAN zone (aw22xxx_led on a single matrix) is wired up here; on
    // RM 10 Pro the logo + shoulder strip aren't exposed via this chip.
    private const val FAN_LED_OFF_VALUE = "2"
    private val FAN_LED_COLOR_CODES = mapOf(
        // color index → (steady, breathe, flashing, flow, burst) hex strings
        1 to LedEffectSet("100", "30", "20", "40", "110"),  // red
        9 to LedEffectSet("101", "31", "21", "41", "111"),  // rose
        3 to LedEffectSet("107", "37", "27", "47", "117"),  // orange
        4 to LedEffectSet("102", "32", "22", "42", "112"),  // yellow
        5 to LedEffectSet("103", "33", "23", "43", "113"),  // green
        6 to LedEffectSet("105", "35", "25", "45", "115"),  // cyan
        7 to LedEffectSet("104", "34", "24", "44", "114"),  // blue
        8 to LedEffectSet("106", "36", "26", "46", "116")   // purple
    )

    private data class LedEffectSet(
        val steady: String,
        val breathe: String,
        val flashing: String,
        val flow: String,
        val burst: String
    ) {
        fun pick(effectName: String): String = when (effectName.lowercase()) {
            "breathe" -> breathe
            "flashing" -> flashing
            "flow" -> flow
            "burst" -> burst
            else -> steady
        }
    }

    private fun fanLedValueFor(effectName: String, color: Int): String {
        val set = FAN_LED_COLOR_CODES[color] ?: FAN_LED_COLOR_CODES.getValue(1)
        return set.pick(effectName)
    }

    fun setFanLedEnabled(enabled: Boolean): Boolean {
        return if (enabled) {
            val on = fanLedValueFor("steady", 5)
            execHardwareWrite("fan_led_enabled:true", "echo $on > $LED_EFFECT; echo 1 > $LED_CFG")
        } else {
            execHardwareWrite("fan_led_enabled:false", "echo $FAN_LED_OFF_VALUE > $LED_EFFECT; echo 1 > $LED_CFG")
        }
    }

    fun setFanLedStockPreset(effectValue: String): Boolean {
        return execHardwareWrite("fan_led_preset:$effectValue", "echo 1 > $FAN_ENABLE; echo $effectValue > $LED_EFFECT; echo 1 > $LED_CFG")
    }

    fun setFanLedEffect(effectName: String, color: Int): Boolean {
        val value = fanLedValueFor(effectName, color)
        return execHardwareWrite("fan_led:$effectName:$color", "echo 1 > $FAN_ENABLE; echo $value > $LED_EFFECT; echo 1 > $LED_CFG")
    }

    // Back logo + X emblem on RM 10 Pro live on the aw22xxx chip too, but at a
    // different value range than the fan matrix. Values 0x60..0x67 each load a
    // dedicated firmware preset (aw_cfg0_1..aw_cfg0_8) — observed sequence is
    // red / orange / yellow / green / cyan / blue / purple / rose.
    private const val BACK_LED_OFF_VALUE = "0"  // loads m_led_off.bin (all-zone blank)
    private fun backLedValueFor(color: Int): String = when (color) {
        1 -> "0x60"  // red
        3 -> "0x61"  // orange
        4 -> "0x62"  // yellow
        5 -> "0x63"  // green
        6 -> "0x64"  // cyan
        7 -> "0x65"  // blue
        8 -> "0x66"  // purple
        9 -> "0x67"  // rose
        else -> "0x63"
    }

    fun setLogoLedEnabled(enabled: Boolean): Boolean {
        return if (enabled) {
            execHardwareWrite("back_led_enabled:true", "echo ${backLedValueFor(5)} > $LED_EFFECT; echo 1 > $LED_CFG")
        } else {
            execHardwareWrite("back_led_enabled:false", "echo $BACK_LED_OFF_VALUE > $LED_EFFECT; echo 1 > $LED_CFG")
        }
    }

    fun setLogoLedEffect(effectName: String, color: Int): Boolean {
        // Back logo is solid-color only; effectName is ignored, the chip drives
        // its own breathing/idle behavior baked into the firmware preset.
        val value = backLedValueFor(color)
        return execHardwareWrite("back_led:$color", "echo $value > $LED_EFFECT; echo 1 > $LED_CFG")
    }

    fun setShoulderLedEnabled(enabled: Boolean): Boolean {
        // No dedicated shoulder LED strip on RM 10 Pro. No-op.
        return true
    }

    fun setShoulderLedEffect(effectName: String, color: Int): Boolean = true

    fun turnOffAllLeds(): Boolean {
        return execHardwareWrite("led_all_off", "echo $FAN_LED_OFF_VALUE > $LED_EFFECT; echo 1 > $LED_CFG")
    }

    fun enableTriggers(): Boolean {
        return execHardwareWrite("triggers_enabled:true", "echo 1 > $SAR0_MODE; echo 1 > $SAR1_MODE")
    }

    fun disableTriggers(): Boolean {
        return execHardwareWrite("triggers_enabled:false", "echo 0 > $SAR0_MODE; echo 0 > $SAR1_MODE")
    }

    fun isTriggersEnabled(): Boolean {
        // sar0/sar1 mode_operation reads back as: "mode : 1, REG_WST(0x1a14) :0x1000000"
        // Parse the first "mode : N" digit rather than the whole string.
        val pattern = Regex("""mode\s*:\s*(\d)""")
        val sar0 = RootShell.execForOutput("cat $SAR0_MODE")?.let { pattern.find(it)?.groupValues?.get(1) }
        val sar1 = RootShell.execForOutput("cat $SAR1_MODE")?.let { pattern.find(it)?.groupValues?.get(1) }
        return sar0 == "1" || sar1 == "1"
    }

    fun injectTap(x: Int, y: Int): Boolean {
        return RootShell.exec("input tap $x $y")
    }

    private fun setSliderStockFunction(value: Int): Boolean {
        val cmd = "settings put system fourth_physical_key_function_value $value; " +
            "settings put system physical_key_function_app_value cn.nubia.gamelauncher"
        return execHardwareWrite("slider_stock_function:$value", cmd)
    }

    fun setSliderOpenCamera(): Boolean = setSliderStockFunction(1)

    fun setSliderOpenGameSpace(): Boolean = setSliderStockFunction(2)

    fun setSliderSoundMode(): Boolean = setSliderStockFunction(3)

    fun setSliderFlashlight(): Boolean = setSliderStockFunction(4)

    fun setSliderVoiceRecorder(): Boolean = setSliderStockFunction(5)

    fun setSliderLaunchApp(pkg: String): Boolean {
        val cmd = "settings put system fourth_physical_key_function_value 16; " +
            "settings put system physical_key_function_app_value $pkg"
        return execHardwareWrite("slider_launch_app:$pkg", cmd)
    }

    fun disableSliderSystemHandling(): Boolean {
        return execHardwareWrite("slider_system_handling:false", "settings put system fourth_physical_key_function_value 0")
    }

    fun readSliderState(): String? {
        return RootShell.execForOutput("settings get global zte_keypad_slide_on_or_off")?.trim()
    }

    fun vibrate(durationMs: Int, gain: Int): Boolean {
        val d = durationMs.coerceIn(1, 5000)
        val g = gain.coerceIn(0, 255)
        val cmd = "echo $d > $HAPTIC_DURATION; echo $g > $HAPTIC_GAIN; echo 1 > $HAPTIC_ACTIVATE"
        return RootShell.exec(cmd)
    }

    fun readTemperatureC(): Float? {
        val candidates = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/class/thermal/thermal_zone3/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone2/temp",
            "/sys/devices/virtual/thermal/thermal_zone3/temp"
        )

        for (path in candidates) {
            val raw = RootShell.execForOutput("cat $path 2>/dev/null")?.trim()?.toFloatOrNull() ?: continue
            if (raw > 1000f && raw < 200000f) return raw / 1000f
            if (raw > 0f && raw < 200f) return raw
        }

        return null
    }

    fun readTemperatureF(): Float? {
        val c = readTemperatureC() ?: return null
        return (c * 9f / 5f) + 32f
    }

    fun chooseFanLevelForTempF(tempF: Float, curve: String): Int {
        return when (curve.toLowerCase()) {
            "quiet" -> when {
                tempF < 95f -> 0
                else -> 1
            }
            "turbo" -> when {
                tempF < 100f -> 4
                else -> 5
            }
            else -> when {
                tempF < 100f -> 2
                else -> 3
            }
        }
    }

    fun chooseAutoFanLevelForTempF(tempF: Float): Int {
        return when {
            tempF < 95f -> 0
            tempF < 104f -> 1
            tempF < 113f -> 2
            tempF < 122f -> 3
            tempF < 131f -> 4
            else -> 5
        }
    }

    fun applyFanCurve(curve: String): Int? {
        val tempF = readTemperatureF() ?: return null
        val level = chooseFanLevelForTempF(tempF, curve)
        setFanLevel(level)
        return level
    }

    fun applyAutoFanCurve(): Int? {
        val tempF = readTemperatureF() ?: return null
        val level = chooseAutoFanLevelForTempF(tempF)
        setFanLevel(level)
        return level
    }

    fun readCpuModel(): String {
        return "Snapdragon 8 Elite Gen 5"
    }

    fun readRamInfo(): String {
        val memInfo = RootShell.execForOutput("cat /proc/meminfo 2>/dev/null") ?: return "Unknown"
        val totalLine = memInfo.lines().firstOrNull { it.startsWith("MemTotal:") } ?: return "Unknown"
        val kb = totalLine.substringAfter("MemTotal:").trim().substringBefore(" ").toLongOrNull() ?: return "Unknown"

        val gb = kb / 1024.0 / 1024.0
        val rounded = round(gb).toInt()
        val supported = listOf(12, 16, 24)
        val nearest = supported.minByOrNull { abs(it - rounded) } ?: rounded

        return "$nearest GB"
    }

    fun readShortRomFingerprint(): String {
        val fp = RootShell.execForOutput("getprop ro.build.fingerprint")?.trim().orEmpty()
        if (fp.isNotBlank()) return fp

        val displayId = RootShell.execForOutput("getprop ro.build.display.id")?.trim().orEmpty()
        if (displayId.isNotBlank()) return displayId

        val incremental = RootShell.execForOutput("getprop ro.build.version.incremental")?.trim().orEmpty()
        if (incremental.isNotBlank()) return incremental

        return "Unknown"
    }


    fun applyHardwareProfile(profile: HardwareProfile): Boolean {
        enableFan(profile.fanEnabled)
        if (profile.fanEnabled) {
            setFanLevel(profile.fanLevel)
        }

        if (profile.pumpEnabled) {
            setPumpProfile(profile.pumpProfile)
        } else {
            enablePump(false)
        }

        if (profile.fanLedEnabled) {
            setFanLedEffect(profile.fanLedEffect, profile.fanLedColor)
        } else {
            setFanLedEnabled(false)
        }

        if (profile.logoLedEnabled) {
            setLogoLedEffect(profile.logoLedEffect, profile.logoLedColor)
        } else {
            setLogoLedEnabled(false)
        }

        if (profile.shoulderLedEnabled) {
            setShoulderLedEffect(profile.shoulderLedEffect, profile.shoulderLedColor)
        } else {
            setShoulderLedEnabled(false)
        }

        return true
    }

}
