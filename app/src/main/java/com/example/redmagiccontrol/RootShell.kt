package com.example.redmagiccontrol

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootShell {
    fun hasRoot(): Boolean = execForOutput("id")?.contains("uid=0") == true

    fun exec(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { os ->
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
            }
            process.waitFor() == 0
        } catch (_: Throwable) {
            false
        }
    }

    fun execForOutput(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { os ->
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
            }
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                buildString {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        appendLine(line)
                    }
                }.trim()
            }
            process.waitFor()
            output.ifBlank { null }
        } catch (_: Throwable) {
            null
        }
    }
}
