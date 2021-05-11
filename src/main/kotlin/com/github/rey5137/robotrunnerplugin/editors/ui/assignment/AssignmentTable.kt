package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.*
import com.intellij.ui.table.JBTable
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

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
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                e.isArrowClicked { _, _, _ ->
                    clearSelection()
                }
            }

            override fun mouseClicked(e: MouseEvent) {
                e.isArrowClicked { row, model, variableRow ->
                    val item = model.getItem(variableRow)
                    if(item.isExpanded)
                        model.collapseAt(variableRow)
                    else
                        model.expandAt(variableRow)
                    assignmentModel.fireTableRowsUpdated(row, row)
                }
            }

            private fun MouseEvent.isArrowClicked(func: (rowIndex: Int, model: VariableModel, variableRow: Int) -> Unit) {
                val p = Point(point)
                val row = rowAtPoint(p)
                val column = columnAtPoint(p)
                if(column == AssignmentModel.INDEX_VALUE) {
                    val variableModel = assignmentModel.getVariableModel(row)
                    if(variableModel != null) {
                        val rect = getCellRect(row, column, false)
                        p.translate(-rect.x, -rect.y)
                        val variableRow = p.y * variableModel.rowCount / rect.height
                        val item = variableModel.getItem(variableRow)
                        if (!item.isLeaf && VariableCellRender.isArrowClicked(p.x, item.level))
                            func(row, variableModel, variableRow)
                    }
                }
            }

        })
    }
}