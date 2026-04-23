package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class TriggerRootService : Service() {

    private var running = true
    private val held = ConcurrentHashMap<String, AtomicBoolean>()
    private val repeatThreads = ConcurrentHashMap<String, Thread>()

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

    private fun hapticTap() {
        try {
            HardwareController.vibrate(durationMs = 20, gain = 180)
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "hapticTap failed: " + t)
        }
    }

    private fun hapticHoldStart() {
        try {
            HardwareController.vibrate(durationMs = 35, gain = 255)
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "hapticHoldStart failed: " + t)
        }
    }

    private fun performAction(action: String) {
        android.util.Log.d("TRIGGER", "performAction=" + action)

        when (action) {
            "VOL_UP" -> runRoot("input keyevent 24")
            "VOL_DOWN" -> runRoot("input keyevent 25")
            "PLAY_PAUSE" -> runRoot("input keyevent 85")
            "NEXT" -> runRoot("input keyevent 87")
            "PREVIOUS" -> runRoot("input keyevent 88")
            "REWIND" -> runRoot("input keyevent 89")
            "FAST_FORWARD" -> runRoot("input keyevent 90")
            "NONE" -> Unit
            else -> Unit
        }
    }

    private fun isRepeatable(action: String): Boolean {
        return when (action) {
            "VOL_UP", "VOL_DOWN", "REWIND", "FAST_FORWARD" -> true
            else -> false
        }
    }

    private fun startRepeater(prefKey: String) {
        stopRepeater(prefKey)

        val action = getAction(prefKey)
        if (!isRepeatable(action)) return

        val flag = AtomicBoolean(true)
        held[prefKey] = flag

        val thread = Thread {
            try {
                Thread.sleep(350)

                if (running && flag.get()) {
                    hapticHoldStart()
                }

                while (running && flag.get()) {
                    performAction(getAction(prefKey))
                    Thread.sleep(110)
                }
            } catch (_: InterruptedException) {
            } catch (t: Throwable) {
                android.util.Log.e("TRIGGER", "startRepeater failed for " + prefKey + ": " + t)
            }
        }

        repeatThreads[prefKey] = thread
        thread.start()
    }

    private fun stopRepeater(prefKey: String) {
        held[prefKey]?.set(false)
        held.remove(prefKey)

        repeatThreads[prefKey]?.interrupt()
        repeatThreads.remove(prefKey)
    }

    private fun handleDown(prefKey: String, device: String, line: String) {
        android.util.Log.d("TRIGGER", "DOWN device=" + device + " key=" + prefKey + " line=" + line)

        hapticTap()
        performAction(getAction(prefKey))
        startRepeater(prefKey)
    }

    private fun handleUp(prefKey: String, device: String, line: String) {
        android.util.Log.d("TRIGGER", "UP device=" + device + " key=" + prefKey + " line=" + line)
        stopRepeater(prefKey)
    }

    private fun startReader(device: String, prefKey: String) {
        Thread {
            try {
                android.util.Log.d("TRIGGER", "startReader device=" + device + " key=" + prefKey)
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l " + device))
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (running) {
                    val line = reader.readLine() ?: break

                    if (line.contains(" DOWN")) {
                        handleDown(prefKey, device, line)
                    } else if (line.contains(" UP")) {
                        handleUp(prefKey, device, line)
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("TRIGGER", "startReader failed for " + device + ": " + t)
            }
        }.start()
    }

    override fun onDestroy() {
        running = false
        stopRepeater("left_trigger")
        stopRepeater("right_trigger")
        android.util.Log.d("TRIGGER", "TriggerRootService onDestroy")
        super.onDestroy()
    }
}
