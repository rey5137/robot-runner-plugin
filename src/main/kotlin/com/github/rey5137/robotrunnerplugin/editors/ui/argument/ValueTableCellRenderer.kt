package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.Variable
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueTableCellRenderer(private val argumentModel: ArgumentModel) : TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable?,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            if(value == null)
                append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            else {
                val data = value.toString()
                if (data.isEmpty())
                    append("Empty String", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                else
                    append(data, SimpleTextAttributes.REGULAR_ATTRIBUTES)
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
        val argument = argumentModel.getArgument(row)
        val component = when(argument.dataType) {
            DataType.NONE -> stringCellRenderer.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column)
            DataType.BOOL -> stringCellRenderer.getTableCellRendererComponent(table, if(argument.value as Boolean) "True" else "False", isSelected, hasFocus, row, column)
            DataType.DICT, DataType.ARRAY -> getCellRendererComponent(argument.value as List<Variable<*>>, isSelected)
            else -> stringCellRenderer.getTableCellRendererComponent(table, argument.value.toString(), isSelected, hasFocus, row, column)
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