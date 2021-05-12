package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.message.MessageCellRender
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.DumbAwareActionButton
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Insets
import javax.swing.DefaultListModel
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class MessagePanel : JPanel(BorderLayout()) {

    private val messageModel = DefaultListModel<MessageElement>()
    private val messageList = JBList(messageModel).apply {
        cellRenderer = MessageCellRender()
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        addListSelectionListener {
            if (!it.valueIsAdjusting) {
                showMessageDetail(selectedValue)
            }
        }
    }
    private val messageDetail = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        margin = Insets(5, 5, 5, 5)
    }
    private val messageSplitter = JBSplitter(0.3F)

    private lateinit var element: KeywordElement
    private var selectedMessageElement: MessageElement? = null

    private var showInfoMessage = true
    private var showDebugMessage = true
    private var showTraceMessage = true
    private var showFailMessage = true

    init {
        messageSplitter.firstComponent = ToolbarDecorator.createDecorator(messageList)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .addExtraAction(object : DumbAwareActionButton(MyBundle.message("robot.output.editor.label.filter-message"), AllIcons.General.Filter) {
                override fun actionPerformed(e: AnActionEvent) {
                    JBPopupFactory.getInstance().createActionGroupPopup(null, DefaultActionGroup().apply {
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", "INFO")) {
                            override fun isSelected(e: AnActionEvent): Boolean = showInfoMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showInfoMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", "DEBUG")) {
                            override fun isSelected(e: AnActionEvent): Boolean = showDebugMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showDebugMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", "TRACE")) {
                            override fun isSelected(e: AnActionEvent): Boolean = showTraceMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showTraceMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", "FAIL")) {
                            override fun isSelected(e: AnActionEvent): Boolean = showFailMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showFailMessage = state
                                populateMessage(element)
                            }
                        })
                    }, e.dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
                        .show(preferredPopupPoint!!)
                }
            })
            .createPanel()
        messageSplitter.secondComponent = JBScrollPane(messageDetail)
        messageSplitter.setHonorComponentsMinimumSize(true)
        add(messageSplitter, BorderLayout.CENTER)
    }

    fun populateData(element: KeywordElement) {
        this.element = element
        selectedMessageElement = null
        populateMessage(element)
    }

    private fun populateMessage(element: KeywordElement) {
        val selectedMessageElement = this.selectedMessageElement
        messageModel.clear()
        var selectedIndex = -1
        element.messages
            .filter {
                (it.level == LOG_LEVEL_INFO && showInfoMessage)
                        || (it.level == LOG_LEVEL_DEBUG && showDebugMessage)
                        || (it.level == LOG_LEVEL_TRACE && showTraceMessage)
                        || (it.level == LOG_LEVEL_FAIL && showFailMessage)
            }
            .forEach {
                messageModel.addElement(it)
                if(it == selectedMessageElement)
                    selectedIndex = messageModel.size() - 1
            }
        if(selectedIndex >= 0)
            messageList.selectedIndex = selectedIndex
    }

    private fun showMessageDetail(messageElement: MessageElement?) {
        selectedMessageElement = messageElement
        messageDetail.text = messageElement?.value() ?: ""
        messageDetail.caretPosition = 0
        messageDetail.revalidate()
    }

}