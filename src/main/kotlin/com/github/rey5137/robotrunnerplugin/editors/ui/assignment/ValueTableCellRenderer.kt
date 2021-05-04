package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableCellRender
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableModel
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueTableCellRenderer(private val assignmentModel: AssignmentModel) : TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable?,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            val assignment = value as Assignment<*>
            when(assignment.dataType) {
                DataType.NONE -> append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                DataType.BOOL -> append(if(assignment.value as Boolean) "True" else "False", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                else -> {
                    val data = assignment.value.toString()
                    if (data.isEmpty())
                        append("Empty String", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                    else
                        append(data, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
            }
        }
    }

    private val variableModel = VariableModel()
    private val table = JBTable(variableModel)

    init {
        table.columnModel.getColumn(0).cellRenderer = VariableCellRender(variableModel)
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val assignment = assignmentModel.getAssignment(row)
        val component = when(assignment.dataType) {
            DataType.DICT, DataType.ARRAY -> getCellRendererComponent(assignment.value as List<Variable<*>>, isSelected)
            else -> stringCellRenderer.getTableCellRendererComponent(table, assignment, isSelected, hasFocus, row, column)
        }
        table?.setRowHeight(row, component.preferredSize.height)
        return component
    }

    private fun getCellRendererComponent(variables: List<Variable<*>>, isSelected: Boolean): Component {
        variableModel.addVariables(variables)

        if(isSelected) {
            if(variableModel.rowCount > 0)
                table.setRowSelectionInterval(0, variableModel.rowCount - 1)
            table.border = UIUtil.getTableFocusCellHighlightBorder()
        }
        else {
            table.clearSelection()
            table.border = null
        }
        return table
    }
}