package com.elitedarkkaiser.redmagic

import android.content.Context
import com.elitedarkkaiser.redmagic.storage.AppPrefs

private const val FIRST_INSTALL_PERMISSIONS_PROMPTED = "first_install_permissions_prompted"

fun isFirstInstallPermissionsPromptedStorage(context: Context): Boolean {
    return context
        .getSharedPreferences(AppPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(FIRST_INSTALL_PERMISSIONS_PROMPTED, false)
}

fun setFirstInstallPermissionsPromptedStorage(context: Context, prompted: Boolean) {
    context
        .getSharedPreferences(AppPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(FIRST_INSTALL_PERMISSIONS_PROMPTED, prompted)
        .apply()
}
