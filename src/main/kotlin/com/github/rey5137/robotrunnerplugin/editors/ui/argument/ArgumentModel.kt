package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.*
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel
import kotlin.math.max

class ArgumentModel : AbstractTableModel() {

    private var items: List<Item> = emptyList()

    fun addColumnHeight(row: Int, column: Int, height: Int): Int {
        val item = items[row]
        when (column) {
            INDEX_ARGUMENT -> item.argumentColumn = height
            INDEX_INPUT -> item.inputColumn = height
            INDEX_VALUE -> item.valueColumn = height
        }
        return item.rowHeight
    }

    fun setArguments(arguments: List<Argument<*>>, inputArguments: List<List<InputArgument>>, highlightInfo: HighlightInfo?) {
        items = arguments.mapIndexed { index, argument ->
            val argumentFullName = argument.getFullName()
            Item(
                argumentHolder = argument.toHighlightHolder(highlightInfo.match(argumentFullName)),
                inputs = inputArguments[index].map { it.toHighlightHolder(highlightInfo.match(it.rawInput)) },
                argumentValue = argumentFullName,
                inputsValue = inputArguments[index].joinToString(separator = "    ") { it.rawInput },
                variableModel = if (argument.dataType == DataType.DICT || argument.dataType == DataType.ARRAY)
                    VariableModel().apply { setVariables(argument.name.ifEmpty { "Argument" }, argument.dataType, argument.value as List<Variable<*>>, highlightInfo) }
                else
                    null,
                isFilePath = argument.isFilePath(),
                valueHighlightType = if (argument.dataType == DataType.DICT || argument.dataType == DataType.ARRAY)
                    HighlightType.UNMATCHED
                else if (highlightInfo.match(argument.rawValue))
                    HighlightType.MATCHED
                else
                    HighlightType.UNMATCHED
            )
        }
        fireTableDataChanged()
    }

    fun getArgumentHolder(index: Int) = items[index].argumentHolder

    fun getInputArgumentHolders(index: Int) = items[index].inputs

    fun getVariableModel(index: Int) = items[index].variableModel

    fun getItem(index: Int) = items[index]

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 3

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = when (columnIndex) {
        INDEX_ARGUMENT -> items[rowIndex].argumentHolder.value.name.isNotEmpty()
        INDEX_INPUT -> items[rowIndex].inputs.isNotEmpty()
        INDEX_VALUE -> items[rowIndex].argumentHolder != ARGUMENT_EMPTY
        else -> false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when (columnIndex) {
        INDEX_ARGUMENT -> items[rowIndex].argumentValue
        INDEX_INPUT -> items[rowIndex].inputsValue
        INDEX_VALUE -> items[rowIndex].argumentHolder.value.rawValue
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

    data class Item(
        val argumentHolder: HighlightHolder<Argument<*>>,
        val inputs: List<HighlightHolder<InputArgument>>,
        val argumentValue: String,
        val inputsValue: String,
        val variableModel: VariableModel?,
        val isFilePath: Boolean = false,
        val valueHighlightType: HighlightType = HighlightType.UNMATCHED,
        var argumentColumn: Int = 0,
        var inputColumn: Int = 0,
        var valueColumn: Int = 0,
    ) {
        val rowHeight: Int
            get() = max(max(argumentColumn, inputColumn), valueColumn)
    }
}
