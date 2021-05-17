package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.intellij.ui.table.JBTable
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable

class ArgumentTable(private val argumentModel: ArgumentModel) : JBTable(argumentModel) {

    init {
        cellSelectionEnabled = true
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
        columnModel.getColumn(ArgumentModel.INDEX_ARGUMENT).apply {
            cellRenderer = ArgumentTableCellRenderer(argumentModel)
        }
        columnModel.getColumn(ArgumentModel.INDEX_INPUT).apply {
            cellRenderer = InputTableCellRenderer(argumentModel)
            cellEditor = InputTableCellEditor(argumentModel)
        }
        columnModel.getColumn(ArgumentModel.INDEX_VALUE).apply {
            cellRenderer = ValueTableCellRenderer(argumentModel)
            cellEditor = ValueTableCellEditor(this@ArgumentTable, argumentModel)
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
                    argumentModel.fireTableRowsUpdated(row, row)
                }
            }

            private fun MouseEvent.isArrowClicked(func: (rowIndex: Int, model: VariableModel, variableRow: Int) -> Unit) {
                val p = Point(point)
                val row = rowAtPoint(p)
                val column = columnAtPoint(p)
                if(column == ArgumentModel.INDEX_VALUE) {
                    val variableModel = argumentModel.getVariableModel(row)
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

    override fun scrollRectToVisible(aRect: Rectangle?) {
        //disable auto scroll
    }
}