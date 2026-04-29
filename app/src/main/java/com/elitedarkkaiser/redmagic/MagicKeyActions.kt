package com.elitedarkkaiser.redmagic

object MagicKeyActions {
    fun readModeLabel(): String {
        val raw = RootShell.execForOutput("settings get system fourth_physical_key_function_value")?.trim().orEmpty()
        return when (raw) {
            "1" -> "Camera"
            "2" -> "GameSpace"
            "3" -> "Sound Mode"
            "4" -> "Flashlight"
            "5" -> "Voice Recorder"
            "16" -> "Launch App"
            "0" -> "Disabled"
            else -> "Unknown"
        }
    }
    fun resolveAppLabel(context: android.content.Context, pkg: String?): String {
        if (pkg.isNullOrBlank()) return "Choose App"
        return try {
            val appInfo = context.packageManager.getApplicationInfo(pkg, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Throwable) {
            pkg
        }
    }

}
