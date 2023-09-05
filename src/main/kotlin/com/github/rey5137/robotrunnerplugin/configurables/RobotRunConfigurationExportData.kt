package com.github.rey5137.robotrunnerplugin.configurables

import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationOptions
import com.google.gson.annotations.SerializedName

data class RobotRunSettingExportData(

    @SerializedName("settings")
    val settings: List<RobotRunConfigurationExportData>

)

data class RobotRunConfigurationExportData(
    @SerializedName("name")
    var name: String = "",

    @SerializedName("test_suite_enable")
    val testSuiteEnable: Boolean = false,

    @SerializedName("test_case_enable")
    val testCaseEnable: Boolean = false,

    @SerializedName("sdk_home_path")
    var sdkHomePath: String = "",
    @SerializedName("suite_paths")
    var suitePaths: List<String> = emptyList(),
    @SerializedName("test_names")
    var testNames: List<String> = emptyList(),
    @SerializedName("suite_names")
    var suiteNames: List<String> = emptyList(),
    @SerializedName("include_tags")
    var includeTags: List<String> = emptyList(),
    @SerializedName("exclude_tags")
    var excludeTags: List<String> = emptyList(),
    @SerializedName("output_dir_path")
    var outputDirPath: String = "",
    @SerializedName("output_file_path")
    var outputFilePath: String = "",
    @SerializedName("log_file_path")
    var logFilePath: String = "",
    @SerializedName("log_title")
    var logTitle: String = "",
    @SerializedName("report_file_path")
    var reportFilePath: String = "",
    @SerializedName("report_title")
    var reportTitle: String = "",
    @SerializedName("timestamp_outputs")
    var timestampOutputs: Boolean = false,
    @SerializedName("suffix_with_config_name")
    var suffixWithConfigName: Boolean = false,
    @SerializedName("split_log")
    var splitLog: Boolean = false,
    @SerializedName("variables")
    var variables: Map<String, String> = emptyMap(),
    @SerializedName("log_level")
    var logLevel: String = "INFO",
    @SerializedName("default_log_level")
    var defaultLogLevel: String = "INFO",
    @SerializedName("dry_run")
    var dryRun: Boolean = false,
    @SerializedName("run_empty_suite")
    var runEmptySuite: Boolean = false,
    @SerializedName("extra_arguments")
    var extraArguments: String = "",
    @SerializedName("show_output_view")
    var showOutputView: Boolean = false,
    @SerializedName("use_pabot")
    var usePabot: Boolean = false,
    @SerializedName("pabot_arguments")
    var pabotArguments: String = "",
)

fun buildRobotRunConfigurationExportData(
    name: String,
    setting: RobotRunSetting,
    options: RobotRunConfigurationOptions
) =
    RobotRunConfigurationExportData(
        name = name,
        testSuiteEnable = setting.testSuiteEnable,
        testCaseEnable = setting.testCaseEnable,
        sdkHomePath = options.sdkHomePath ?: "",
        suitePaths = options.suitePaths,
        testNames = options.testNames,
        suiteNames = options.suiteNames,
        includeTags = options.includeTags,
        excludeTags = options.excludeTags,
        outputDirPath = options.outputDirPath ?: "",
        outputFilePath = options.outputFilePath ?: "",
        logFilePath = options.logFilePath ?: "",
        logTitle = options.logTitle ?: "",
        reportFilePath = options.reportFilePath ?: "",
        reportTitle = options.reportTitle ?: "",
        timestampOutputs = options.timestampOutputs,
        suffixWithConfigName = options.suffixWithConfigName,
        splitLog = options.splitLog,
        variables = options.variables,
        logLevel = options.logLevel ?: " INFO",
        defaultLogLevel = options.defaultLogLevel ?: "INFO",
        dryRun = options.dryRun,
        runEmptySuite = options.runEmptySuite,
        extraArguments = options.extraArguments ?: "",
        showOutputView = options.showOutputView,
        usePabot = options.usePabot,
        pabotArguments = options.pabotArguments ?: "",
    )

fun RobotRunConfigurationExportData.setRobotRunConfigurationOptions(options: RobotRunConfigurationOptions) {
    options.sdkHomePath = sdkHomePath
    options.suitePaths = suitePaths.toMutableList()
    options.testNames = testNames.toMutableList()
    options.suiteNames = suiteNames.toMutableList()
    options.includeTags = includeTags.toMutableList()
    options.excludeTags = excludeTags.toMutableList()
    options.outputDirPath = outputDirPath
    options.outputFilePath = outputFilePath
    options.logFilePath = logFilePath
    options.logTitle = logTitle
    options.reportFilePath = reportFilePath
    options.reportTitle = reportTitle
    options.timestampOutputs = timestampOutputs
    options.suffixWithConfigName = suffixWithConfigName
    options.splitLog = splitLog
    options.variables = variables.toMutableMap()
    options.logLevel = logLevel
    options.defaultLogLevel = defaultLogLevel
    options.dryRun = dryRun
    options.runEmptySuite = runEmptySuite
    options.extraArguments = extraArguments
    options.showOutputView = showOutputView
    options.usePabot = usePabot
    options.pabotArguments = pabotArguments
}