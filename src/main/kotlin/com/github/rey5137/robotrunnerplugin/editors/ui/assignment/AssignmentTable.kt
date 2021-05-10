package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.*
import com.intellij.icons.AllIcons
import com.intellij.ui.table.JBTable
import javax.swing.JTable

class AssignmentTable(private val assignmentModel: AssignmentModel) : JBTable(assignmentModel) {

    init {
        val levelPadding = AllIcons.General.ArrowDown.iconWidth
        columnModel.getColumn(AssignmentModel.INDEX_ASSIGNMENT).apply {
            cellRenderer = AssignmentTableCellRenderer(assignmentModel)
        }
        columnModel.getColumn(AssignmentModel.INDEX_VALUE).apply {
            cellRenderer = ValueTableCellRenderer(levelPadding, assignmentModel)
            cellEditor = ValueTableCellEditor(levelPadding, assignmentModel)
        }
        setDefaultEditor(Any::class.java, StringCellEditor())
    }
}