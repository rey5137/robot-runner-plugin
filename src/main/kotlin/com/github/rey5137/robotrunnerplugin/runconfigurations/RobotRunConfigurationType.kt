package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons
import org.jetbrains.annotations.NonNls

@NonNls
const val ROBOT_RUN_CONFIGURATION_TYPE_ID = "robot-runner"

class RobotRunConfigurationType :
    ConfigurationTypeBase(ROBOT_RUN_CONFIGURATION_TYPE_ID, "Robot Runner", "Robot Runner Configuration", AllIcons.RunConfigurations.TestMark) {

    private val configurationFactory: RobotRunConfigurationFactory = RobotRunConfigurationFactory(this)

    init {
        addFactory(configurationFactory)
    }


}