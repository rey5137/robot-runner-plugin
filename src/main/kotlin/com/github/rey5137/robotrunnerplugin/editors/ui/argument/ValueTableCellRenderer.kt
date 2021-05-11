package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.ARGUMENT_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.Argument
import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueTableCellRenderer(private val argumentModel: ArgumentModel) :
    TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable?,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            val argument = value as Argument<*>
            if (argument == ARGUMENT_EMPTY)
                append("")
            else
                when (argument.dataType) {
                    DataType.NONE -> append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    DataType.BOOL -> append(
                        if (argument.value as Boolean) "True" else "False",
                        SimpleTextAttributes.REGULAR_ATTRIBUTES
                    )
                    else -> {
                        val data = argument.value.toString()
                        if (data.isEmpty())
                            append("Empty String", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                        else
                            append(data, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    }
                }
        }
    }

    private val table = JBTable().apply {
        setDefaultRenderer(Any::class.java, VariableCellRender())
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val variableModel = argumentModel.getVariableModel(row)
        val component = if (variableModel == null)
            stringCellRenderer.getTableCellRendererComponent(
                table,
                argumentModel.getArgument(row),
                isSelected,
                hasFocus,
                row,
                column
            )
        else {
            getCellRendererComponent(variableModel, isSelected, hasFocus)
        }
        table.setRowHeight(row, component.preferredSize.height)
        return component
    }

    private fun getCellRendererComponent(
        variableModel: VariableModel,
        isSelected: Boolean,
        hasFocus: Boolean
    ): Component {
        table.model = variableModel
        if (isSelected) {
            if (variableModel.rowCount > 0)
                table.setRowSelectionInterval(0, variableModel.rowCount - 1)
        } else {
            table.clearSelection()
        }
        table.border = if (hasFocus) UIUtil.getTableFocusCellHighlightBorder() else null
        return table
    }
}