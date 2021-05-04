package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.intellij.ui.table.JBTable
import javax.swing.JTable

class ArgumentTable(private val argumentModel: ArgumentModel) : JBTable(argumentModel) {

    init {
        cellSelectionEnabled = true
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
        columnModel.getColumn(ArgumentModel.INDEX_ARGUMENT).apply {
            cellRenderer = ArgumentTableCellRenderer(argumentModel)
        }
        columnModel.getColumn(ArgumentModel.INDEX_INPUT).apply {
            cellRenderer = InputTableCellRenderer(argumentModel)
            cellEditor = InputTableCellEditor(argumentModel)
        }
        columnModel.getColumn(ArgumentModel.INDEX_VALUE).apply {
            cellRenderer = ValueTableCellRenderer(argumentModel)
            cellEditor = ValueTableCellEditor(argumentModel)
        }
        setDefaultEditor(Any::class.java, StringCellEditor())
    }
}