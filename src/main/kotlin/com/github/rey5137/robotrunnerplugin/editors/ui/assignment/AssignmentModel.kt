package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightInfo
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.VariableModel
import com.github.rey5137.robotrunnerplugin.editors.ui.match
import com.github.rey5137.robotrunnerplugin.editors.ui.toHighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import javax.swing.table.AbstractTableModel

class AssignmentModel : AbstractTableModel() {

    private var items: List<Item> = emptyList()

    fun setAssignments(assignments: List<Assignment<*>>, highlightInfo: HighlightInfo?) {
        items = assignments.map {
            val fullName = it.getFullName()
            Item(
                assignmentHolder = it.toHighlightHolder(highlightInfo.match(fullName)),
                assigmentValue = fullName,
                variableModel = if (it.dataType == DataType.DICT || it.dataType == DataType.ARRAY)
                    VariableModel().apply {
                        setVariables(
                            it.name.ifEmpty { "Assignment" },
                            it.dataType,
                            it.value as List<Variable<*>>,
                            highlightInfo
                        )
                    }
                else
                    null,
                isFilePath = it.isFilePath(),
            )
        }
        fireTableDataChanged()
    }

    fun getAssignmentHolder(rowIndex: Int) = items[rowIndex].assignmentHolder

    fun getVariableModel(rowIndex: Int) = items[rowIndex].variableModel

    fun getItem(rowIndex: Int) = items[rowIndex]

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 2

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = when (columnIndex) {
        INDEX_ASSIGNMENT -> items[rowIndex].assignmentHolder.value.name.isNotEmpty()
        INDEX_VALUE -> items[rowIndex].assignmentHolder.value.hasValue
        else -> true
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when (columnIndex) {
        INDEX_ASSIGNMENT -> items[rowIndex].assigmentValue
        INDEX_VALUE -> ""
        else -> null
    }

    private fun Assignment<*>.getFullName() = when (assignmentType) {
        AssignmentType.SINGLE -> "$ARG_SINGLE$ARG_NAME_START$name$ARG_NAME_END"
        AssignmentType.ARRAY -> "$ARG_ARRAY$ARG_NAME_START$name$ARG_NAME_END"
    }

    override fun getColumnName(column: Int): String = when (column) {
        INDEX_ASSIGNMENT -> MyBundle.message("robot.output.editor.label.assignment")
        INDEX_VALUE -> MyBundle.message("robot.output.editor.label.value")
        else -> ""
    }

    companion object {
        const val INDEX_ASSIGNMENT = 0
        const val INDEX_VALUE = 1
    }

    data class Item(
        val assignmentHolder: HighlightHolder<Assignment<*>>,
        val assigmentValue: String,
        val variableModel: VariableModel?,
        val isFilePath: Boolean = false,
    )
}
