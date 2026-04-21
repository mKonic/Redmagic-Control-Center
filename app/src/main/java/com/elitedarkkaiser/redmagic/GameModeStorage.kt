package com.elitedarkkaiser.redmagic

import android.content.Context
import org.json.JSONObject

private const val GAME_PREFS_NAME = "redmagic_hw_controls_prefs"
private const val GAME_MODE_PACKAGES_KEY = "game_mode_packages"

fun saveProfileForPackageStorage(
    context: Context,
    pkg: String,
    fanEnabled: Boolean,
    fanLevel: Int,
    fanLedEnabled: Boolean,
    fanLedEffect: String,
    fanLedColor: Int
) {
    val prefs = context.getSharedPreferences(GAME_PREFS_NAME, Context.MODE_PRIVATE)

    val obj = JSONObject().apply {
        put("fanEnabled", fanEnabled)
        put("fanLevel", fanLevel)
        put("fanLedEnabled", fanLedEnabled)
        put("fanLedEffect", fanLedEffect)
        put("fanLedColor", fanLedColor)
    }

    prefs.edit().putString("game_profile_$pkg", obj.toString()).apply()
}

fun getSavedGamePackagesStorage(context: Context): MutableSet<String> {
    val prefs = context.getSharedPreferences(GAME_PREFS_NAME, Context.MODE_PRIVATE)
    return (prefs.getStringSet(GAME_MODE_PACKAGES_KEY, emptySet()) ?: emptySet()).toMutableSet()
}

fun setSavedGamePackagesStorage(context: Context, packages: Set<String>) {
    val prefs = context.getSharedPreferences(GAME_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putStringSet(GAME_MODE_PACKAGES_KEY, packages).apply()
}

fun getGameModeStatusTextStorage(context: Context): String {
    val prefs = context.getSharedPreferences(GAME_PREFS_NAME, Context.MODE_PRIVATE)
    val tracked = prefs.getStringSet(GAME_MODE_PACKAGES_KEY, emptySet()) ?: emptySet()

    return if (tracked.isEmpty()) {
        "Game Mode: No apps selected"
    } else {
        "Game Mode: ${tracked.size} apps tracked"
    }
}
