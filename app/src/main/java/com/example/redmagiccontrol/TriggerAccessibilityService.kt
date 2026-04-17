package com.example.redmagiccontrol

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

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        return when (event.keyCode) {
            131 -> {
                HardwareController.injectTap(500, 1000)
                true
            }
            132 -> {
                HardwareController.injectTap(800, 1000)
                true
            }
            else -> false
        }
    }
}
