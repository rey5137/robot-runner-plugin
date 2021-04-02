package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.runconfigurations.ROBOT_RUN_CONFIGURATION_TYPE_ID
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.execution.RunManagerEx
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreateRunRobotTestSuiteConfigAction :
    AnAction(
        MyBundle.message("robot.run.action.label.new-config"),
        MyBundle.message("robot.run.action.desc.new-config"),
        null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val files = e.files
        val runManager = RunManagerEx.getInstanceEx(project)
        val configurationType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.first { it.id == ROBOT_RUN_CONFIGURATION_TYPE_ID } as RobotRunConfigurationType
        val runConfiguration = RobotRunConfiguration(project, configurationType.configurationFactory, "${files.joinToString(separator = " & ", transform = { it.nameWithoutExtension })}")
        runConfiguration.options.suitePaths = files.map { it.path }.toMutableList()
        val newRunConfigurationSetting = runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
        runManager.setTemporaryConfiguration(newRunConfigurationSetting)
        runManager.selectedConfiguration = newRunConfigurationSetting

        EditConfigurationsDialog(project).show()
    }
}