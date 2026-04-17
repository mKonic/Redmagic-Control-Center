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

    fun enableFan(enabled: Boolean): Boolean =
        RootShell.exec("echo ${if (enabled) 1 else 0} > $FAN_ENABLE")

    fun isFanEnabled(): Boolean =
        RootShell.execForOutput("cat $FAN_ENABLE")?.trim() == "1"

    fun setFanLevel(level: Int): Boolean {
        val safe = level.coerceIn(0, 5)
        val cmd = if (safe == 0) {
            "echo 0 > $FAN_LEVEL; echo 0 > $FAN_ENABLE"
        } else {
            "echo 1 > $FAN_ENABLE; echo $safe > $FAN_LEVEL"
        }
        return RootShell.exec(cmd)
    }

    fun setFanPwm(value: Int): Boolean {
        val safe = value.coerceIn(0, 255)
        val cmd = if (safe == 0) {
            "echo 0 > $FAN_PWM; echo 0 > $FAN_ENABLE"
        } else {
            "echo 1 > $FAN_ENABLE; echo $safe > $FAN_PWM"
        }
        return RootShell.exec(cmd)
    }

    fun readFanRpm(): Int? = RootShell.execForOutput("cat $FAN_RPM")?.trim()?.toIntOrNull()

    fun enablePump(enabled: Boolean): Boolean =
        RootShell.exec("echo ${if (enabled) 1 else 0} > $PUMP_ENABLE")

    fun setLed(zone: Int, mode: Int, color: Int): Boolean {
        val hex = "0x${zone}00${mode}00${color}"
        return RootShell.exec("echo $hex > $LED_EFFECT; echo 1 > $LED_CFG")
    }

    fun setAllLeds(mode: Int, color: Int): Boolean {
        val cmd = buildString {
            for (zone in 1..3) {
                append("echo 0x${zone}00${mode}00${color} > $LED_EFFECT; echo 1 > $LED_CFG; ")
            }
        }
        return RootShell.exec(cmd)
    }

    fun turnOffAllLeds(): Boolean {
        val cmd = buildString {
            for (zone in 1..3) {
                append("echo 0x${zone}000000 > $LED_EFFECT; echo 1 > $LED_CFG; ")
            }
        }
        return RootShell.exec(cmd)
    }

    fun enableTriggers(): Boolean = RootShell.exec("echo 1 > $SAR0_MODE; echo 1 > $SAR1_MODE")

    fun disableTriggers(): Boolean = RootShell.exec("echo 0 > $SAR0_MODE; echo 0 > $SAR1_MODE")

    fun areTriggersEnabled(): Boolean =
        RootShell.execForOutput("cat $SAR0_MODE")?.trim() == "1" &&
            RootShell.execForOutput("cat $SAR1_MODE")?.trim() == "1"

    fun injectTap(x: Int, y: Int): Boolean = RootShell.exec("input tap $x $y")

    fun setSliderLaunchApp(packageName: String): Boolean {
        val cmd = "settings put system fourth_physical_key_function_value 16; " +
            "settings put system physical_key_function_app_value $packageName"
        return RootShell.exec(cmd)
    }

    fun disableSliderSystemHandling(): Boolean =
        RootShell.exec("settings put system fourth_physical_key_function_value 0")

    fun readSliderState(): String? =
        RootShell.execForOutput("settings get global zte_keypad_slide_on_or_off")?.trim()

    fun vibrate(durationMs: Int, gain: Int): Boolean {
        val d = durationMs.coerceIn(1, 5000)
        val g = gain.coerceIn(0, 255)
        val cmd = "echo $d > $HAPTIC_DURATION; echo $g > $HAPTIC_GAIN; echo 1 > $HAPTIC_ACTIVATE"
        return RootShell.exec(cmd)
    }
}
