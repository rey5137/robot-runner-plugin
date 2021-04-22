package com.github.rey5137.robotrunnerplugin.editors.ui

import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableColumn
import kotlin.math.max


class TableColumnAdjuster(private val table: JTable) {

    fun adjustColumn(column: Int) {
        val tableColumn: TableColumn = table.columnModel.getColumn(column)
        if (!tableColumn.resizable) return
        val columnHeaderWidth = getColumnHeaderWidth(column)
        val columnDataWidth = getColumnDataWidth(column)
        val preferredWidth = max(columnHeaderWidth, columnDataWidth)
        updateTableColumn(column, preferredWidth)
    }

    private fun getColumnHeaderWidth(column: Int): Int {
        val tableColumn: TableColumn = table.columnModel.getColumn(column)
        val value: Any = tableColumn.headerValue
        var renderer = tableColumn.headerRenderer ?: table.tableHeader.defaultRenderer
        val c: Component = renderer!!.getTableCellRendererComponent(
            table, value,
            false, false, -1, column
        )
        return c.preferredSize.width
    }

    private fun getColumnDataWidth(column: Int): Int {
        var preferredWidth = 0
        for (row in 0 until table.rowCount) preferredWidth = max(
            preferredWidth,
            getCellDataWidth(row, column)
        )
        return preferredWidth
    }

    private fun getCellDataWidth(row: Int, column: Int): Int {
        val cellRenderer = table.getCellRenderer(row, column)
        val value = table.getValueAt(row, column)
        val c: Component = cellRenderer.getTableCellRendererComponent(
            table, value,
            false, false, row, column
        )
        return (c.preferredSize.width + table.intercellSpacing.width)
    }

    private fun updateTableColumn(column: Int, width: Int) {
        val tableColumn: TableColumn = table.columnModel
            .getColumn(column)
        if (!tableColumn.resizable) return
        table.tableHeader.resizingColumn = tableColumn
        tableColumn.width = width + SPACING
    }

    companion object {
        // the amount of padding to add to each column after determining the width
        // of its data
        private const val SPACING = 10
    }
}