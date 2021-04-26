package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ArgumentModel : AbstractTableModel() {

    private var arguments: List<Argument<*>> = emptyList()

    fun setArguments(arguments: List<Argument<*>>) {
        this.arguments = arguments
        fireTableDataChanged()
    }

    fun getArgument(index: Int) = arguments[index]

    override fun getRowCount(): Int  = arguments.size

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = true

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

class ValueTableCellRenderer(private val argumentModel: ArgumentModel) : TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable?,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            append(value?.toString() ?: "", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
    }

    private val variableModel = VariableModel()
    private val table = JBTable(variableModel)

    init {
        table.columnModel.getColumn(0).cellRenderer = VariableCellRender(variableModel)
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val argument = argumentModel.getArgument(row)
        val component = when(argument.dataType) {
            DataType.NONE -> stringCellRenderer.getTableCellRendererComponent(table, "None", isSelected, hasFocus, row, column)
            DataType.BOOL -> stringCellRenderer.getTableCellRendererComponent(table, if(argument.value as Boolean) "True" else "False", isSelected, hasFocus, row, column)
            DataType.DICT, DataType.ARRAY -> getCellRendererComponent(argument.value as List<Variable<*>>, isSelected)
            else -> stringCellRenderer.getTableCellRendererComponent(table, argument.value.toString(), isSelected, hasFocus, row, column)
        }
        table?.setRowHeight(row, component.preferredSize.height)
        return component
    }

    private fun getCellRendererComponent(variables: List<Variable<*>>, isSelected: Boolean): Component {
        variableModel.addVariables(variables)

        if(isSelected) {
            if(variableModel.rowCount > 0)
                table.setRowSelectionInterval(0, variableModel.rowCount - 1)
            table.border = UIUtil.getTableFocusCellHighlightBorder()
        }
        else {
            table.clearSelection()
            table.border = null
        }
        return table
    }
}

class ValueTableCellEditor(private val argumentModel: ArgumentModel): AbstractTableCellEditor() {

    private val variableModel = VariableModel()
    private val table = JBTable(variableModel)
    var editEvent: MouseEvent? = null

    init {
        table.columnModel.getColumn(0).apply {
            cellRenderer = VariableCellRender(variableModel)
            cellEditor = VariableCellEditor(variableModel, this@ValueTableCellEditor)
        }
        table.setDefaultEditor(Any::class.java, StringCellEditor())
    }

    override fun isCellEditable(e: EventObject?): Boolean {
        if(e is MouseEvent) {
            val enable = e.clickCount > 1
            if(enable) {
                editEvent = e
            }
            return enable
        }
        return false
    }

    override fun stopCellEditing(): Boolean {
        editEvent = null
        return super.stopCellEditing()
    }

    override fun cancelCellEditing() {
        editEvent = null
        super.cancelCellEditing()
    }

    override fun getCellEditorValue(): Any = ""

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val argument = argumentModel.getArgument(row)
        val editor = table.getDefaultEditor(Any::class.java)
        return when(argument.dataType) {
            DataType.NONE -> editor.getTableCellEditorComponent(table, "None", isSelected, row, column)
            DataType.BOOL -> editor.getTableCellEditorComponent(table, if(argument.value as Boolean) "True" else "False", isSelected, row, column)
            DataType.DICT, DataType.ARRAY -> getCellEditorComponent(argument.value as List<Variable<*>>)
            else -> editor.getTableCellEditorComponent(table, argument.value.toString(), isSelected, row, column)
        }
    }

    private fun getCellEditorComponent(variables: List<Variable<*>>): Component {
        variableModel.addVariables(variables)
        return table
    }

}

class VariableModel : AbstractTableModel() {

    private val items = ArrayList<Pair<Variable<*>, Int>>()

    fun addVariables(variables: List<Variable<*>>) {
        items.clear()
        if(variables.isEmpty())
            items.add(Pair(VARIABLE_EMPTY, 0))
        else
            variables.forEach { addVariable(it, 0) }
        fireTableDataChanged()
    }

    private fun addVariable(variable: Variable<*>, level: Int) {
        items.add(Pair(variable, level))
        if(variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            (variable.value as List<Variable<*>>).forEach {
                addVariable(it, level + 1)
            }
        }
    }

    fun getItem(row: Int) = items[row]

    override fun getRowCount(): Int  = items.size

    override fun getColumnCount(): Int = 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = ""

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = items[rowIndex].first != VARIABLE_EMPTY

}

class VariableCellRender(private val model : VariableModel) : ColoredTableCellRenderer() {

    private val spacingMap = HashMap<Int, String>()

    private fun getSpacing(level: Int) : String {
        return spacingMap.getOrPut(level) {
            val builder = StringBuilder()
            repeat(level * 4) { builder.append(' ')}
            builder.toString()
        }
    }

    override fun customizeCellRenderer(
        table: JTable?,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val (variable, level) = model.getItem(row)
        if(variable == VARIABLE_EMPTY) {
            append("Empty Data", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
        }
        else if(variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            append(getSpacing(level))
            append("• ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            append(variable.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
        else {
            append(getSpacing(level))
            append("• ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            append(variable.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            append(": ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            when(variable.type) {
                DataType.NONE -> append("None", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                DataType.BOOL -> append(if(variable.value as Boolean) "True" else "False", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                DataType.STRING -> {
                    if((variable.value as String).isEmpty())
                        append("Empty String", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                    else
                        append(variable.value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
                else -> append(variable.value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }
    }

}

class VariableCellEditor(private val model : VariableModel, private val valueTableCellEditor: ValueTableCellEditor) : AbstractTableCellEditor() {

    private val spacingMap = HashMap<Int, String>()

    private fun getSpacing(level: Int) : String {
        return spacingMap.getOrPut(level) {
            val builder = StringBuilder()
            repeat(level * 4) { builder.append(' ')}
            builder.toString()
        }
    }

    override fun isCellEditable(e: EventObject?): Boolean {
        if(e is MouseEvent) {
            if(e.isSameEvent(valueTableCellEditor.editEvent)) {
                valueTableCellEditor.editEvent = null
                return false
            }
            return e.clickCount > 1
        }
        return false
    }

    private fun MouseEvent.isSameEvent(p: MouseEvent?): Boolean {
        if(p == null)
            return false
        return p.locationOnScreen == locationOnScreen
            && p.button == button
            && p.clickCount == clickCount
    }

    override fun getCellEditorValue(): Any = ""

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val (variable, level) = model.getItem(row)
        val builder = StringBuilder()
        builder.append(getSpacing(level))
            .append("• ")
            .append(variable.name)
        if(variable.type != DataType.DICT && variable.type != DataType.ARRAY) {
            builder.append(": ")
            when(variable.type) {
                DataType.NONE -> builder.append("None")
                DataType.BOOL -> builder.append(if(variable.value as Boolean) "True" else "False")
                else -> builder.append(variable.value.toString())
            }
        }
        val editor = table.getDefaultEditor(Any::class.java)
        return editor.getTableCellEditorComponent(table, builder.toString(), isSelected, row, column)
    }

}

class StringCellEditor : DefaultCellEditor(JBTextField()) {

    init {
        val textField = editorComponent as JBTextField
        textField.border = UIUtil.getTableFocusCellHighlightBorder()
        textField.isEditable = false
        textField.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                textField.selectAll()
            }

            override fun focusLost(e: FocusEvent?) {}

        })
    }
}

val VARIABLE_EMPTY = Variable<Any?>(type = DataType.NONE, value = null)
