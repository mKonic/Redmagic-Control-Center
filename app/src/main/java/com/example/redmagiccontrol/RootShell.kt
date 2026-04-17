package com.example.redmagiccontrol

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootShell {

    fun hasRoot(): Boolean {
        val output = execForOutput("id")
        return output?.contains("uid=0") == true
    }

    fun exec(command: String): Boolean {
        return execInteractive(command) || execSuC(command)
    }

    fun execForOutput(command: String): String? {
        return execForOutputInteractive(command) ?: execForOutputSuC(command)
    }

    private fun execInteractive(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun execSuC(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun execForOutputInteractive(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()

            process.waitFor()

            val result = buildString {
                if (stdout.isNotBlank()) append(stdout)
                if (stderr.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(stderr)
                }
            }.trim()

            result.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun execForOutputSuC(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()

            process.waitFor()

            val result = buildString {
                if (stdout.isNotBlank()) append(stdout)
                if (stderr.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(stderr)
                }
            }.trim()

            result.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}
