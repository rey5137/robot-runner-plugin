package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.scale.JBUIScale
import icons.MyIcons
import java.awt.Insets
import javax.swing.JTable

class VariableCellRender : ColoredTableCellRenderer() {
    
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
            ipad = Insets(PADDING_VERTICAL, PADDING_HORIZONTAL + PADDING_LEVEL * level + AllIcons.General.ArrowDown.iconWidth, PADDING_VERTICAL, PADDING_HORIZONTAL)
            append("Empty Data", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
        }
        else if(variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            ipad = Insets(PADDING_VERTICAL, PADDING_HORIZONTAL+ PADDING_LEVEL * level, PADDING_VERTICAL, PADDING_HORIZONTAL)
            setFocusBorderAroundIcon(true)
            iconTextGap = 0
            icon = if(isExpanded)
                if (selected) MyIcons.ArrowDownWhite else MyIcons.ArrowDown
            else
                if (selected) MyIcons.ArrowRightWhite else MyIcons.ArrowRight
            append(variable.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
        else {
            ipad = Insets(PADDING_VERTICAL, PADDING_HORIZONTAL + PADDING_LEVEL * level + MyIcons.ArrowDown.iconWidth, PADDING_VERTICAL, PADDING_HORIZONTAL)
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

    companion object {

        val PADDING_HORIZONTAL = JBUIScale.scale(1)
        val PADDING_VERTICAL = JBUIScale.scale(2)
        val PADDING_LEVEL = MyIcons.ArrowDown.iconWidth

        fun isArrowClicked(x: Int, level: Int) : Boolean {
            return x >= PADDING_HORIZONTAL + PADDING_LEVEL * level && x < PADDING_HORIZONTAL + PADDING_LEVEL * level + MyIcons.ArrowDown.iconWidth
        }

    }
}