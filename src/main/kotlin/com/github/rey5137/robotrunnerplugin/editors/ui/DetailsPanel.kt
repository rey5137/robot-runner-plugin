package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.HasCommonField
import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class DetailsPanel : JPanel(MigLayout(createLayoutConstraints(10, 10))) {

    private val nameLabel = JBLabel()
    private val statusLabel = JBLabel()
    private val tagsPanel = JPanel(MigLayout())
    private val tagsField = JBTextField()

    init {
        add(statusLabel, CC().cell(0, 0))
        add(nameLabel, CC().cell(0, 0).growX().pushX(1F))

        tagsPanel.add(JBLabel("Tags"))
        tagsPanel.add(tagsField, CC().pushX(1F))
        tagsField.isEditable = false
        add(tagsPanel, CC().newline().span(2))
    }

    fun showDetails(element: Element) {
        if (element is HasCommonField) {
            nameLabel.text = element.name
            if("PASS".equals(element.status.status, true))
                statusLabel.icon = MyIcons.TestPass
            else
                statusLabel.icon = MyIcons.TestFail
        }

        if(element is KeywordElement) {
            tagsField.text = element.tags.joinToString(separator = ",")
        }
        else {
            tagsField.text = ""
        }
    }
}