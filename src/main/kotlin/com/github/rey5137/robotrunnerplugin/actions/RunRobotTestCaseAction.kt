package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.github.rey5137.robotrunnerplugin.runconfigurations.escapeCharsInTestName
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.DataFlavor

class RunRobotTestCaseAction(private val runConfigurationSetting: RunnerAndConfigurationSettings?) :
    AnAction(runConfigurationSetting?.name ?: "", runConfigurationSetting?.name ?: "", runConfigurationSetting?.configuration?.icon) {

    constructor () : this(runConfigurationSetting = null)

    override fun actionPerformed(e: AnActionEvent) {
        if(runConfigurationSetting == null)
            return

        val dataContext = e.dataContext
        val provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext) ?: return
        provider.performCopy(dataContext)
        val string = CopyPasteManager.getInstance().getContents<String>(DataFlavor.stringFlavor) ?: return

        val file = e.file ?: return

        val runManager = RunManagerEx.getInstanceEx(e.project!!)
        val runConfiguration = runConfigurationSetting.configuration.clone() as RobotRunConfiguration
        runConfiguration.options.suitePaths = mutableListOf(file.path)
        runConfiguration.options.testNames = mutableListOf("*${string.escapeCharsInTestName()}*")
        runConfiguration.name = "[${runConfigurationSetting.configuration.name}] ${file.nameWithoutExtension} - \"$string\""
        val newRunConfigurationSetting = runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
        runManager.setTemporaryConfiguration(newRunConfigurationSetting)
        runManager.selectedConfiguration = newRunConfigurationSetting

        ProgramRunnerUtil.executeConfiguration(newRunConfigurationSetting, DefaultRunExecutor.getRunExecutorInstance())
    }
}