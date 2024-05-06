package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.MyNotifier
import com.github.rey5137.robotrunnerplugin.runconfigurations.ROBOT_RUN_CONFIGURATION_TYPE_ID
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.github.rey5137.robotrunnerplugin.runconfigurations.escapeCharsInTestName
import com.intellij.execution.RunManagerEx
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile

class CreateRunRobotTestCaseConfigAction(private val values: List<String> = emptyList(),
                                         private val files: List<VirtualFile>? = null) :
    AnAction(MyBundle.message("robot.run.action.label.new-config"),
        MyBundle.message("robot.run.action.desc.new-config"),
        null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val files = this.files ?: e.files
        if(files.isEmpty()) {
            MyNotifier.notify(e.project!!, MyBundle.message("robot.run.action.message.error-file-not-found"), NotificationType.ERROR)
            return
        }

        val file = files.first()

        val runManager = RunManagerEx.getInstanceEx(project)
        val configurationType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.first { it.id == ROBOT_RUN_CONFIGURATION_TYPE_ID } as RobotRunConfigurationType
        val runConfiguration = RobotRunConfiguration(project, configurationType.configurationFactory, "${file.nameWithoutExtension} - \"${values.first()}\"")
        runConfiguration.options.suitePaths = mutableListOf(file.path)
        runConfiguration.options.testNames = values.map { v -> "*${v.escapeCharsInTestName()}*" }.toMutableList()
        val newRunConfigurationSetting = runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
        runManager.setTemporaryConfiguration(newRunConfigurationSetting)
        runManager.selectedConfiguration = newRunConfigurationSetting

        EditConfigurationsDialog(project).show()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}