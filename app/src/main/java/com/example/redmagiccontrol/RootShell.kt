package com.example.redmagiccontrol

import java.io.BufferedReader
import java.io.InputStreamReader

object RootShell {

    fun hasRoot(): Boolean {
        return execForOutput("id")?.contains("uid=0") == true
    }

    fun exec(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun execForOutput(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()

            process.waitFor()
            output.trim().ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}
