package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.INPUT_EMPTY
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTable

class InputArgumentCellRender(private val model : InputArgumentModel) : ColoredTableCellRenderer() {

    override fun customizeCellRenderer(
        table: JTable?,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val input = model.getItem(row)
        when {
            input == INPUT_EMPTY -> {
                append("No input", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
            }
            input.name == null -> {
                append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
            else -> {
                append(input.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                append(" = ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }
    }

}