package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class ArgumentModel : AbstractTableModel() {

    private var items: List<Item> = emptyList()

    fun setArguments(arguments: List<Argument<*>>, inputArguments: List<List<InputArgument>>) {
        items = arguments.mapIndexed { index, argument ->
            Item(
                argument = argument,
                inputs = inputArguments[index],
                argumentValue = argument.getFullName(),
                inputsValue = inputArguments[index].joinToString(separator = "    ") { it.rawInput },
                variableModel = if(argument.dataType == DataType.DICT || argument.dataType == DataType.ARRAY)
                    VariableModel().apply { setVariables(argument.value as List<Variable<*>>) }
                else
                    null
            )
        }
        fireTableDataChanged()
    }

    fun getArgument(index: Int) = items[index].argument

    fun getInputArguments(index: Int) = items[index].inputs

    fun getVariableModel(index: Int) = items[index].variableModel

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 3

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = when (columnIndex) {
        INDEX_ARGUMENT -> items[rowIndex].argument.name.isNotEmpty()
        INDEX_INPUT -> items[rowIndex].inputs.isNotEmpty()
        INDEX_VALUE -> items[rowIndex].argument != ARGUMENT_EMPTY
        else -> false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when (columnIndex) {
        INDEX_ARGUMENT -> items[rowIndex].argumentValue
        INDEX_INPUT -> items[rowIndex].inputsValue
        INDEX_VALUE -> items[rowIndex].argument.rawValue
        else -> null
    }

    private fun Argument<*>.getFullName() = if (name.isEmpty())
        ""
    else {
        when (argumentType) {
            ArgumentType.SINGLE -> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.DICT -> "$ARG_DICT$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
            ArgumentType.PYTHON -> name
        }

    }

    override fun getColumnName(column: Int): String = when (column) {
        INDEX_ARGUMENT -> MyBundle.message("robot.output.editor.label.argument")
        INDEX_INPUT -> MyBundle.message("robot.output.editor.label.input")
        INDEX_VALUE -> MyBundle.message("robot.output.editor.label.value")
        else -> ""
    }

    companion object {
        const val INDEX_ARGUMENT = 0
        const val INDEX_INPUT = 1
        const val INDEX_VALUE = 2
    }

    private data class Item(
        val argument: Argument<*>,
        val inputs: List<InputArgument>,
        val argumentValue: String,
        val inputsValue: String,
        val variableModel: VariableModel?
    )
}
