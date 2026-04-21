package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.pm.PackageManager

fun showGamePickerDialogUI(
    context: Context,
    onSave: (Set<String>) -> Unit
) {
    val pm = context.packageManager

    val apps = pm.getInstalledApplications(0)
        .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
        .sortedBy { it.loadLabel(pm).toString().lowercase() }

    val selected = getSavedGamePackagesStorage(context)

    val listView = ListView(context)

    val adapter = object : BaseAdapter() {
        override fun getCount() = apps.size
        override fun getItem(position: Int) = apps[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val app = apps[position]

            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(24, 18, 24, 18)
            }

            val check = CheckBox(context).apply {
                isChecked = selected.contains(app.packageName)
            }

            val icon = ImageView(context).apply {
                val size = 100
                layoutParams = LinearLayout.LayoutParams(size, size)
                setImageDrawable(app.loadIcon(pm))
            }

            val textWrap = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 0, 0, 0)
            }

            val label = TextView(context).apply {
                text = app.loadLabel(pm)
                textSize = 15f
            }

            val pkg = TextView(context).apply {
                text = app.packageName
                textSize = 11f
            }

            textWrap.addView(label)
            textWrap.addView(pkg)

            row.addView(check)
            row.addView(icon)
            row.addView(textWrap)

            row.setOnClickListener {
                if (selected.contains(app.packageName)) {
                    selected.remove(app.packageName)
                    check.isChecked = false
                } else {
                    selected.add(app.packageName)
                    check.isChecked = true
                }
            }

            return row
        }
    }

    listView.adapter = adapter

    val title = TextView(context).apply {
        text = "Choose Games / Apps"
        textSize = 18f
        setPadding(24, 24, 24, 12)
    }

    var dialogRef: AlertDialog? = null

    val saveBtn = Button(context).apply {
        text = "Save"
        setOnClickListener {
            setSavedGamePackagesStorage(context, selected)
            onSave(selected)
            Toast.makeText(context, "Saved ${selected.size} apps", Toast.LENGTH_SHORT).show()
            dialogRef?.dismiss()
        }
    }

    val cancelBtn = Button(context).apply {
        text = "Cancel"
        setOnClickListener { dialogRef?.dismiss() }
    }

    val buttonRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.END
        setPadding(16, 12, 16, 16)
        addView(cancelBtn)
        addView(saveBtn)
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(12, 12, 12, 12)
        addView(title)
        addView(listView)
        addView(buttonRow)
    }

    val dialog = AlertDialog.Builder(context)
        .setView(container)
        .create()

    dialogRef = dialog
    dialog.show()
}
