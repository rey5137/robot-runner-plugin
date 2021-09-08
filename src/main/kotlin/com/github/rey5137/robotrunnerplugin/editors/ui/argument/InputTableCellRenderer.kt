package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightType
import com.github.rey5137.robotrunnerplugin.editors.ui.setHighlightBorder
import com.github.rey5137.robotrunnerplugin.editors.xml.INPUT_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.InputArgument
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class InputTableCellRenderer(private val argumentModel: ArgumentModel) : TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            val inputHolder = value as HighlightHolder<InputArgument>
            setHighlightBorder(inputHolder.highlight)

            val input = inputHolder.value
            if (input.name == null)
                append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            else {
                append(input.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                append(" = ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }
    }

    private val inputArgumentModel = InputArgumentModel()
    private val table = JBTable(inputArgumentModel).apply {
        setDefaultRenderer(Any::class.java, InputArgumentCellRender())
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val inputHolders = argumentModel.getInputArgumentHolders(row)

        val component = if (inputHolders.size == 1)
            stringCellRenderer.getTableCellRendererComponent(table, inputHolders[0], isSelected, hasFocus, row, column)
        else
            getCellRendererComponent(inputHolders, isSelected, hasFocus)
        table.setRowHeight(row, argumentModel.addColumnHeight(row, column, component.preferredSize.height))
        return component
    }

    private fun getCellRendererComponent(inputs: List<HighlightHolder<InputArgument>>, isSelected: Boolean, hasFocus: Boolean): Component {
        if (inputs.isEmpty())
            inputArgumentModel.add(listOf(HighlightHolder(INPUT_EMPTY, HighlightType.UNMATCHED)))
        else
            inputArgumentModel.add(inputs)
        if (isSelected) {
            if (inputArgumentModel.rowCount > 0)
                table.setRowSelectionInterval(0, inputArgumentModel.rowCount - 1)
        } else {
            table.clearSelection()
        }
        table.border = if (hasFocus) UIUtil.getTableFocusCellHighlightBorder() else null
        return table
    }
}
