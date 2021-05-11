package com.github.rey5137.robotrunnerplugin.editors.ui.assignment

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.*
import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class ValueTableCellEditor(private val parentTable: JBTable, private val assignmentModel: AssignmentModel): AbstractTableCellEditor(), VariableCellEditor.EditEventProvider {

    private val table = JBTable().apply {
        setDefaultRenderer(Any::class.java, VariableCellRender())
        setDefaultEditor(Any::class.java, VariableCellEditor(this@ValueTableCellEditor))
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                e.isArrowClicked { _, _ -> clearSelection() }
            }

            override fun mouseClicked(e: MouseEvent) {
                e.isArrowClicked { model, row ->
                    if(model.getItem(row).isExpanded)
                        model.collapseAt(row)
                    else
                        model.expandAt(row)
                    parentTable.cellEditor.cancelCellEditing()
                    parentTable.clearSelection()
                }
            }

            private fun MouseEvent.isArrowClicked(func: (model: VariableModel, row: Int) -> Unit) {
                val variableModel = model as VariableModel
                val row = rowAtPoint(point)
                val item = variableModel.getItem(row)
                if (!item.isLeaf && VariableCellRender.isArrowClicked(point.x, item.level))
                    func(variableModel, row)
            }
        })
    }
    override var editEvent: MouseEvent? = null

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
        val assignment = assignmentModel.getAssignment(row)
        val editor = table.getDefaultEditor(Any::class.java)
        return when(assignment.dataType) {
            DataType.NONE -> editor.getTableCellEditorComponent(table, "None", isSelected, row, column)
            DataType.BOOL -> editor.getTableCellEditorComponent(table, if(assignment.value as Boolean) "True" else "False", isSelected, row, column)
            DataType.DICT, DataType.ARRAY -> this.table.apply { model = assignmentModel.getVariableModel(row) }
            else -> editor.getTableCellEditorComponent(table, assignment.value.toString(), isSelected, row, column)
        }
    }

}