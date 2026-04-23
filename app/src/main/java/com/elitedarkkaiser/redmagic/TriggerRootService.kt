package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader

class TriggerRootService : Service() {

    private var running = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l"))

                val reader = BufferedReader(InputStreamReader(process.inputStream))

                val prefs = getSharedPreferences("triggers", MODE_PRIVATE)

                while (running) {
                    val line = reader.readLine() ?: continue

                    if (line.contains("KEY_F7") && line.contains("DOWN")) {
                        performAction(prefs.getString("left_trigger", "NONE") ?: "NONE")
                    }

                    if (line.contains("KEY_F8") && line.contains("DOWN")) {
                        performAction(prefs.getString("right_trigger", "NONE") ?: "NONE")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        return START_STICKY
    }

    private fun performAction(action: String) {
        when (action) {
            "VOL_UP" -> Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 24"))
            "VOL_DOWN" -> Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 25"))
        }
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
