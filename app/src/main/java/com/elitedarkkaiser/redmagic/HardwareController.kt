package com.elitedarkkaiser.redmagic

import kotlin.math.abs
import kotlin.math.round

object HardwareController {

    private const val FAN_ENABLE = "/sys/kernel/fan/fan_enable"
    private const val FAN_LEVEL = "/sys/kernel/fan/fan_speed_level"
    private const val FAN_PWM = "/sys/kernel/fan/fan_speed_pwm"
    private const val FAN_RPM = "/sys/kernel/fan/fan_speed_count"

    private const val PUMP_ENABLE = "/proc/driver/micropump/enable"

    private const val LED_EFFECT = "/sys/class/leds/aw22xxx_led/effect"
    private const val LED_CFG = "/sys/class/leds/aw22xxx_led/cfg"

    private const val SAR0_MODE = "/sys/class/leds/sar0/mode_operation"
    private const val SAR1_MODE = "/sys/class/leds/sar1/mode_operation"

    private const val HAPTIC_DURATION = "/sys/class/leds/zte_vibrator/duration"
    private const val HAPTIC_GAIN = "/sys/class/leds/zte_vibrator/gain"
    private const val HAPTIC_ACTIVATE = "/sys/class/leds/zte_vibrator/activate"

    fun enableFan(enabled: Boolean): Boolean {
        return RootShell.exec("echo ${if (enabled) 1 else 0} > $FAN_ENABLE")
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
        return RootShell.exec(cmds)
    }

    fun setFanPwm(value: Int): Boolean {
        val safe = value.coerceIn(0, 255)
        val cmds = if (safe == 0) {
            "echo 0 > $FAN_PWM; echo 0 > $FAN_ENABLE"
        } else {
            "echo 1 > $FAN_ENABLE; echo $safe > $FAN_PWM"
        }
        return RootShell.exec(cmds)
    }

    fun readFanRpm(): Int? {
        return RootShell.execForOutput("cat $FAN_RPM")?.trim()?.toIntOrNull()
    }

    fun enablePump(enabled: Boolean): Boolean {
        return RootShell.exec("echo ${if (enabled) 1 else 0} > $PUMP_ENABLE")
    }

    fun setLed(zone: Int, mode: Int, color: Int): Boolean {
        val hex = "0x${zone}00${mode}00${color}"
        val cmd = "echo 1 > $FAN_ENABLE; echo $hex > $LED_EFFECT; echo 1 > $LED_CFG"
        return RootShell.exec(cmd)
    }

    fun setAllLeds(mode: Int, color: Int): Boolean {
        val cmd = buildString {
            append("echo 1 > $FAN_ENABLE; ")
            for (z in 1..3) {
                append("echo 0x${z}00${mode}00${color} > $LED_EFFECT; ")
                append("echo 1 > $LED_CFG; ")
            }
        }
        return RootShell.exec(cmd)
    }


    fun setFanLedEnabled(enabled: Boolean, mode: Int = 2, color: Int = 1): Boolean {
        return if (enabled) {
            setLed(zone = 1, mode = mode, color = color)
        } else {
            setLed(zone = 1, mode = 0, color = 0)
        }
    }

    fun setFanLedEffect(effectName: String, color: Int): Boolean {
        val mode = when (effectName.lowercase()) {
            "steady" -> 2
            "breathe" -> 3
            "flashing" -> 4
            "burst" -> 5
            "flow" -> 6
            else -> 2
        }
        return setLed(zone = 1, mode = mode, color = color)
    }

    fun turnOffAllLeds(): Boolean {
        val cmd = buildString {
            for (z in 1..3) {
                append("echo 0x${z}000000 > $LED_EFFECT; ")
                append("echo 1 > $LED_CFG; ")
            }
        }
        return RootShell.exec(cmd)
    }

    fun enableTriggers(): Boolean {
        return RootShell.exec("echo 1 > $SAR0_MODE; echo 1 > $SAR1_MODE")
    }

    fun disableTriggers(): Boolean {
        return RootShell.exec("echo 0 > $SAR0_MODE; echo 0 > $SAR1_MODE")
    }

    fun injectTap(x: Int, y: Int): Boolean {
        return RootShell.exec("input tap $x $y")
    }

    fun setSliderLaunchApp(pkg: String): Boolean {
        val cmd = "settings put system fourth_physical_key_function_value 16; " +
            "settings put system physical_key_function_app_value $pkg"
        return RootShell.exec(cmd)
    }

    fun disableSliderSystemHandling(): Boolean {
        return RootShell.exec("settings put system fourth_physical_key_function_value 0")
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
            tempF < 90f -> 0
            tempF < 97f -> 1
            tempF < 104f -> 2
            tempF < 111f -> 3
            tempF < 118f -> 4
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
}
