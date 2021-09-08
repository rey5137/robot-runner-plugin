package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class DetailsPanel(project: Project) : JPanel(MigLayout(LC().gridGap("10px", "5px").insets("0px").hideMode(2))) {

    private val nameField = JBTextField()
    private val statusLabel = JBLabel()
    private val tagsField = JBTextField()
    private val tabPane = JBTabbedPane()
    private val reasonLabel = JBLabel()
    private val reasonField = JBTextField()

    private val argumentPanel = ArgumentPanel(project)
    private val messagePanel = MessagePanel(project)
    private val miscPanel = MiscPanel(project)

    private var element: Element? = null

    init {
        relayout(false)
    }

    fun updateHighlightInfo(highlightInfo: HighlightInfo?) {
        element?.let { showDetails(it, highlightInfo) }
    }

    fun showDetails(element: Element, highlightInfo: HighlightInfo?) {
        this.element = element
        var failReason: String? = null
        if (element is KeywordElement) {
            failReason = element.messages.firstOrNull { it.level == LOG_LEVEL_FAIL }?.value()
        }
        relayout(failReason != null)

        if (element is HasCommonField) {
            nameField.text = element.name
            nameField.select(0, 0)
            statusLabel.icon = when {
                element.status.isPassed -> MyIcons.StatusPass
                element.status.isRunning -> MyIcons.StatusRunning
                else -> MyIcons.StatusFail
            }
        }

        if (element is HasTagsField)
            tagsField.text = element.tags.joinToString(separator = ", ")
        else
            tagsField.text = ""

        tabPane.removeAll()
        if (element is KeywordElement) {
            tabPane.add(MyBundle.message("robot.output.editor.label.argument-tab"), argumentPanel)
            argumentPanel.border = null
            argumentPanel.populateData(element, highlightInfo)

            tabPane.add(MyBundle.message("robot.output.editor.label.message-tab"), messagePanel)
            messagePanel.border = null
            messagePanel.populateData(element, highlightInfo)

            val failMessage = element.messages.firstOrNull { it.level == LOG_LEVEL_FAIL }
            if (failMessage != null) {
                reasonField.text = failMessage.value() ?: ""
            }
        }

        tabPane.add(MyBundle.message("robot.output.editor.label.misc-tab"), miscPanel)
        miscPanel.border = null
        miscPanel.populateData(element)
    }

    fun relayout(hasReason: Boolean) {
        removeAll()

        var row = 0
        add(statusLabel, CC().cell(0, row).minWidth("48px"))
        add(nameField, CC().cell(0, row).growX().pushX(1F))
        nameField.isEditable = false
        row++

        if (hasReason) {
            add(reasonLabel, CC().cell(0, row).minWidth("48px"))
            add(reasonField, CC().cell(0, row).growX().pushX(1F))
            reasonLabel.text = MyBundle.message("robot.output.editor.label.fail-reason")
            reasonField.isEditable = false
            row++
        }

        add(JBLabel(MyBundle.message("robot.output.editor.label.tags")), CC().cell(0, row).minWidth("48px"))
        add(tagsField, CC().cell(0, row).growX().pushX(1F))
        tagsField.isEditable = false

        row++
        add(tabPane, CC().cell(0, row).grow().push(1F, 1F).gapBottom("10px").gapRight("10px"))
    }
}
