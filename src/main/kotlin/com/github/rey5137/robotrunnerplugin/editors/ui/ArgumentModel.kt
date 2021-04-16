package com.github.rey5137.robotrunnerplugin.editors.ui

import javax.swing.table.AbstractTableModel

class ArgumentModel: AbstractTableModel() {

    data class Item(val argument: String, val value: String = "")

    private var items = listOf<Item>()

    fun setItems(items: List<Item>) {
        this.items = items
        fireTableStructureChanged()
    }

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 2

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_ARGUMENT -> "Argument"
        INDEX_VALUE -> "Value"
        else -> ""
    }

    override fun getColumnClass(columnIndex: Int): Class<*> = when(columnIndex) {
        INDEX_ARGUMENT -> String::class.java
        INDEX_VALUE -> String::class.java
        else -> Any::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_ARGUMENT -> items[rowIndex].argument
        INDEX_VALUE -> items[rowIndex].value
        else -> null
    }

    override fun setValueAt(value: Any, rowIndex: Int, columnIndex: Int) {}

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = false

    companion object {
        const val INDEX_ARGUMENT = 0
        const val INDEX_VALUE = 1
    }
}