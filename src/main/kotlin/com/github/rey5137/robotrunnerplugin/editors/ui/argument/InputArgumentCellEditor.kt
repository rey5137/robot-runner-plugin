package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class InputArgumentCellEditor(private val model : InputArgumentModel, private val inputTableCellEditor: InputTableCellEditor) : AbstractTableCellEditor() {

    override fun isCellEditable(e: EventObject?): Boolean {
        if(e is MouseEvent) {
            if(inputTableCellEditor.editEvent != null) {
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
        val input = model.getItem(row)
        val editor = table.getDefaultEditor(Any::class.java)
        return if(input.name == null)
            editor.getTableCellEditorComponent(table, input.value, isSelected, row, column)
        else
            editor.getTableCellEditorComponent(table, "${input.name}: ${input.value}", isSelected, row, column)
    }

}