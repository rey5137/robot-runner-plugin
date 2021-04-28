package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.INPUT_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.InputArgument
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class InputTableCellRenderer(private val argumentModel: ArgumentModel) : TableCellRenderer {

    private val inputArgumentModel = InputArgumentModel()
    private val table = JBTable(inputArgumentModel)

    init {
        table.columnModel.getColumn(0).cellRenderer = InputArgumentCellRender(inputArgumentModel)
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val inputs = argumentModel.getInputArguments(row)
        return getCellRendererComponent(inputs, isSelected)
    }

    private fun getCellRendererComponent(inputs: List<InputArgument>, isSelected: Boolean): Component {
        if(inputs.isEmpty())
            inputArgumentModel.add(listOf(INPUT_EMPTY))
        else
            inputArgumentModel.add(inputs)
        if(isSelected) {
            if(inputArgumentModel.rowCount > 0)
                table.setRowSelectionInterval(0, inputArgumentModel.rowCount - 1)
            table.background = UIUtil.getTableSelectionBackground(true)
            table.border = UIUtil.getTableFocusCellHighlightBorder()
        }
        else {
            table.clearSelection()
            table.background = null
            table.border = null
        }
        return table
    }
}