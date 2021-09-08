package com.github.rey5137.robotrunnerplugin.runconfigurations.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.openFile
import com.github.rey5137.robotrunnerplugin.runconfigurations.findOutputFilePath
import com.github.rey5137.robotrunnerplugin.runconfigurations.text
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class OpenOutputFileAction(
    private val project: Project,
    private val processHandler: ProcessHandler,
    private val console: ConsoleView
) :
    DumbAwareAction(MyBundle.message("robot.run.configuration.label.open-output"), null, AllIcons.Actions.Menu_open),
    AnAction.TransparentUpdate {
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = processHandler.isProcessTerminated
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (processHandler.isProcessTerminated) {
            val outputFilePath = console.text().findOutputFilePath()
            if (outputFilePath != null)
                project.openFile(outputFilePath)
        }
    }
}
