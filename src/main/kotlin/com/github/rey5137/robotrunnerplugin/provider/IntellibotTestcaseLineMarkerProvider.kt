package com.github.rey5137.robotrunnerplugin.provider

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.MyNotifier
import com.github.rey5137.robotrunnerplugin.actions.CreateRunRobotTestCaseConfigAction
import com.github.rey5137.robotrunnerplugin.actions.RunRobotTestCaseAction
import com.github.rey5137.robotrunnerplugin.actions.findLocalFile
import com.github.rey5137.robotrunnerplugin.configurables.RobotRunProjectSettingsState
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.execution.RunManager
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.ui.awt.RelativePoint
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.Icon

private const val KEYWORD_CLASS = ".KeywordDefinitionIdImpl";
private const val HEADING_CLASS = ".HeadingImpl";

class IntellibotTestcaseLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName(): String = "Run testcase"

    override fun getIcon(): Icon = AllIcons.Actions.Execute

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        return when {
            element.javaClass.name.endsWith(KEYWORD_CLASS) -> {
                val isTestCaseId = element.parents.firstOrNull { it.javaClass.name.endsWith(HEADING_CLASS) }
                    ?.firstChild
                    ?.text
                    ?.contains("Test Cases", ignoreCase = true)
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
            element.javaClass.name.endsWith(HEADING_CLASS) -> {
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
            val files = listOfNotNull(findLocalFile(elt.containingFile.virtualFile))
            val actions = RobotRunProjectSettingsState.getInstance(project).settingMap.entries
                .filter { it.value.testCaseEnable }
                .mapNotNull { configurationMap[it.key] }
                .map { RunRobotTestCaseAction(runConfigurationSetting = it, values = values, files = files) as AnAction }
                .toMutableList()
                .apply {
                    add(Separator(null))
                    add(CreateRunRobotTestCaseConfigAction(values = values, files = files))
                }
            val group = DefaultActionGroup(actions)
            val context = createDataContext(elt.project)
            val popup = JBPopupFactory.getInstance()
                .createActionGroupPopup(null, group, context, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
            popup.show(RelativePoint(e))
        }

        private fun createDataContext(project: Project): DataContext {
            val projectContext = SimpleDataContext.getProjectContext(project)
            if (PlatformDataKeys.CONTEXT_COMPONENT.getData(projectContext) == null) {
                val prjFocusManager = IdeFocusManager.getInstance(project)
                val prjComponent: Component? = prjFocusManager.focusOwner

                return if (prjComponent != null) {
                    wrapComponentIntoContext(prjComponent, projectContext)
                } else {
                    val projectFrame: Component? = WindowManager.getInstance().getFrame(project)
                    if (projectFrame != null) {
                        wrapComponentIntoContext(projectFrame, projectContext)
                    } else {
                        projectContext
                    }
                }
            } else {
                return projectContext
            }
        }

        private fun wrapComponentIntoContext(component: Component, parent: DataContext): DataContext {
            return SimpleDataContext.getSimpleContext(
                mapOf(
                    PlatformDataKeys.CONTEXT_COMPONENT.name to component
                ),
                parent
            )
        }

        private fun PsiElement.collectTestCases(values: MutableList<String>) {
            if (this.javaClass.name.endsWith(KEYWORD_CLASS))
                values.add(text)
            this.children.forEach { it.collectTestCases(values) }
        }

    }
}