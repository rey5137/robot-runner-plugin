package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import icons.MyIcons
import com.intellij.execution.configurations.ConfigurationTypeBase
import org.jetbrains.annotations.NonNls

@NonNls
const val ROBOT_RUN_CONFIGURATION_TYPE_ID = "robot-runner"

class RobotRunConfigurationType :
    ConfigurationTypeBase(ROBOT_RUN_CONFIGURATION_TYPE_ID,
        MyBundle.message("robot.run.configuration.type"),
        MyBundle.message("robot.run.configuration.type.desc"),
        MyIcons.RunRobot) {

    val configurationFactory: RobotRunConfigurationFactory = RobotRunConfigurationFactory(this)

    init {
        addFactory(configurationFactory)
    }


}