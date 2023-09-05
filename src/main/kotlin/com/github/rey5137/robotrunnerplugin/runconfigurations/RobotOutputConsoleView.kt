package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.RobotOutputView
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

class RobotOutputConsoleView(val project: Project,
                             val consoleView: ConsoleView,
                             val robotOutputView: RobotOutputView) : JBTabbedPane(), ConsoleView {

    init {
        add(MyBundle.message("robot.run.configuration.label.output-view"), robotOutputView)
        add(MyBundle.message("robot.run.configuration.label.console-view"), consoleView.component)

        consoleView.component.border = JBUI.Borders.empty(0, 0, 0, 0)
        robotOutputView.border = JBUI.Borders.empty(0, 0, 0, 0)
    }

    override fun dispose() {
        consoleView.dispose()
        robotOutputView.dispose()
    }

    override fun getComponent(): JComponent {
        return this
    }

    override fun getPreferredFocusableComponent(): JComponent {
        return consoleView.preferredFocusableComponent
    }

    override fun print(text: String, contentType: ConsoleViewContentType) {
        consoleView.print(text, contentType)
    }

    override fun clear() {
        consoleView.clear()
    }

    override fun scrollTo(offset: Int) {
        consoleView.scrollTo(offset)
    }

    override fun attachToProcess(processHandler: ProcessHandler) {
        consoleView.attachToProcess(processHandler)
    }

    override fun setOutputPaused(value: Boolean) {
        consoleView.isOutputPaused = value
    }

    override fun isOutputPaused(): Boolean {
        return consoleView.isOutputPaused
    }

    override fun hasDeferredOutput(): Boolean {
        return consoleView.hasDeferredOutput()
    }

    override fun performWhenNoDeferredOutput(runnable: Runnable) {
        consoleView.performWhenNoDeferredOutput(runnable)
    }

    override fun setHelpId(helpId: String) {
        consoleView.setHelpId(helpId)
    }

    override fun addMessageFilter(filter: Filter) {
        consoleView.addMessageFilter(filter)
    }

    override fun printHyperlink(hyperlinkText: String, info: HyperlinkInfo?) {
        consoleView.printHyperlink(hyperlinkText, info)
    }

    override fun getContentSize(): Int {
        return consoleView.contentSize
    }

    override fun canPause(): Boolean {
        return consoleView.canPause()
    }

    override fun createConsoleActions(): Array<AnAction> {
        return consoleView.createConsoleActions()
    }

    override fun allowHeavyFilters() {
        return consoleView.allowHeavyFilters()
    }
}