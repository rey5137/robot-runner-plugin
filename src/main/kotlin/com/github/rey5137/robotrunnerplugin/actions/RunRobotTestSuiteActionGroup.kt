package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.configurables.RobotRunProjectSettingsState
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.execution.RunManager
import com.intellij.openapi.actionSystem.*

class RunRobotTestSuiteActionGroup : ActionGroup() {

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return emptyArray()
        val configurationMap =
            RunManager.getInstance(project).getConfigurationSettingsList(RobotRunConfigurationType::class.java)
                .map { it.uniqueID to it }
                .toMap()
        return RobotRunProjectSettingsState.getInstance(project).settingMap.entries.filter { it.value.testSuiteEnable }
            .mapNotNull { configurationMap[it.key] }
            .map { RunRobotTestSuiteAction(it) as AnAction }
            .toMutableList()
            .apply {
                add(Separator(null))
                add(CreateRunRobotTestSuiteConfigAction())
            }
            .toTypedArray()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && e.files.isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

}