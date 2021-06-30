package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementAdapter
import com.intellij.refactoring.listeners.RefactoringElementListener

@State(
    name = "RobotRunConfiguration",
    storages = [Storage("robot-runner-plugin__run-configuration.xml")]
)
class RobotRunConfiguration(
    project: Project,
    factory: RobotRunConfigurationFactory,
    name: String
) : RunConfigurationBase<RobotRunConfigurationOptions>(project, factory, name), RefactoringListenerProvider {

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        RobotRunTaskState(project, name, options, environment)

    override fun getOptionsClass(): Class<out RunConfigurationOptions> =
        RobotRunConfigurationOptions::class.java

    public override fun getOptions(): RobotRunConfigurationOptions =
        super.getOptions() as RobotRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RobotSettingsEditor()

    override fun checkConfiguration() {
        if(options.sdkHomePath == null)
            throw RuntimeConfigurationException(MyBundle.message("robot.run.configuration.message.interpreter-missing"))
    }

    override fun getRefactoringElementListener(element: PsiElement): RefactoringElementListener? {
        val index = options.suitePaths.indexOf(element.getPath() ?: "")
        if(index < 0)
            return null

        return object : RefactoringElementAdapter() {
            override fun elementRenamedOrMoved(newElement: PsiElement) {
                newElement.getPath()?.let {
                    options.suitePaths.set(index, it)
                }
            }

            override fun undoElementMovedOrRenamed(newElement: PsiElement, oldQualifiedName: String) {
                elementRenamedOrMoved(newElement)
            }
        }
    }

    private fun PsiElement.getPath() = this.containingFile?.virtualFile?.path

}