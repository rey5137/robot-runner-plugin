package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.ui.setHighlightBorder
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JTable

class ArgumentTableCellRenderer(private val model: ArgumentModel) : ColoredTableCellRenderer() {

    override fun customizeCellRenderer(
        table: JTable,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val argumentHolder = model.getArgumentHolder(row)
        setHighlightBorder(argumentHolder.highlight)

        if (argumentHolder.value.name.isEmpty())
            append("")
        else {
            val sign = when (argumentHolder.value.argumentType) {
                ArgumentType.SINGLE -> ARG_SINGLE
                ArgumentType.DICT -> ARG_DICT
                ArgumentType.ARRAY -> ARG_ARRAY
                else -> ""
            }.toString()
            if (sign.isNotEmpty()) {
                append(sign, SimpleTextAttributes.GRAY_ATTRIBUTES)
                append(ARG_NAME_START.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES)
            }
            append(argumentHolder.value.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            if (sign.isNotEmpty())
                append(ARG_NAME_END.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES)
        }
        table.setRowHeight(row, model.addColumnHeight(row, column, this.preferredSize.height))
    }
}
