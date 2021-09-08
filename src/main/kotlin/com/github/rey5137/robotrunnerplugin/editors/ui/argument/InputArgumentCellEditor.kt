package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class InputArgumentCellEditor(private val inputTableCellEditor: InputTableCellEditor) : AbstractTableCellEditor() {

    private val stringCellEditor = StringCellEditor()

    override fun isCellEditable(e: EventObject?): Boolean {
        if (e is MouseEvent) {
            if (inputTableCellEditor.editEvent != null) {
                inputTableCellEditor.editEvent = null
                return false
            }
            return e.clickCount > 1
        }
        return false
    }

    override fun getCellEditorValue(): Any = ""

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val inputHolder = (table.model as InputArgumentModel).getItem(row)
        val input = inputHolder.value
        return if (input.name == null)
            stringCellEditor.getTableCellEditorComponent(table, input.value, isSelected, row, column)
        else
            stringCellEditor.getTableCellEditorComponent(table, "${input.name}: ${input.value}", isSelected, row, column)
    }
}
