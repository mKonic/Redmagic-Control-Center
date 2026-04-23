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

    
    
    private fun getTriggerAction(key: String): TriggerAction {
        val prefs = getSharedPreferences("rmc_prefs", MODE_PRIVATE)
        val raw = prefs.getString(key, TriggerAction.NONE.name) ?: TriggerAction.NONE.name
        return try {
            TriggerAction.valueOf(raw)
        } catch (e: Exception) {
            TriggerAction.NONE
        }
    }

    private fun performAction(action: TriggerAction) {
        val audio = getSystemService(AUDIO_SERVICE) as android.media.AudioManager

        when (action) {
            TriggerAction.VOLUME_UP -> {
                audio.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_RAISE,
                    android.media.AudioManager.FLAG_SHOW_UI
                )
            }
            TriggerAction.VOLUME_DOWN -> {
                audio.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_LOWER,
                    android.media.AudioManager.FLAG_SHOW_UI
                )
            }
            else -> {}
        }
    }

    override fun onKeyEvent(event: android.view.KeyEvent): Boolean {

        if (event.action != android.view.KeyEvent.ACTION_DOWN) return false

        val audio = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager

        
        val leftAction = getTriggerAction("trigger_left_action")
        val rightAction = getTriggerAction("trigger_right_action")

        return when (event.keyCode) {


            
            android.view.KeyEvent.KEYCODE_F7 -> {
                performAction(leftAction)

                audio.adjustStreamVolume(
                    android.media.AudioManager.STREAM_MUSIC,
                    android.media.AudioManager.ADJUST_LOWER,
                    android.media.AudioManager.FLAG_SHOW_UI
                )
                true
            }

            
            android.view.KeyEvent.KEYCODE_F8 -> {
                performAction(rightAction)

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
