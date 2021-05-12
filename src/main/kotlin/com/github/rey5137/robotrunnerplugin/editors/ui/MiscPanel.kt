package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Insets
import javax.swing.JPanel

class MiscPanel : JPanel(BorderLayout()) {

    private val miscDetail = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        margin = Insets(5, 5, 5, 5)
    }

    init {
        add(JBScrollPane(miscDetail), BorderLayout.CENTER)
    }

    fun populateData(element: Element) {
        miscDetail.text = ""
        if(element is HasCommonField) {
            miscDetail.append(MyBundle.message("robot.output.editor.label.start-time"))
            miscDetail.append(": ")
            miscDetail.append(element.status.startTime)
            miscDetail.append("\n")
            miscDetail.append(MyBundle.message("robot.output.editor.label.end-time"))
            miscDetail.append(": ")
            miscDetail.append(element.status.endTime)
            miscDetail.append("\n")
            if(element.status.message.isNotEmpty()) {
                miscDetail.append(MyBundle.message("robot.output.editor.label.status-message"))
                miscDetail.append(": ")
                miscDetail.append(element.status.message)
                miscDetail.append("\n")
            }
        }
        if(element is KeywordElement) {
            val failMessage = element.messages.firstOrNull { it.level == LOG_LEVEL_FAIL }
            if(failMessage != null) {
                miscDetail.append(MyBundle.message("robot.output.editor.label.fail-reason"))
                miscDetail.append(": ")
                miscDetail.append(failMessage.value())
                miscDetail.append("\n")
            }
        }
    }

}