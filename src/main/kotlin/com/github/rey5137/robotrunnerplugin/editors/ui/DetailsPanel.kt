package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel


class DetailsPanel : JPanel(MigLayout(LC().gridGap("10px", "10px").insets("0px"))) {

    private val nameField = JBTextField()
    private val statusLabel = JBLabel()
    private val tagsField = JBTextField()
    private val tabPane = JBTabbedPane()

    private val argumentPanel = ArgumentPanel()
    private val messagePanel = MessagePanel()
    private val miscPanel = MiscPanel()

    init {
        add(statusLabel, CC().cell(0, 0).minWidth("48px"))
        add(nameField, CC().cell(0, 0).growX().pushX(1F))
        nameField.isEditable = false

        add(JBLabel(MyBundle.message("robot.output.editor.label.tags")), CC().cell(0, 1).minWidth("48px"))
        add(tagsField, CC().cell(0, 1).growX().pushX(1F))
        tagsField.isEditable = false

        add(tabPane, CC().cell(0, 2).grow().push(1F, 1F).gapBottom("10px").gapRight("10px"))
    }

    fun showDetails(element: Element) {
        if (element is HasCommonField) {
            nameField.text = element.name
            nameField.select(0, 0)
            statusLabel.icon = if (element.status.isPassed) MyIcons.StatusPass else MyIcons.StatusFail
        }

        if(element is HasTagsField)
            tagsField.text = element.tags.joinToString(separator = ", ")
        else
            tagsField.text = ""

        tabPane.removeAll()
        if(element is KeywordElement) {
            tabPane.add(MyBundle.message("robot.output.editor.label.argument-tab"), argumentPanel)
            argumentPanel.border = null
            argumentPanel.populateData(element)

            tabPane.add(MyBundle.message("robot.output.editor.label.message-tab"), messagePanel)
            messagePanel.border = null
            messagePanel.populateData(element)
        }

        tabPane.add(MyBundle.message("robot.output.editor.label.misc-tab"), miscPanel)
        miscPanel.border = null
        miscPanel.populateData(element)
    }

}