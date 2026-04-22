package com.elitedarkkaiser.redmagic

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class TriggerAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        HardwareController.enableTriggers()
    }

    
    override fun onKeyEvent(event: android.view.KeyEvent): Boolean {

        if (event.action != android.view.KeyEvent.ACTION_DOWN) return false

        val audio = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager

        return when (event.keyCode) {

            android.view.KeyEvent.KEYCODE_F7 -> {
                audio.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_LOWER,
                    android.media.AudioManager.FLAG_SHOW_UI
                )
                true
            }

            android.view.KeyEvent.KEYCODE_F8 -> {
                audio.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_RAISE,
                    android.media.AudioManager.FLAG_SHOW_UI
                )
                true
            }

            else -> false
        }
    }

}
