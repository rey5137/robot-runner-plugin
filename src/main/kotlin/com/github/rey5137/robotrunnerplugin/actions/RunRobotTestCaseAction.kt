package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.MyNotifier
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.github.rey5137.robotrunnerplugin.runconfigurations.escapeCharsInTestName
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VirtualFile

class RunRobotTestCaseAction(
    private val runConfigurationSetting: RunnerAndConfigurationSettings?,
    private val values: List<String> = emptyList(),
    private val files: List<VirtualFile>? = null,
) :
    AnAction(
        runConfigurationSetting?.name ?: "",
        runConfigurationSetting?.name ?: "",
        runConfigurationSetting?.configuration?.icon
    ) {

    constructor () : this(runConfigurationSetting = null)

    override fun actionPerformed(e: AnActionEvent) {
        if (runConfigurationSetting == null || values.isEmpty())
            return

        val files = this.files ?: e.files
        if(files.isEmpty()) {
            MyNotifier.notify(e.project!!, MyBundle.message("robot.run.action.message.error-file-not-found"), NotificationType.ERROR)
            return
        }

        val file = files.first()

        val runManager = RunManagerEx.getInstanceEx(e.project!!)
        val runConfiguration = runConfigurationSetting.configuration.clone() as RobotRunConfiguration
        runConfiguration.options.suitePaths = mutableListOf(file.path)
        runConfiguration.options.testNames = values.map { v -> "*${v.escapeCharsInTestName()}*" }.toMutableList()
        runConfiguration.name =
            "[${runConfigurationSetting.configuration.name}] ${file.nameWithoutExtension} - \"${values.first()}\""
        val newRunConfigurationSetting = runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
        runManager.setTemporaryConfiguration(newRunConfigurationSetting)
        runManager.selectedConfiguration = newRunConfigurationSetting

        ProgramRunnerUtil.executeConfiguration(newRunConfigurationSetting, DefaultRunExecutor.getRunExecutorInstance())
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}