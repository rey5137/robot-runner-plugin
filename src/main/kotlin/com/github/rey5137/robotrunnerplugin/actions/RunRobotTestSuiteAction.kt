package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RunRobotTestSuiteAction(private val runConfigurationSetting: RunnerAndConfigurationSettings?) :
    AnAction(runConfigurationSetting?.name ?: "", runConfigurationSetting?.name ?: "", runConfigurationSetting?.configuration?.icon) {

    constructor () : this(runConfigurationSetting = null)

    override fun actionPerformed(e: AnActionEvent) {
        if(runConfigurationSetting == null)
            return

        val files = e.files
        val runManager = RunManagerEx.getInstanceEx(e.project!!)
        val runConfiguration = runConfigurationSetting.configuration.clone() as RobotRunConfiguration
        runConfiguration.options.suitePaths = files.map { it.path }.toMutableList()
        runConfiguration.name = "[${runConfigurationSetting.configuration.name}] ${files.joinToString(separator = " & ", transform = { it.nameWithoutExtension })}"
        val newRunConfigurationSetting = runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
        runManager.setTemporaryConfiguration(newRunConfigurationSetting)
        runManager.selectedConfiguration = newRunConfigurationSetting

        ProgramRunnerUtil.executeConfiguration(newRunConfigurationSetting, DefaultRunExecutor.getRunExecutorInstance())
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}