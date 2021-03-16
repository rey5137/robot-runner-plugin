package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
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

}