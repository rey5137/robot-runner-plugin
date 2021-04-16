package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.ui.table.JBTable
import icons.MyIcons
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import javax.swing.JPanel

class DetailsPanel(private val robotElement: RobotElement)
    : JPanel(MigLayout(createLayoutConstraints(10, 10))) {

    private val nameField = JBTextField()
    private val statusLabel = JBLabel()
    private val tagsField = JBTextField()
    private val tabPane = JBTabbedPane()
    private val argumentModel = ArgumentModel()
    private val argumentTable = JBTable(argumentModel)
    private val messagePanel = JPanel()

    init {
        add(statusLabel, CC().cell(0, 0).minWidth("32px"))
        add(nameField, CC().cell(0, 0).growX().pushX(1F))
        nameField.isEditable = false

        add(JBLabel("Tags"), CC().cell(0, 1).minWidth("32px"))
        add(tagsField, CC().cell(0, 1).growX().pushX(1F))
        tagsField.isEditable = false

        add(tabPane, CC().newline().grow().push(1F, 1F))
        argumentTable.tableHeader.resizingAllowed = true
        argumentTable.tableHeader.reorderingAllowed = false
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
            tabPane.add("Arguments", argumentTable)
            tabPane.add("Messages", messagePanel)

            argumentModel.setItems(findActualArguments(element))
        }
    }

    private fun findActualArguments(element: KeywordElement): List<ArgumentModel.Item> {
        val regex = "Arguments: \\[ (.*) ]".toRegex()
        val message = element.messages.asSequence()
            .filter { it.level == "TRACE"}
            .mapNotNull { robotElement.messageMap[it.valueIndex] }
            .find { it.matches(regex) } ?: return emptyList()

        val argRegex = ".*\\{(.*)}=(.*)".toRegex()
        return regex.matchEntire(message)!!.groupValues[1].split(" | ")
            .map {
                val result = argRegex.matchEntire(it)
                if(result == null)
                    ArgumentModel.Item(
                        argument = "",
                        value = it
                    )
                else
                    ArgumentModel.Item(
                        argument = result.groupValues[1],
                        value = result.groupValues[2]
                    )
            }
    }

}