package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

internal object ProfileDialogs {

    fun showDeleteProfileDialog(
        context: Context,
        profileName: String,
        onConfirmDelete: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Delete Profile")
            .setMessage("Delete $profileName?")
            .setPositiveButton("Delete") { _, _ ->
                onConfirmDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun saveProfile(
        context: Context,
        profileName: String,
        profile: HardwareProfile,
        onSaved: () -> Unit
    ) {
        ProfileManager.upsertProfile(context, profile)
        Toast.makeText(context, "Saved $profileName", Toast.LENGTH_SHORT).show()
        onSaved()
    }
}
