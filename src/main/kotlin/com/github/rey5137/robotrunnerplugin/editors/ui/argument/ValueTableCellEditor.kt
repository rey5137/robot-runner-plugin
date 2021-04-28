package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.Variable
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class ValueTableCellEditor(private val argumentModel: ArgumentModel): AbstractTableCellEditor() {

    private val variableModel = VariableModel()
    private val table = JBTable(variableModel)
    var editEvent: MouseEvent? = null

    init {
        table.columnModel.getColumn(0).apply {
            cellRenderer = VariableCellRender(variableModel)
            cellEditor = VariableCellEditor(variableModel, this@ValueTableCellEditor)
        }
        table.setDefaultEditor(Any::class.java, StringCellEditor())
    }

    override fun isCellEditable(e: EventObject?): Boolean {
        if(e is MouseEvent) {
            val enable = e.clickCount > 1
            if(enable) {
                editEvent = e
            }
            return enable
        }
        return false
    }

    override fun stopCellEditing(): Boolean {
        editEvent = null
        return super.stopCellEditing()
    }

    override fun cancelCellEditing() {
        editEvent = null
        super.cancelCellEditing()
    }

    override fun getCellEditorValue(): Any = ""

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val argument = argumentModel.getArgument(row)
        val editor = table.getDefaultEditor(Any::class.java)
        return when(argument.dataType) {
            DataType.NONE -> editor.getTableCellEditorComponent(table, "None", isSelected, row, column)
            DataType.BOOL -> editor.getTableCellEditorComponent(table, if(argument.value as Boolean) "True" else "False", isSelected, row, column)
            DataType.DICT, DataType.ARRAY -> getCellEditorComponent(argument.value as List<Variable<*>>)
            else -> editor.getTableCellEditorComponent(table, argument.value.toString(), isSelected, row, column)
        }
    }

    private fun getCellEditorComponent(variables: List<Variable<*>>): Component {
        variableModel.addVariables(variables)
        return table
    }

}