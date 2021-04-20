package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class ArgumentModel : AbstractTableModel() {

    private var arguments: List<Argument<*>> = emptyList()

    fun setArguments(arguments: List<Argument<*>>) {
        this.arguments = arguments
        fireTableDataChanged()
    }

    override fun getRowCount(): Int  = arguments.size

    override fun getColumnCount(): Int = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_NAME -> arguments[rowIndex].getFullName()
        INDEX_VALUE -> arguments[rowIndex].getValue()
        else -> null
    }

    private fun Argument<*>.getFullName(): String = when(argumentType) {
        ArgumentType.SINGLE -> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
        ArgumentType.DICT -> "$ARG_DICT$ARG_NAME_START$name$ARG_NAME_END"
        ArgumentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
    }

    private fun Argument<*>.getValue(): String = when(dataType) {
        DataType.NONE -> "None"
        DataType.BOOL, DataType.INTEGER, DataType.NUMBER, DataType.STRING -> value.toString()
        else -> ""
    }

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_NAME -> "Name"
        INDEX_VALUE -> "value"
        else -> ""
    }

    companion object {
        const val INDEX_NAME = 0
        const val INDEX_VALUE = 1
    }
}