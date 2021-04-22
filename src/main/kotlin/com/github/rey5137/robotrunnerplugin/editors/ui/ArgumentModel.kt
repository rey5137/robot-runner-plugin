package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import java.awt.Component
import javax.swing.JList
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer

class ArgumentModel : AbstractTableModel() {

    private var arguments: List<Argument<*>> = emptyList()

    fun setArguments(arguments: List<Argument<*>>) {
        this.arguments = arguments
        fireTableDataChanged()
    }

    fun getArgument(index: Int) = arguments[index]

    override fun getRowCount(): Int  = arguments.size

    override fun getColumnCount(): Int = 2

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_ARGUMENT -> arguments[rowIndex].getFullName()
        INDEX_VALUE -> arguments[rowIndex].rawValue
        else -> null
    }

    private fun Argument<*>.getFullName() = if(name.isEmpty())
        ""
    else {
        val sign = when(argumentType) {
            ArgumentType.SINGLE -> ARG_SINGLE
            ArgumentType.DICT -> ARG_DICT
            ArgumentType.ARRAY -> ARG_ARRAY
        }
        "$sign$ARG_NAME_START$name$ARG_NAME_END"
    }

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_ARGUMENT -> "Argument"
        INDEX_VALUE -> "Value"
        else -> ""
    }

    companion object {
        const val INDEX_ARGUMENT = 0
        const val INDEX_VALUE = 1
    }
}

class ArgumentTableCellRenderer(private val model: ArgumentModel) : ColoredTableCellRenderer() {

    override fun customizeCellRenderer(
        table: JTable?,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val argument = model.getArgument(row)
        if(argument.name.isEmpty())
            append("")
        else {
            val sign = when(argument.argumentType) {
                ArgumentType.SINGLE -> ARG_SINGLE
                ArgumentType.DICT -> ARG_DICT
                ArgumentType.ARRAY -> ARG_ARRAY
            }.toString()
            append(sign, SimpleTextAttributes.GRAY_ATTRIBUTES)
            append(ARG_NAME_START.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES)
            append(argument.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            append(ARG_NAME_END.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES)
        }
    }

}

class ValueTableCellRenderer(private val model: ArgumentModel) : JBList<ValueTableCellRenderer.Item>(), TableCellRenderer {

    init {
        cellRenderer = object : ColoredListCellRenderer<Item>() {
            override fun customizeCellRenderer(
                list: JList<out Item>,
                value: Item,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if(value.name.isEmpty()) {
                    append(value.spacing)
                    append(value.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
                else {
                    append(value.spacing)
                    append("• ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    append(value.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    if(value.value.isNotEmpty()) {
                        append(": ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                        append(value.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    }
                }
            }
        }
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val list = ArrayList<Item>()
        val argument = model.getArgument(row)
        when(argument.value) {
            null -> list.add(Item(value = "None"))
            is Boolean -> list.add(Item(value = if(argument.value) "True" else "False"))
            is List<*> -> {
                list.addVariables(argument.value as List<Variable<*>>)
                setEmptyText(if(argument.dataType == DataType.DICT) "Empty Dict" else "Empty Array")
            }
            else -> list.add(Item(value = argument.value.toString()))
        }

        setListData(list.toTypedArray())

        if(isSelected) {
            selectedIndices = IntArray(size = list.size) { it }
            background = UIManager.getColor("Table.selectionBackground")
        }
        else {
            selectedIndex = -1
            background = UIManager.getColor("Table.background")
        }
        table?.setRowHeight(row, preferredSize.height)
        return this
    }

    private fun ArrayList<Item>.addVariables(variables: List<Variable<*>>, spacing: String = "") {
        variables.forEach { variable ->
            when(variable.type) {
                DataType.NONE -> add(Item(name = variable.name, value = "None", spacing = spacing))
                DataType.BOOL -> add(Item(name = variable.name, value = if(variable.value as Boolean) "True" else "False", spacing = spacing))
                DataType.DICT, DataType.ARRAY -> {
                    add(Item(name = variable.name, spacing = spacing))
                    addVariables(variable.value as List<Variable<*>>, "$spacing    ")
                }
                else -> add(Item(name = variable.name, value = variable.value.toString(), spacing = spacing))
            }
        }
    }

    data class Item (
        val spacing: String = "",
        val name: String = "",
        val value: String = ""
    )
}
