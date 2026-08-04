package com.modark.reset

import java.io.BufferedReader
import java.io.InputStreamReader

object RootExecutor {
    
    fun hasRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }
    
    fun execute(command: String): Pair<Int, String> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val exitCode = process.waitFor()
            val output = BufferedReader(InputStreamReader(process.inputStream)).readText()
            Pair(exitCode, output)
        } catch (e: Exception) {
            Pair(-1, "Error: ${e.message}")
        }
    }
}
