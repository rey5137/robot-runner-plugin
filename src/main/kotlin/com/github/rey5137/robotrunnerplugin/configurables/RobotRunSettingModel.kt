package com.github.rey5137.robotrunnerplugin.configurables

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.util.ui.EditableModel
import javax.swing.table.AbstractTableModel

class RobotRunSettingModel: AbstractTableModel(), EditableModel {

    private val settings = mutableListOf<RobotRunSetting>()
    private val configurationMap = mutableMapOf<Int, RunnerAndConfigurationSettings>()

    fun addRow(setting: RobotRunSetting, configuration: RunnerAndConfigurationSettings) {
        settings.add(setting)
        configurationMap[setting.runConfigurationId] = configuration
        fireTableDataChanged()
    }

    fun reset(newSettings: List<RobotRunSetting>) {
        settings.forEachIndexed { index, robotRunSetting ->
            settings[index] = newSettings.find { it.runConfigurationId == robotRunSetting.runConfigurationId }
                ?: robotRunSetting.copy(fileEnable = false, textEnable = false)
        }
        fireTableDataChanged()
    }

    fun getSettings() = settings.filter { it.fileEnable || it.textEnable }

    fun getConfiguration(index: Int) = configurationMap[settings[index].runConfigurationId]

    override fun getRowCount(): Int = settings.size

    override fun getColumnCount(): Int = 3

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_NAME -> "Run Configuration"
        INDEX_FILE -> "File"
        INDEX_TEXT -> "Text"
        else -> ""
    }

    override fun getColumnClass(columnIndex: Int): Class<*> = when(columnIndex) {
        INDEX_NAME -> RunnerAndConfigurationSettings::class.java
        INDEX_FILE -> Boolean::class.java
        INDEX_TEXT -> Boolean::class.java
        else -> Any::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_NAME -> configurationMap[settings[rowIndex].runConfigurationId]
        INDEX_FILE -> settings[rowIndex].fileEnable
        INDEX_TEXT -> settings[rowIndex].textEnable
        else -> null
    }

    override fun setValueAt(value: Any, rowIndex: Int, columnIndex: Int) {
        when(columnIndex) {
            INDEX_FILE -> {
                settings[rowIndex] = settings[rowIndex].copy(fileEnable = value as Boolean)
                fireTableRowsUpdated(rowIndex, rowIndex + 1)
            }
            INDEX_TEXT -> {
                settings[rowIndex] = settings[rowIndex].copy(textEnable = value as Boolean)
                fireTableRowsUpdated(rowIndex, rowIndex + 1)
            }
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
        columnIndex != INDEX_NAME

    override fun removeRow(idx: Int) {
        settings.removeAt(idx)
        fireTableDataChanged()
    }

    override fun addRow() {}

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {}

    override fun canExchangeRows(oldIndex: Int, newIndex: Int) = false

    companion object {
        const val INDEX_NAME = 0
        const val INDEX_FILE = 1
        const val INDEX_TEXT = 2
    }
}