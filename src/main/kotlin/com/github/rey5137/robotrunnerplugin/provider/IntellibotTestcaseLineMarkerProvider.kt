package com.github.rey5137.robotrunnerplugin.provider

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.actions.CreateRunRobotTestCaseConfigAction
import com.github.rey5137.robotrunnerplugin.actions.RunRobotTestCaseAction
import com.github.rey5137.robotrunnerplugin.configurables.RobotRunProjectSettingsState
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.execution.RunManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.ui.awt.RelativePoint
import com.millennialmedia.intellibot.psi.element.HeadingImpl
import com.millennialmedia.intellibot.psi.element.KeywordDefinitionIdImpl
import java.awt.event.MouseEvent


class IntellibotTestcaseLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName(): String {
        return "Run testcase"
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        return when (element) {
            is KeywordDefinitionIdImpl -> {
                val isTestCaseId =
                    element.parentOfType<HeadingImpl>()?.firstChild?.text?.contains("Test Cases", ignoreCase = true)
                        ?: false
                return if (isTestCaseId)
                    LineMarkerInfo(
                        element,
                        element.textRange,
                        AllIcons.Actions.Execute,
                        { MyBundle.message("robot.run.gutter-icon.label.run-test-case") },
                        IntellibotTestcaseNavigationHandler(),
                        GutterIconRenderer.Alignment.CENTER
                    )
                else null
            }
            is HeadingImpl -> {
                return if (element.text.contains("Test Cases", ignoreCase = true))
                    LineMarkerInfo(
                        element,
                        element.textRange,
                        AllIcons.Actions.Execute,
                        { MyBundle.message("robot.run.gutter-icon.label.run-test-case-in-block") },
                        IntellibotTestcaseNavigationHandler(),
                        GutterIconRenderer.Alignment.CENTER
                    )
                else null
            }
            else -> null
        }
    }

    class IntellibotTestcaseNavigationHandler : GutterIconNavigationHandler<PsiElement> {

        override fun navigate(e: MouseEvent, elt: PsiElement) {
            val project = elt.project
            val configurationMap = RunManager.getInstance(project)
                .getConfigurationSettingsList(RobotRunConfigurationType::class.java)
                .associateBy { it.uniqueID }
            val values = mutableListOf<String>()
            elt.collectTestCases(values)
            val actions = RobotRunProjectSettingsState.getInstance(project).settingMap.entries
                .filter { it.value.testCaseEnable }
                .mapNotNull { configurationMap[it.key] }
                .map { RunRobotTestCaseAction(runConfigurationSetting = it, values = values) as AnAction }
                .toMutableList()
                .apply {
                    add(Separator(null))
                    add(CreateRunRobotTestCaseConfigAction(values = values))
                }
            val group = DefaultActionGroup(actions)
            val context = SimpleDataContext.getProjectContext(null)
            val popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(null, group, context, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
            popup.show(RelativePoint(e))
        }

        private fun PsiElement.collectTestCases(values: MutableList<String>) {
            if (this is KeywordDefinitionIdImpl)
                values.add(text)
            this.children.forEach { it.collectTestCases(values) }
        }

    }
}