package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.ui.message.MessageCellRender
import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement
import com.github.rey5137.robotrunnerplugin.editors.xml.MessageElement
import com.github.rey5137.robotrunnerplugin.editors.xml.RobotElement
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel

class MessagePanel(private val robotElement: RobotElement) : JPanel(BorderLayout()) {

    private val messageModel = DefaultListModel<MessageElement>()
    private val messageList = JBList(messageModel).apply {
        cellRenderer = MessageCellRender()
        addListSelectionListener {
            if(!it.valueIsAdjusting) {
                showMessageDetail(selectedValue)
            }
        }
    }
    private val messageDetail = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
    }
    private val messageSplitter = JBSplitter(0.3F)

    private lateinit var element: KeywordElement

    init {
        messageSplitter.firstComponent = ToolbarDecorator.createDecorator(messageList)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .createPanel()
        messageSplitter.secondComponent = JBScrollPane(messageDetail)
        add(messageSplitter, BorderLayout.CENTER)
    }

    fun populateData(element: KeywordElement) {
        this.element = element
        messageModel.populateModel(element)
    }

    private fun DefaultListModel<MessageElement>.populateModel(element: KeywordElement) {
        clear()
        element.messages.forEach { addElement(it) }
    }

    private fun showMessageDetail(messageElement: MessageElement?) {
        messageDetail.text = if(messageElement != null) robotElement.messageMap[messageElement.valueIndex] else ""
        messageDetail.caretPosition = 0
        messageDetail.revalidate()
    }

}