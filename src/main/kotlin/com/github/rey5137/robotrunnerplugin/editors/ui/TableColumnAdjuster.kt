package com.github.rey5137.robotrunnerplugin.editors.ui

import java.awt.Component
import javax.swing.JTable
import javax.swing.table.TableColumn
import kotlin.math.max


fun JTable.adjustColumn(column: Int): Int? {
    val tableColumn: TableColumn = columnModel.getColumn(column)
    if (!tableColumn.resizable) return null
    val columnHeaderWidth = getColumnHeaderWidth(column)
    val columnDataWidth = getColumnDataWidth(column)
    val preferredWidth = max(columnHeaderWidth, columnDataWidth)
    updateTableColumn(column, preferredWidth)
    return preferredWidth
}

private fun JTable.getColumnHeaderWidth(column: Int): Int {
    val tableColumn: TableColumn = columnModel.getColumn(column)
    val value: Any = tableColumn.headerValue
    val renderer = tableColumn.headerRenderer ?: tableHeader.defaultRenderer
    val c: Component = renderer!!.getTableCellRendererComponent(
        this, value,
        false, false, -1, column
    )
    return c.preferredSize.width
}

private fun JTable.getColumnDataWidth(column: Int): Int {
    var preferredWidth = 0
    for (row in 0 until rowCount) preferredWidth = max(
        preferredWidth,
        getCellDataWidth(row, column)
    )
    return preferredWidth
}

private fun JTable.getCellDataWidth(row: Int, column: Int): Int {
    val cellRenderer = getCellRenderer(row, column)
    val value = getValueAt(row, column)
    val c: Component = cellRenderer.getTableCellRendererComponent(
        this, value,
        false, false, row, column
    )
    if (c is JTable) {
        val width = c.adjustColumn(0) ?: c.preferredSize.width
        return (width + intercellSpacing.width)
    }
    return (c.preferredSize.width + intercellSpacing.width)
}

private fun JTable.updateTableColumn(column: Int, width: Int) {
    val tableColumn: TableColumn = columnModel
        .getColumn(column)
    if (!tableColumn.resizable) return
    tableHeader.resizingColumn = tableColumn
    tableColumn.width = width + SPACING
}

private const val SPACING = 10