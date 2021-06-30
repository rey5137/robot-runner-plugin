package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.Variable
import java.io.File
import javax.swing.table.AbstractTableModel

class VariableModel : AbstractTableModel() {

    private val items = ArrayList<Item>()

    private val allItems = ArrayList<Item>()

    fun setVariables(variables: List<Variable<*>>) {
        allItems.clear()
        if (variables.isEmpty())
            allItems.add(
                Item(
                    index = allItems.size,
                    variable = VARIABLE_EMPTY,
                    level = 0,
                    isLeaf = true,
                    isExpanded = true
                )
            )
        else
            variables.forEach { addVariable(it, 0) }
        items.clear()
        items.addAll(allItems)
        fireTableDataChanged()
    }

    private fun addVariable(variable: Variable<*>, level: Int) {
        if (variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            var variables = (variable.value as List<Variable<*>>)
            if (variable.type == DataType.DICT && !variable.childOrdered)
                variables = variables.sortedBy { it.name }
            allItems.add(
                Item(
                    index = allItems.size,
                    variable = variable,
                    level = level,
                    isLeaf = variables.isEmpty(),
                    isExpanded = true,
                    isFilePath = variable.isFilePath()
                )
            )
            variables.forEach { addVariable(it, level + 1) }
        } else
            allItems.add(
                Item(
                    index = allItems.size,
                    variable = variable,
                    level = level,
                    isLeaf = true,
                    isExpanded = true,
                    isFilePath = variable.isFilePath()
                )
            )
    }

    fun collapseAt(row: Int) {
        if (items[row].isLeaf)
            return
        items[row].isExpanded = false
        val level = items[row].level
        while (row < items.size - 1 && items[row + 1].level > level) {
            if(!items[row + 1].isLeaf)
                items[row + 1].isExpanded = false
            items.removeAt(row + 1)
        }
    }

    fun expandAt(row: Int) {
        if (items[row].isLeaf)
            return
        items[row].isExpanded = true
        var index = items[row].index + 1
        val level = items[row].level
        var row = row + 1

        while (index < allItems.size && allItems[index].level > level) {
            val item = allItems[index]
            items.add(row, item)
            row++
            index++
            if (!item.isLeaf && !item.isExpanded) {
                while (index < allItems.size && allItems[index].level > item.level) {
                    index++
                }
            }
        }
    }

    fun getItem(row: Int) = items[row]

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = ""

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = items[rowIndex].variable != VARIABLE_EMPTY

    data class Item(
        val variable: Variable<*>,
        val level: Int,
        val isLeaf: Boolean,
        var isExpanded: Boolean,
        val index: Int,
        val isFilePath: Boolean = false
    )
}