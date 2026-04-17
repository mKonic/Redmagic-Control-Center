package com.example.redmagiccontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.redmagiccontrol.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi()
        refreshStatus()
    }

    private fun setupUi() = with(binding) {
        btnCheckRoot.setOnClickListener { refreshStatus() }
        btnFanOn.setOnClickListener { HardwareController.enableFan(true); refreshStatus() }
        btnFanOff.setOnClickListener { HardwareController.enableFan(false); refreshStatus() }
        btnReadRpm.setOnClickListener { refreshStatus() }
        btnPumpOn.setOnClickListener { HardwareController.enablePump(true); refreshStatus() }
        btnPumpOff.setOnClickListener { HardwareController.enablePump(false); refreshStatus() }
        btnEnableTriggers.setOnClickListener { HardwareController.enableTriggers(); refreshStatus() }
        btnDisableTriggers.setOnClickListener { HardwareController.disableTriggers(); refreshStatus() }
        btnSliderApp.setOnClickListener { HardwareController.setSliderLaunchApp(packageName); refreshStatus() }
        btnSliderRaw.setOnClickListener { HardwareController.disableSliderSystemHandling(); refreshStatus() }
        btnLedPurple.setOnClickListener { HardwareController.setAllLeds(mode = 3, color = 8) }
        btnLedRed.setOnClickListener { HardwareController.setLed(zone = 1, mode = 2, color = 1) }
        btnLedOff.setOnClickListener { HardwareController.turnOffAllLeds() }
        btnVibrate.setOnClickListener { HardwareController.vibrate(durationMs = 100, gain = 220) }

        seekFanLevel.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                HardwareController.setFanLevel(value.toInt())
                refreshStatus()
            }
        }
    }

    private fun refreshStatus() = with(binding) {
        val root = if (RootShell.hasRoot()) "Root available" else "Root not available"
        val fanEnabled = HardwareController.isFanEnabled()
        val rpm = HardwareController.readFanRpm()?.toString() ?: "n/a"
        val slider = HardwareController.readSliderState() ?: "n/a"
        val triggerState = if (HardwareController.areTriggersEnabled()) "enabled" else "disabled"

        tvStatus.text = buildString {
            appendLine(root)
            appendLine("Fan enabled: $fanEnabled")
            appendLine("Fan RPM: $rpm")
            appendLine("Triggers: $triggerState")
            appendLine("Slider state: $slider")
        }
        tvFanLevel.text = getString(R.string.fan_level_value, seekFanLevel.value.toInt())
    }
}
