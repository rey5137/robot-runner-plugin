package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import java.io.IOException

import java.net.ServerSocket




fun String.findOutputFilePath(): String? {
    val regex = "^.*Output:\\s+([^\\n]*).*$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null
    else
        result.groupValues[1]
}

fun String.findLogFilePath(): String? {
    val regex = "^.*Log:\\s+([^\\n]*).*$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null
    else
        result.groupValues[1]
}

fun String.findReportFilePath(): String? {
    val regex = "^.*Report:\\s+([^\\n]*).*$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null
    else
        result.groupValues[1]
}

fun String.findNumberOfFailedCases(): Int? {
    val regex = "^.*\\d+ tests? total, \\d+ passed, (\\d+) failed.*$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null
    else
        result.groupValues[1].toIntOrNull()
}

fun String.escapeCharsInTestName(): String {
    val builder = StringBuilder()
    forEach { c ->
        when(c) {
            '*', '?', '[', ']' -> builder.append('[').append(c).append(']')
            else -> builder.append(c)
        }
    }
    return builder.toString()
}

fun findAvailablePort() : Int {
    try {
        ServerSocket(0).use { serverSocket ->
            return serverSocket.localPort
        }
    } catch (e: IOException) {
        return -1
    }
}

fun ConsoleView.text() = if(this is RobotOutputConsoleView)
    (this.consoleView as ConsoleViewImpl).text
else
    (this as ConsoleViewImpl).text