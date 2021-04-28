package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.Variable
import javax.swing.table.AbstractTableModel

class VariableModel : AbstractTableModel() {

    private val items = ArrayList<Pair<Variable<*>, Int>>()

    fun addVariables(variables: List<Variable<*>>) {
        items.clear()
        if(variables.isEmpty())
            items.add(Pair(VARIABLE_EMPTY, 0))
        else
            variables.forEach { addVariable(it, 0) }
        fireTableDataChanged()
    }

    private fun addVariable(variable: Variable<*>, level: Int) {
        items.add(Pair(variable, level))
        if(variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            val variables = (variable.value as List<Variable<*>>).sortedBy { it.name }
            variables.forEach { addVariable(it, level + 1) }
        }
    }

    fun getItem(row: Int) = items[row]

    override fun getRowCount(): Int  = items.size

    override fun getColumnCount(): Int = 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = ""

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = items[rowIndex].first != VARIABLE_EMPTY

}