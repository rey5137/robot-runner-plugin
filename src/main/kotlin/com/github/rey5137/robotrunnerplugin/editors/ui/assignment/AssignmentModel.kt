package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class AssignmentModel : AbstractTableModel() {

    private var assignments: List<Assignment<*>> = emptyList()

    fun setAssignments(assignments: List<Assignment<*>>) {
        this.assignments = assignments
        fireTableDataChanged()
    }

    fun getAssignment(rowIndex: Int) = assignments[rowIndex]

    override fun getRowCount(): Int  = assignments.size

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_ASSIGNMENT -> assignments[rowIndex].getFullName()
        INDEX_VALUE -> assignments[rowIndex].value
        else -> null
    }

    private fun Assignment<*>.getFullName() = when(assignmentType) {
        AssignmentType.SINGLE-> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
        AssignmentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
    }

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_ASSIGNMENT -> "Assignment"
        INDEX_VALUE -> "Value"
        else -> ""
    }

    companion object {
        const val INDEX_ASSIGNMENT = 0
        const val INDEX_VALUE = 1
    }
}
