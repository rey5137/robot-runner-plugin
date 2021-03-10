package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons

class RobotRunConfigurationType :
    ConfigurationTypeBase("robot-runner", "Robot Runner", "Robot Runner Configuration", AllIcons.RunConfigurations.TestMark) {

    private val configurationFactory: RobotRunConfigurationFactory = RobotRunConfigurationFactory(this)

    init {
        addFactory(configurationFactory)
    }


}