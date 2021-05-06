package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Insets
import javax.swing.JPanel

class MiscPanel : JPanel(BorderLayout()) {

    lateinit var robotElement: RobotElement

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
            miscDetail.append("Start time: ")
            miscDetail.append(element.status.startTime)
            miscDetail.append("\n")
            miscDetail.append("End time: ")
            miscDetail.append(element.status.endTime)
            miscDetail.append("\n")
            if(element.status.message.isNotEmpty()) {
                miscDetail.append("Status message: ")
                miscDetail.append(element.status.message)
                miscDetail.append("\n")
            }
        }
    }

}