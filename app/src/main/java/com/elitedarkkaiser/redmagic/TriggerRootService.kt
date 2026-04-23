package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader

class TriggerRootService : Service() {

    private var running = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startReader("/dev/input/event2", "left_trigger")
        startReader("/dev/input/event5", "right_trigger")
    }

    private fun prefs() = getSharedPreferences("triggers", MODE_PRIVATE)

    private fun getAction(key: String): String {
        return prefs().getString(key, "NONE") ?: "NONE"
    }

    private fun performAction(action: String) {
        val audio = getSystemService(AUDIO_SERVICE) as AudioManager

        when (action) {
            "VOL_UP" -> audio.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            "VOL_DOWN" -> audio.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        }
    }

    private fun startReader(device: String, key: String) {
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l $device"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (running) {
                    val line = reader.readLine() ?: break

                    if (line.contains("DOWN")) {
                        performAction(getAction(key))
                    }
                }
            } catch (_: Exception) {}
        }.start()
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }
}
