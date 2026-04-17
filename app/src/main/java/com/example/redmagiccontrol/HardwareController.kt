package com.example.redmagiccontrol

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
        val cmd = """
            for f in /sys/class/thermal/thermal_zone*/temp /sys/devices/virtual/thermal/thermal_zone*/temp; do
              if [ -f "\$f" ]; then
                v=$(cat "\$f" 2>/dev/null)
                case "\$v" in
                  ''|*[!0-9-]*) continue;;
                esac
                if [ "\$v" -gt 1000 ] && [ "\$v" -lt 200000 ]; then
                  echo "\$v"
                  break
                elif [ "\$v" -gt 0 ] && [ "\$v" -lt 200 ]; then
                  echo \$((v * 1000))
                  break
                fi
              fi
            done
        """.trimIndent().replace("\n", "; ")
        val raw = RootShell.execForOutput(cmd)?.trim()?.toFloatOrNull() ?: return null
        return raw / 1000f
    }

    fun readTemperatureF(): Float? {
        val c = readTemperatureC() ?: return null
        return (c * 9f / 5f) + 32f
    }

    fun chooseFanLevelForTempF(tempF: Float, curve: String): Int {
        return when (curve.toLowerCase()) {
            "quiet" -> when {
                tempF < 90f -> 1
                tempF < 100f -> 2
                tempF < 110f -> 3
                tempF < 118f -> 4
                else -> 5
            }
            "turbo" -> when {
                tempF < 86f -> 2
                tempF < 95f -> 3
                tempF < 104f -> 4
                else -> 5
            }
            else -> when {
                tempF < 88f -> 1
                tempF < 97f -> 2
                tempF < 106f -> 3
                tempF < 115f -> 4
                else -> 5
            }
        }
    }

    fun applyFanCurve(curve: String): Int? {
        val tempF = readTemperatureF() ?: return null
        val level = chooseFanLevelForTempF(tempF, curve)
        setFanLevel(level)
        return level
    }
}
