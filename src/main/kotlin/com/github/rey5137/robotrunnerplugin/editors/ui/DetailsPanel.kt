package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.HasCommonField
import com.github.rey5137.robotrunnerplugin.editors.xml.HasTagsField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class DetailsPanel : JPanel(MigLayout(createLayoutConstraints(10, 10))) {

    private val nameField = JBTextField()
    private val statusLabel = JBLabel()
    private val tagsPanel = JPanel(MigLayout())
    private val tagsField = JBTextField()

    init {
        add(statusLabel, CC().cell(0, 0).minWidth("32px"))
        add(nameField, CC().cell(0, 0).growX().pushX(1F))
        nameField.isEditable = false

        tagsPanel.add(JBLabel("Tags"))
        tagsPanel.add(tagsField, CC().pushX(1F))
        tagsField.isEditable = false
        add(tagsPanel, CC().newline().span(2))
    }

    fun showDetails(element: Element) {
        if (element is HasCommonField) {
            nameField.text = element.name
            nameField.select(0, 0)
            statusLabel.icon = if (element.status.isPassed) MyIcons.StatusPass else MyIcons.StatusFail
        }

        if(element is HasTagsField) {
            tagsField.text = element.tags.joinToString(separator = ",")
        }
        else {
            tagsField.text = ""
        }
    }
}