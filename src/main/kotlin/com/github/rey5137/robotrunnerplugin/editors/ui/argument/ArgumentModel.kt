package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class ArgumentModel : AbstractTableModel() {

    private var arguments: List<Argument<*>> = emptyList()
    private var inputArguments: List<List<InputArgument>> = emptyList()

    fun setArguments(arguments: List<Argument<*>>, inputArguments: List<List<InputArgument>>) {
        this.arguments = arguments
        this.inputArguments = inputArguments
        fireTableDataChanged()
    }

    fun getArgument(index: Int) = arguments[index]

    fun getInputArguments(index: Int) = inputArguments[index]

    override fun getRowCount(): Int  = arguments.size

    override fun getColumnCount(): Int = 3

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = when(columnIndex) {
        INDEX_ARGUMENT -> arguments[rowIndex].name.isNotEmpty()
        INDEX_INPUT -> inputArguments[rowIndex].isNotEmpty()
        INDEX_VALUE -> arguments[rowIndex] != ARGUMENT_EMPTY
        else -> false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_ARGUMENT -> arguments[rowIndex].getFullName()
        INDEX_INPUT -> inputArguments[rowIndex].joinToString(separator = "    ") { it.rawInput }
        INDEX_VALUE -> arguments[rowIndex].rawValue
        else -> null
    }

    private fun Argument<*>.getFullName() = if(name.isEmpty())
        ""
    else {
        when(argumentType) {
            ArgumentType.SINGLE -> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.DICT -> "$ARG_DICT$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.PYTHON -> name
        }

    }

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_ARGUMENT -> "Argument"
        INDEX_INPUT -> "Input"
        INDEX_VALUE -> "Value"
        else -> ""
    }

    companion object {
        const val INDEX_ARGUMENT = 0
        const val INDEX_INPUT = 1
        const val INDEX_VALUE = 2
    }
}