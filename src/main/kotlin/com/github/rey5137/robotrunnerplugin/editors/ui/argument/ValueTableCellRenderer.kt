package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.setHighlightBorder
import com.github.rey5137.robotrunnerplugin.editors.xml.ARGUMENT_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import icons.MyIcons
import java.awt.Component
import java.awt.Insets
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ValueTableCellRenderer(private val argumentModel: ArgumentModel) :
    TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            val item = value as ArgumentModel.Item
            val argument = item.argumentHolder.value
            setHighlightBorder(item.valueHighlightType)

            ipad = Insets(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL)
            when {
                argument == ARGUMENT_EMPTY -> append("")
                item.isFilePath -> {
                    setFocusBorderAroundIcon(true)
                    iconTextGap = 0
                    icon = if (selected) MyIcons.OpenFileWhite else MyIcons.OpenFile
                    append(argument.value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                }
                else -> when (argument.dataType) {
                    DataType.NONE -> append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    DataType.BOOL -> append(
                        if (argument.value as Boolean) "True" else "False",
                        SimpleTextAttributes.REGULAR_ATTRIBUTES
                    )
                    else -> {
                        val data = argument.value.toString()
                        if (data.isEmpty())
                            append(
                                MyBundle.message("robot.output.editor.desc.empty-string"),
                                SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES
                            )
                        else
                            append(data, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    }
                }
            }
        }
    }

    private val table = JBTable().apply {
        setDefaultRenderer(Any::class.java, VariableCellRender())
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val variableModel = argumentModel.getVariableModel(row)
        val component = if (variableModel == null)
            stringCellRenderer.getTableCellRendererComponent(
                table,
                argumentModel.getItem(row),
                isSelected,
                hasFocus,
                row,
                column
            )
        else {
            getCellRendererComponent(variableModel, isSelected, hasFocus)
        }
        table.setRowHeight(row, argumentModel.addColumnHeight(row, column, component.preferredSize.height))
        return component
    }

    private fun getCellRendererComponent(
        variableModel: VariableModel,
        isSelected: Boolean,
        hasFocus: Boolean
    ): Component {
        table.model = variableModel
        if (isSelected) {
            if (variableModel.rowCount > 0)
                table.setRowSelectionInterval(0, variableModel.rowCount - 1)
        } else {
            table.clearSelection()
        }
        table.border = if (hasFocus) UIUtil.getTableFocusCellHighlightBorder() else null
        return table
    }

    companion object {

        val PADDING_HORIZONTAL = JBUIScale.scale(1)
        val PADDING_VERTICAL = JBUIScale.scale(2)

        fun isIconClicked(x: Int): Boolean {
            return x >= PADDING_HORIZONTAL && x < PADDING_HORIZONTAL + MyIcons.OpenFile.iconWidth
        }
    }
}
