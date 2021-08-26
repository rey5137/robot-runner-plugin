package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.ui.components.JBTabbedPane
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel

class RobotOutputConsoleView(val consoleView: ConsoleView) : JBTabbedPane(), ConsoleView {

    init {
        add("Console view", consoleView.component)
        add("Output view", JPanel().apply {
            background = Color.RED
        })
    }

    override fun dispose() {
        consoleView.dispose()
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

    override fun attachToProcess(processHandler: ProcessHandler?) {
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