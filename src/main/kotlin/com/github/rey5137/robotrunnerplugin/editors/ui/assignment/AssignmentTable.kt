package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.*
import com.intellij.ui.table.JBTable
import javax.swing.JTable

class AssignmentTable(private val assignmentModel: AssignmentModel) : JBTable(assignmentModel) {

    init {
        columnModel.getColumn(AssignmentModel.INDEX_ASSIGNMENT).apply {
            cellRenderer = AssignmentTableCellRenderer(assignmentModel)
        }
        columnModel.getColumn(AssignmentModel.INDEX_VALUE).apply {
            cellRenderer = ValueTableCellRenderer(assignmentModel)
            cellEditor = ValueTableCellEditor(assignmentModel)
        }
        setDefaultEditor(Any::class.java, StringCellEditor())
    }
}