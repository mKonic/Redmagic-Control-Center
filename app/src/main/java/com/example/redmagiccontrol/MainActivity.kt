package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var rpmText: TextView
    private lateinit var fanSeek: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply {
            text = "Checking root..."
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }

        rpmText = TextView(this).apply {
            text = "RPM: unknown"
            textSize = 16f
            setPadding(24, 8, 24, 16)
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

        val rootCheckBtn = button("Check root") {
            val ok = RootShell.hasRoot()
            status.text = if (ok) "Root available" else "Root not available"

            AlertDialog.Builder(this)
                .setTitle("Root Status")
                .setMessage(
                    if (ok) "Root access granted\n\nApp is running as root"
                    else "Root access NOT granted\n\nCheck your root manager"
                )
                .setPositiveButton("OK", null)
                .show()
        }

        val fanOnBtn = button("Fan on") {
            HardwareController.enableFan(true)
            refreshStatus()
        }

        val fanOffBtn = button("Fan off") {
            HardwareController.enableFan(false)
            refreshStatus()
        }

        val rpmBtn = button("Read RPM") {
            val rpm = HardwareController.readFanRpm()
            rpmText.text = "RPM: ${rpm ?: "unavailable"}"
        }

        val pumpOnBtn = button("Pump on") {
            HardwareController.enablePump(true)
            refreshStatus()
        }

        val pumpOffBtn = button("Pump off") {
            HardwareController.enablePump(false)
            refreshStatus()
        }

        val ledPurpleBtn = button("All LEDs purple breathing") {
            HardwareController.setAllLeds(mode = 3, color = 8)
        }

        val ledRedBtn = button("Logo red static") {
            HardwareController.setLed(zone = 1, mode = 2, color = 1)
        }

        val ledOffBtn = button("LEDs off") {
            HardwareController.turnOffAllLeds()
        }

        val trigEnableBtn = button("Enable triggers") {
            HardwareController.enableTriggers()
            refreshStatus()
        }

        val sliderAppBtn = button("Set slider to open this app") {
            HardwareController.setSliderLaunchApp(packageName)
            refreshStatus()
        }

        val sliderRawBtn = button("Disable slider system action") {
            HardwareController.disableSliderSystemHandling()
            refreshStatus()
        }

        val vibrateBtn = button("Test haptic") {
            HardwareController.vibrate(durationMs = 100, gain = 220)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)

            addView(status)
            addView(rpmText)
            addView(label("Fan level (0-5)"))
            addView(fanSeek)
            addView(rootCheckBtn)
            addView(fanOnBtn)
            addView(fanOffBtn)
            addView(rpmBtn)
            addView(pumpOnBtn)
            addView(pumpOffBtn)
            addView(ledPurpleBtn)
            addView(ledRedBtn)
            addView(ledOffBtn)
            addView(trigEnableBtn)
            addView(sliderAppBtn)
            addView(sliderRawBtn)
            addView(vibrateBtn)
        }

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)

        refreshStatus()
    }

    private fun refreshStatus() {
        status.text = buildString {
            appendLine(if (RootShell.hasRoot()) "Root available" else "Root not available")
            appendLine("Fan enabled: ${HardwareController.isFanEnabled()}")
            appendLine("Fan RPM: ${HardwareController.readFanRpm() ?: "n/a"}")
            appendLine("Slider state: ${HardwareController.readSliderState() ?: "n/a"}")
        }
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }

    private fun label(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.BLACK)
            gravity = Gravity.START
            setPadding(0, 12, 0, 6)
        }
    }
}
