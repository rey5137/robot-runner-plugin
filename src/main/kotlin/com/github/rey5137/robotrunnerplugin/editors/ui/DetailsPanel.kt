package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel


class DetailsPanel(robotElement: RobotElement)
    : JPanel(MigLayout(createLayoutConstraints(10, 10))) {

    private val nameField = JBTextField()
    private val statusLabel = JBLabel()
    private val tagsField = JBTextField()
    private val tabPane = JBTabbedPane()

    private val argumentPanel = ArgumentPanel(robotElement)
    private val messagePanel = MessagePanel(robotElement)

    init {
        add(statusLabel, CC().cell(0, 0).minWidth("32px"))
        add(nameField, CC().cell(0, 0).growX().pushX(1F))
        nameField.isEditable = false

        add(JBLabel("Tags"), CC().cell(0, 1).minWidth("32px"))
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

        if(element is HasTagsField) {
            tagsField.text = element.tags.joinToString(separator = ", ")
        }
        else {
            tagsField.text = ""
        }

        tabPane.removeAll()
        if(element is KeywordElement) {
            tabPane.add("Argument / Assigment", argumentPanel)
            argumentPanel.border = null
            argumentPanel.populateData(element)

            tabPane.add("Messages", messagePanel)
            messagePanel.border = null
            messagePanel.populateData(element)
        }
    }

}