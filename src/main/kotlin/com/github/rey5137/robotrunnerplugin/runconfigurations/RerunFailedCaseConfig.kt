package com.github.rey5137.robotrunnerplugin.runconfigurations

data class RerunFailedCaseConfig(
    val outputFile: String,
    val logFile: String,
    val reportFile: String,
    val rerunTime: Int,
    val testcases: List<String>,
    val suffix: String,
    val previousRerunConfig: RerunFailedCaseConfig? = null
)