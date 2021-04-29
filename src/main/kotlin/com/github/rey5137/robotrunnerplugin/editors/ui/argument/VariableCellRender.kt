package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTable

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
            append(" = ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            when(variable.type) {
                DataType.NONE -> append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                DataType.BOOL -> append(if(variable.value as Boolean) "True" else "False",
                    SimpleTextAttributes.REGULAR_ATTRIBUTES
                )
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