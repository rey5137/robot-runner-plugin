package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableCellRender
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableModel
import com.github.rey5137.robotrunnerplugin.editors.xml.*
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

class ValueTableCellRenderer(private val assignmentModel: AssignmentModel) :
    TableCellRenderer {

    private val stringCellRenderer = object : ColoredTableCellRenderer() {
        override fun customizeCellRenderer(
            table: JTable?,
            value: Any?,
            selected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ) {
            val item = value as AssignmentModel.Item
            val assignment = item.assignmentHolder.value
            ipad = Insets(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL)
            if (!assignment.hasValue)
                append("")
            else if (item.isFilePath) {
                setFocusBorderAroundIcon(true)
                iconTextGap = 0
                icon = if (selected) MyIcons.OpenFileWhite else MyIcons.OpenFile
                append(assignment.value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES)
            } else
                when (assignment.dataType) {
                    DataType.NONE -> append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    DataType.BOOL -> append(
                        if (assignment.value as Boolean) "True" else "False",
                        SimpleTextAttributes.REGULAR_ATTRIBUTES
                    )
                    else -> {
                        val data = assignment.value.toString()
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

    private val table = JBTable().apply {
        setDefaultRenderer(Any::class.java, VariableCellRender())
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val variableModel = assignmentModel.getVariableModel(row)
        val component = if (variableModel == null)
            stringCellRenderer.getTableCellRendererComponent(
                table,
                assignmentModel.getItem(row),
                isSelected,
                hasFocus,
                row,
                column
            )
        else
            getCellRendererComponent(variableModel, isSelected, hasFocus)
        table?.setRowHeight(row, component.preferredSize.height)
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