package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightInfo
import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightType
import com.github.rey5137.robotrunnerplugin.editors.ui.match
import com.github.rey5137.robotrunnerplugin.editors.xml.DataType
import com.github.rey5137.robotrunnerplugin.editors.xml.VARIABLE_EMPTY
import com.github.rey5137.robotrunnerplugin.editors.xml.Variable
import javax.swing.table.AbstractTableModel

class VariableModel : AbstractTableModel() {

    private val items = ArrayList<Item>()

    private val allItems = ArrayList<Item>()

    fun setVariables(rootName: String, rootDataType: DataType, variables: List<Variable<*>>, highlightInfo: HighlightInfo?) {
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
        else {
            val item = Item(
                index = allItems.size,
                variable = Variable(
                    name = rootName,
                    value = null,
                    type = rootDataType
                ),
                level = 0,
                isLeaf = false,
                isExpanded = true,
            )
            allItems.add(item)
            variables.forEach {
                val childHighlight = addVariable(it, 1, highlightInfo)
                if (childHighlight != HighlightType.UNMATCHED)
                    item.highlightType = HighlightType.CONTAINED
            }
        }
        items.clear()
        items.addAll(allItems)
        fireTableDataChanged()
    }

    private fun addVariable(variable: Variable<*>, level: Int, highlightInfo: HighlightInfo?): HighlightType {
        if (variable.type == DataType.DICT || variable.type == DataType.ARRAY) {
            var variables = (variable.value as List<Variable<*>>)
            if (variable.type == DataType.DICT && !variable.childOrdered)
                variables = variables.sortedBy { it.name }
            val item = Item(
                index = allItems.size,
                variable = variable,
                level = level,
                isLeaf = variables.isEmpty(),
                isExpanded = true,
                isFilePath = variable.isFilePath(),
                highlightType = if (highlightInfo.match(variable.name)) HighlightType.MATCHED else HighlightType.UNMATCHED
            )
            allItems.add(item)
            variables.forEach {
                val childHighlight = addVariable(it, level + 1, highlightInfo)
                if (item.highlightType == HighlightType.UNMATCHED && childHighlight != HighlightType.UNMATCHED)
                    item.highlightType = HighlightType.CONTAINED
            }
            return item.highlightType
        } else {
            val item = Item(
                index = allItems.size,
                variable = variable,
                level = level,
                isLeaf = true,
                isExpanded = true,
                isFilePath = variable.isFilePath(),
                highlightType = if (highlightInfo.match("${variable.name} = ${variable.valueAsString()}"))
                    HighlightType.MATCHED
                else
                    HighlightType.UNMATCHED
            )
            allItems.add(item)
            return item.highlightType
        }
    }

    fun collapseAt(row: Int) {
        if (items[row].isLeaf)
            return
        items[row].isExpanded = false
        val level = items[row].level
        while (row < items.size - 1 && items[row + 1].level > level) {
            if (!items[row + 1].isLeaf)
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

    private fun Variable<*>.valueAsString(): String {
        return when (type) {
            DataType.NONE -> "None"
            DataType.BOOL -> if (value as Boolean) "True" else "False"
            else -> value.toString()
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
        val isFilePath: Boolean = false,
        var highlightType: HighlightType = HighlightType.UNMATCHED,
    )
}
