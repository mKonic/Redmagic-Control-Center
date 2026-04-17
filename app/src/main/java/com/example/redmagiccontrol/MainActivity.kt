package com.example.redmagiccontrol

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var rpmText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply {
            text = "Checking root..."
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }

        rpmText = TextView(this).apply {
            text = "RPM: unknown"
            textSize = 16f
            setPadding(24, 8, 24, 16)
        }

        val rootCheckBtn = button("Check root") {
            val ok = RootShell.hasRoot()
            status.text = if (ok) "Root available" else "Root not available"

            AlertDialog.Builder(this)
                .setTitle("Root Status")
                .setMessage(
                    if (ok)
                        "Root access granted\n\nApp is running as root"
                    else
                        "Root access NOT granted\n\nCheck your root manager"
                )
                .setPositiveButton("OK", null)
                .show()
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)

            addView(status)
            addView(rpmText)
            addView(rootCheckBtn)
        }

        val scroll = ScrollView(this)
        scroll.addView(layout)

        setContentView(scroll)
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }
}
