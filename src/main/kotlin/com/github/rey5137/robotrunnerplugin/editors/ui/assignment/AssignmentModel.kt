package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableModel
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class AssignmentModel : AbstractTableModel() {

    private var items: List<Item> = emptyList()

    fun setAssignments(assignments: List<Assignment<*>>) {
        items = assignments.map { Item(
            assignment = it,
            assigmentValue = it.getFullName(),
            variableModel = if(it.dataType == DataType.DICT || it.dataType == DataType.ARRAY)
                VariableModel().apply { setVariables(it.value as List<Variable<*>>) }
            else
                null
        ) }
        fireTableDataChanged()
    }

    fun getAssignment(rowIndex: Int) = items[rowIndex].assignment

    fun getVariableModel(rowIndex: Int) = items[rowIndex].variableModel

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = when (columnIndex) {
        INDEX_ASSIGNMENT -> items[rowIndex].assignment.name.isNotEmpty()
        INDEX_VALUE -> items[rowIndex].assignment.hasValue
        else -> true
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when (columnIndex) {
        INDEX_ASSIGNMENT -> items[rowIndex].assigmentValue
        INDEX_VALUE -> ""
        else -> null
    }

    private fun Assignment<*>.getFullName() = when (assignmentType) {
        AssignmentType.SINGLE -> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
        AssignmentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
    }

    override fun getColumnName(column: Int): String = when (column) {
        INDEX_ASSIGNMENT -> "Assignment"
        INDEX_VALUE -> "Value"
        else -> ""
    }

    companion object {
        const val INDEX_ASSIGNMENT = 0
        const val INDEX_VALUE = 1
    }

    data class Item(
        val assignment: Assignment<*>,
        val assigmentValue: String,
        val variableModel: VariableModel?,
    )
}
