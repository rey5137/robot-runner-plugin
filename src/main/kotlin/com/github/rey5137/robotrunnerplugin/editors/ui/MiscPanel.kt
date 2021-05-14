package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.HasCommonField
import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement
import com.github.rey5137.robotrunnerplugin.editors.xml.LOG_LEVEL_FAIL
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
import java.awt.BorderLayout
import javax.swing.JPanel

class MiscPanel(project: Project) : JPanel(BorderLayout()) {

    private val miscDetail = object : EditorTextField(null, project, FileTypes.PLAIN_TEXT, true, false) {
        override fun createEditor(): EditorEx {
            return super.createEditor().apply {
                setCaretEnabled(true)
                setCaretVisible(true)
                setVerticalScrollbarVisible(true)
                setHorizontalScrollbarVisible(true)
            }
        }

        init {
            border = IdeBorderFactory.createBorder(SideBorder.ALL)
        }
    }

    init {
        add(miscDetail, BorderLayout.CENTER)
    }

    fun populateData(element: Element) {
        val builder = StringBuilder()
        if (element is HasCommonField) {
            builder.append(MyBundle.message("robot.output.editor.label.start-time"))
                .append(": ")
                .append(element.status.startTime)
                .append("\n")
                .append(MyBundle.message("robot.output.editor.label.end-time"))
                .append(": ")
                .append(element.status.endTime)
                .append("\n")
            if (element.status.message.isNotEmpty())
                builder.append(MyBundle.message("robot.output.editor.label.status-message"))
                    .append(": ")
                    .append(element.status.message)
                    .append("\n")
        }
        if (element is KeywordElement) {
            val failMessage = element.messages.firstOrNull { it.level == LOG_LEVEL_FAIL }
            if (failMessage != null)
                builder.append(MyBundle.message("robot.output.editor.label.fail-reason"))
                    .append(": ")
                    .append(failMessage.value())
                    .append("\n")
            val document = element.doc
            if(document.isNotEmpty())
                builder.append(MyBundle.message("robot.output.editor.label.document"))
                    .append(": ")
                    .append(document)
                    .append("\n")
        }
        miscDetail.text = builder.toString()
        miscDetail.setCaretPosition(0)
    }

}