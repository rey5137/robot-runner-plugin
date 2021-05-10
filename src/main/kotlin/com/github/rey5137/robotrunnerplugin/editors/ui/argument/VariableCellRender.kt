package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.LayeredIcon
import com.intellij.ui.SimpleTextAttributes
import java.awt.Insets
import javax.swing.JTable

class VariableCellRender(private val levelPadding: Int) : ColoredTableCellRenderer() {

    override fun customizeCellRenderer(
        table: JTable,
        value: Any?,
        selected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ) {
        val (variable, level, _, isExpanded) = (table.model as VariableModel).getItem(row)
        if(variable == VARIABLE_EMPTY) {
            ipad = Insets(0, levelPadding * level + AllIcons.General.ArrowDown.iconWidth, 0, 0)
            append("Empty Data", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
        }
        else if(variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            ipad = Insets(0, levelPadding * level, 0, 0)
            setFocusBorderAroundIcon(true)
            iconTextGap = 0
            icon = if(isExpanded)
                AllIcons.General.ArrowDown
            else
                AllIcons.General.ArrowRight
            append(variable.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
        else {
            ipad = Insets(0, levelPadding * level + AllIcons.General.ArrowDown.iconWidth, 0, 0)
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