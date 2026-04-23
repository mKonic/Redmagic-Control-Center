package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader

class TriggerRootService : Service() {

    private var running = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("TRIGGER", "TriggerRootService onCreate")
        startReader("/dev/input/event2", "left_trigger")
        startReader("/dev/input/event5", "right_trigger")
    }

    private fun prefs() = getSharedPreferences("triggers", MODE_PRIVATE)

    private fun getAction(key: String): String {
        val value = prefs().getString(key, "NONE") ?: "NONE"
        android.util.Log.d("TRIGGER", "getAction(" + key + ")=" + value)
        return value
    }

    private fun runRoot(cmd: String) {
        try {
            android.util.Log.d("TRIGGER", "runRoot=" + cmd)
            Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "runRoot failed: " + t)
        }
    }

    private fun performAction(action: String) {
        android.util.Log.d("TRIGGER", "performAction=" + action)

        when (action) {
            "VOL_UP" -> runRoot("cmd media_session volume --stream 3 --adj raise")
            "VOL_DOWN" -> runRoot("cmd media_session volume --stream 3 --adj lower")
            "PLAY_PAUSE" -> runRoot("input keyevent 85")
            "NONE" -> Unit
            else -> Unit
        }
    }

    private fun startReader(device: String, key: String) {
        Thread {
            try {
                android.util.Log.d("TRIGGER", "startReader device=" + device + " key=" + key)
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l " + device))
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (running) {
                    val line = reader.readLine() ?: break

                    if (line.contains("DOWN")) {
                        android.util.Log.d("TRIGGER", "root event device=" + device + " key=" + key + " line=" + line)
                        performAction(getAction(key))
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("TRIGGER", "startReader failed for " + device + ": " + t)
            }
        }.start()
    }

    override fun onDestroy() {
        running = false
        android.util.Log.d("TRIGGER", "TriggerRootService onDestroy")
        super.onDestroy()
    }
}
