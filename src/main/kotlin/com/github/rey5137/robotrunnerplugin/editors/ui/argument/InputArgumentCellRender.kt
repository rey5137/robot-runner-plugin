package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.setHighlightBorder
import com.github.rey5137.robotrunnerplugin.editors.xml.INPUT_EMPTY
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.awt.Insets
import javax.swing.JTable

class InputArgumentCellRender : ColoredTableCellRenderer() {

    override fun customizeCellRenderer(
        table: JTable,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val inputHolder = (table.model as InputArgumentModel).getItem(row)
        setHighlightBorder(inputHolder.highlight)

        val input = inputHolder.value
        ipad = Insets(VariableCellRender.PADDING_VERTICAL, VariableCellRender.PADDING_HORIZONTAL, VariableCellRender.PADDING_VERTICAL, VariableCellRender.PADDING_HORIZONTAL)
        when {
            input == INPUT_EMPTY -> append(MyBundle.message("robot.output.editor.desc.no-input"), SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
            input.name == null -> append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            else -> {
                append(input.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                append(" = ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                append(input.value, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }
    }
}
