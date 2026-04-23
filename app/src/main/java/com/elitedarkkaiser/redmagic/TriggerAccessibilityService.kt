package com.elitedarkkaiser.redmagic

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class TriggerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        HardwareController.enableTriggers()
    }

    private fun prefs() = getSharedPreferences("triggers", Context.MODE_PRIVATE)

    private fun getAction(key: String): String {
        return prefs().getString(key, "NONE") ?: "NONE"
    }

    private fun performAction(action: String) {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (action) {
            "VOL_UP" -> audio.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )

            "VOL_DOWN" -> audio.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )

            "PLAY_PAUSE" -> audio.dispatchMediaKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            )

            "NONE" -> {}
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {

            KeyEvent.KEYCODE_F7 -> {
                performAction(getAction("left_trigger"))
                true
            }

            KeyEvent.KEYCODE_F8 -> {
                performAction(getAction("right_trigger"))
                true
            }

            else -> false
        }
    }
}
