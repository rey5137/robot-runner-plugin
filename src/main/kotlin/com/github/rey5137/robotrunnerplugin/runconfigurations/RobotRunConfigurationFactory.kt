package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class RobotRunConfigurationFactory(type: RobotRunConfigurationType) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        RobotRunConfiguration(project, this, "Template")

    override fun getName() = "Robot Run Configuration Factory"

    override fun getId() = "RobotRunConfigurationFactory"

}