package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.xml.InputArgument
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.AbstractTableCellEditor
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JTable

class InputTableCellEditor(private val argumentModel: ArgumentModel): AbstractTableCellEditor() {

    private val inputArgumentModel = InputArgumentModel()
    var editEvent: MouseEvent? = null

    private val table = JBTable(inputArgumentModel).apply {
        setDefaultRenderer(Any::class.java, InputArgumentCellRender())
        setDefaultEditor(Any::class.java, InputArgumentCellEditor(this@InputTableCellEditor))
    }

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
        val inputHolders = argumentModel.getInputArgumentHolders(row)
        val editor = table.getDefaultEditor(Any::class.java)
        return if(inputHolders.size == 1) {
            val input = inputHolders[0].value
            if(input.name == null)
                editor.getTableCellEditorComponent(table, input.value, isSelected, row, column)
            else
                editor.getTableCellEditorComponent(table, "${input.name}: ${input.value}", isSelected, row, column)
        } else
            getCellRendererComponent(inputHolders)
    }

    private fun getCellRendererComponent(inputs: List<HighlightHolder<InputArgument>>): Component {
        inputArgumentModel.add(inputs)
        return table
    }

}