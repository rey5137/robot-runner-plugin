package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class VariableCellEditor(private val editEventProvider: EditEventProvider) : AbstractTableCellEditor() {

    private val stringCellEditor = StringCellEditor()

    override fun isCellEditable(e: EventObject?): Boolean {
        if (e is MouseEvent) {
            if (editEventProvider.editEvent != null) {
                editEventProvider.editEvent = null
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
        val (variable) = (table.model as VariableModel).getItem(row)
        val builder = StringBuilder()
        builder.append(variable.name)
        if (variable.type != DataType.DICT && variable.type != DataType.ARRAY) {
            builder.append(" = ")
            when (variable.type) {
                DataType.NONE -> builder.append("None")
                DataType.BOOL -> builder.append(if (variable.value as Boolean) "True" else "False")
                else -> builder.append(variable.value.toString())
            }
        }
        return stringCellEditor.getTableCellEditorComponent(table, builder.toString(), isSelected, row, column)
    }

    interface EditEventProvider {
        var editEvent: MouseEvent?
    }
}
