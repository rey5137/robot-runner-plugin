package com.github.rey5137.robotrunnerplugin.configurables

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.util.ui.EditableModel
import javax.swing.table.AbstractTableModel

class RobotRunSettingModel: AbstractTableModel(), EditableModel {

    private val items = mutableListOf<Pair<String, RobotRunSetting>>()
    private val configurationMap = hashMapOf<String, RunnerAndConfigurationSettings>()

    fun addRow(setting: RobotRunSetting, configuration: RunnerAndConfigurationSettings) {
        items.add(Pair(configuration.uniqueID, setting))
        configurationMap[configuration.uniqueID] = configuration
        fireTableDataChanged()
    }

    fun updateRow(index: Int, configuration: RunnerAndConfigurationSettings) {
        val oldId = items[index].first
        items[index] = items[index].copy(first = configuration.uniqueID)
        configurationMap.remove(oldId)
        configurationMap[configuration.uniqueID] = configuration
        fireTableDataChanged()
    }

    fun reset(settingMap: Map<String, RobotRunSetting>) {
        items.forEachIndexed { index, item ->
            val configurationId = item.first
            items[index] = Pair(configurationId, settingMap[configurationId] ?: RobotRunSetting())
        }
        fireTableDataChanged()
    }

    fun getSettingMap(): LinkedHashMap<String, RobotRunSetting> {
        val map = LinkedHashMap<String, RobotRunSetting>()
        items.filter { it.second.testSuiteEnable || it.second.testCaseEnable }
            .forEach { map[it.first] = it.second }
        return map
    }

    fun getConfiguration(index: Int) = configurationMap[items[index].first]

    override fun getRowCount(): Int = items.size

    override fun getColumnCount(): Int = 3

    override fun getColumnName(column: Int): String = when(column) {
        INDEX_NAME -> MyBundle.message("robot.run.settings.column.name")
        INDEX_TEST_SUITE -> MyBundle.message("robot.run.settings.column.test-suite")
        INDEX_TEST_CASE -> MyBundle.message("robot.run.settings.column.test-case")
        else -> ""
    }

    override fun getColumnClass(columnIndex: Int): Class<*> = when(columnIndex) {
        INDEX_NAME -> RunnerAndConfigurationSettings::class.java
        INDEX_TEST_SUITE -> Boolean::class.java
        INDEX_TEST_CASE -> Boolean::class.java
        else -> Any::class.java
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when(columnIndex) {
        INDEX_NAME -> configurationMap[items[rowIndex].first]
        INDEX_TEST_SUITE -> items[rowIndex].second.testSuiteEnable
        INDEX_TEST_CASE -> items[rowIndex].second.testCaseEnable
        else -> null
    }

    override fun setValueAt(value: Any, rowIndex: Int, columnIndex: Int) {
        when(columnIndex) {
            INDEX_TEST_SUITE -> {
                items[rowIndex] = items[rowIndex].copy(second = items[rowIndex].second.copy(testSuiteEnable = value as Boolean))
                fireTableRowsUpdated(rowIndex, rowIndex + 1)
            }
            INDEX_TEST_CASE -> {
                items[rowIndex] = items[rowIndex].copy(second = items[rowIndex].second.copy(testCaseEnable = value as Boolean))
                fireTableRowsUpdated(rowIndex, rowIndex + 1)
            }
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean =
        columnIndex != INDEX_NAME

    override fun removeRow(idx: Int) {
        val item = items.removeAt(idx)
        configurationMap.remove(item.first)
        fireTableDataChanged()
    }

    override fun addRow() {}

    override fun exchangeRows(oldIndex: Int, newIndex: Int) {}

    override fun canExchangeRows(oldIndex: Int, newIndex: Int) = false

    companion object {
        const val INDEX_NAME = 0
        const val INDEX_TEST_SUITE = 1
        const val INDEX_TEST_CASE = 2
    }
}