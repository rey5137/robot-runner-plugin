package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.message.MessageCellRender
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.*
import com.intellij.ui.components.JBList
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class MessagePanel(project: Project) : JPanel(BorderLayout()) {

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
    private val messageDetail = object : EditorTextField(null, project, FileTypes.PLAIN_TEXT, true, false) {
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
    private val messageSplitter = JBSplitter(0.3F)

    private lateinit var element: KeywordElement
    private var selectedMessageElement: MessageElement? = null

    private var showInfoMessage = true
    private var showDebugMessage = true
    private var showTraceMessage = true
    private var showFailMessage = true
    private var showErrorMessage = true

    init {
        messageSplitter.firstComponent = ToolbarDecorator.createDecorator(messageList)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .addExtraAction(object : DumbAwareActionButton(MyBundle.message("robot.output.editor.label.filter-message"), AllIcons.General.Filter) {
                override fun actionPerformed(e: AnActionEvent) {
                    JBPopupFactory.getInstance().createActionGroupPopup(null, DefaultActionGroup().apply {
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", LOG_LEVEL_INFO)) {
                            override fun isSelected(e: AnActionEvent): Boolean = showInfoMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showInfoMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", LOG_LEVEL_DEBUG)) {
                            override fun isSelected(e: AnActionEvent): Boolean = showDebugMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showDebugMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", LOG_LEVEL_TRACE)) {
                            override fun isSelected(e: AnActionEvent): Boolean = showTraceMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showTraceMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", LOG_LEVEL_FAIL)) {
                            override fun isSelected(e: AnActionEvent): Boolean = showFailMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showFailMessage = state
                                populateMessage(element)
                            }
                        })
                        add(object : ToggleAction(MyBundle.message("robot.output.editor.desc.show-level-message", LOG_LEVEL_ERROR)) {
                            override fun isSelected(e: AnActionEvent): Boolean = showErrorMessage

                            override fun setSelected(e: AnActionEvent, state: Boolean) {
                                showErrorMessage = state
                                populateMessage(element)
                            }
                        })
                    }, e.dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
                        .show(preferredPopupPoint!!)
                }
            })
            .createPanel()
        messageSplitter.secondComponent = messageDetail
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
                        || (it.level == LOG_LEVEL_ERROR && showErrorMessage)
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
        messageDetail.setCaretPosition(0)
        messageDetail.revalidate()
    }

}