package com.github.rey5137.robotrunnerplugin.actions

import com.github.rey5137.robotrunnerplugin.configurables.RobotRunProjectSettingsState
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.execution.RunManager
import com.intellij.ide.CutProvider
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.EditorGutter
import com.intellij.openapi.project.DumbService

class RunRobotTestCaseActionGroup : ActionGroup() {

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val project = e?.project ?: return emptyArray()
        val configurationMap =
            RunManager.getInstance(project).getConfigurationSettingsList(RobotRunConfigurationType::class.java)
                .map { it.uniqueID to it }
                .toMap()
        return RobotRunProjectSettingsState.getInstance(project).settingMap.entries.filter { it.value.testCaseEnable }
            .mapNotNull { configurationMap[it.key] }
            .map { RunRobotTestCaseAction(it) }
            .toTypedArray()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null
            || !project.isOpen
            || EditorGutter.KEY.getData(e.dataContext) != null
            || java.lang.Boolean.TRUE == e.dataContext.getData(CommonDataKeys.EDITOR_VIRTUAL_SPACE)
        ){
            e.presentation.isEnabledAndVisible = false
        }
        else {
            val provider = getAvailableCutProvider(e)
            e.presentation.isEnabled = provider != null && provider.isCutEnabled(e.dataContext)
            e.presentation.isVisible = provider != null && provider.isCutVisible(e.dataContext)
        }
    }

    private fun getAvailableCutProvider(e: AnActionEvent): CutProvider? {
        val provider = e.getData(PlatformDataKeys.CUT_PROVIDER)
        val project = e.project
        return if (project != null && DumbService.isDumb(project) && !DumbService.isDumbAware(provider)) {
            null
        } else provider
    }

}