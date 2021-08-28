package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.util.xmlb.annotations.OptionTag

open class RobotRunConfigurationOptions : RunConfigurationOptions() {

    @get:OptionTag("sdkHomePath")
    var sdkHomePath by string("")

    @get:OptionTag("suitePaths")
    var suitePaths by list<String>()

    @get:OptionTag("testNames")
    var testNames by list<String>()

    @get:OptionTag("suiteNames")
    var suiteNames by list<String>()

    @get:OptionTag("includeTags")
    var includeTags by list<String>()

    @get:OptionTag("excludeTags")
    var excludeTags by list<String>()

    @get:OptionTag("outputDirPath")
    var outputDirPath by string("")

    @get:OptionTag("outputFilePath")
    var outputFilePath by string("")

    @get:OptionTag("logFilePath")
    var logFilePath by string("")

    @get:OptionTag("logTitle")
    var logTitle by string("")

    @get:OptionTag("reportFilePath")
    var reportFilePath by string("")

    @get:OptionTag("reportTitle")
    var reportTitle by string("")

    @get:OptionTag("timestampOutputs")
    var timestampOutputs by property(false)

    @get:OptionTag("suffixWithConfigName")
    var suffixWithConfigName by property(false)

    @get:OptionTag("splitLog")
    var splitLog by property(false)

    @get:OptionTag("variables")
    var variables by linkedMap<String, String>()

    @get:OptionTag("logLevel")
    var logLevel by string("INFO")

    @get:OptionTag("defaultLogLevel")
    var defaultLogLevel by string("INFO")

    @get:OptionTag("dryRun")
    var dryRun by property(false)

    @get:OptionTag("runEmptySuite")
    var runEmptySuite by property(false)

    @get:OptionTag("extraArguments")
    var extraArguments by string("")

    @get:OptionTag("showOutputView")
    var showOutputView by property(false)

}