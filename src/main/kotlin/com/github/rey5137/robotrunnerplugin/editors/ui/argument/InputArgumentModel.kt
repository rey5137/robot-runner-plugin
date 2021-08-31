package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.xml.InputArgument
import javax.swing.table.AbstractTableModel

class InputArgumentModel : AbstractTableModel() {

    private val items = ArrayList<HighlightHolder<InputArgument>>()

    fun add(inputs: List<HighlightHolder<InputArgument>>) {
        items.clear()
        items.addAll(inputs.sortedBy { it.value.name })
        fireTableDataChanged()
    }

    fun getItem(row: Int) = items[row]

    override fun getRowCount(): Int  = items.size

    override fun getColumnCount(): Int = 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = items[rowIndex].value.rawInput

    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = true

}