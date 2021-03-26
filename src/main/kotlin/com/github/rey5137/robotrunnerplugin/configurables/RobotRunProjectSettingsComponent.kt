package com.github.rey5137.robotrunnerplugin.configurables

import com.github.rey5137.robotrunnerplugin.runconfigurations.ROBOT_RUN_CONFIGURATION_TYPE_ID
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationFactory
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfigurationType
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.ui.BooleanTableCellRenderer
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import java.awt.FontMetrics
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.TableColumn


class RobotRunProjectSettingsComponent(private val project: Project) {

    val mainPanel by lazy { buildMainPanel() }
    private val settingModel = RobotRunSettingModel()
    private val table by lazy { JBTable(settingModel) }

    fun resetSettings(settingMap: Map<String, RobotRunSetting>) {
        settingModel.reset(settingMap)
    }

    fun getSettings() = settingModel.getSettingMap()

    private fun buildMainPanel(): JPanel {
        table.setShowGrid(false)
        table.tableHeader.resizingAllowed = false
        table.tableHeader.reorderingAllowed = false
        table.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val editor = table.getDefaultEditor(Any::class.java) as DefaultCellEditor
        editor.clickCountToStart = 1
        DumbAwareAction.create { editSetting() }
            .registerCustomShortcutSet(CustomShortcutSet(KeyEvent.VK_SPACE), table)

        val nameColumn = table.columnModel.getColumn(RobotRunSettingModel.INDEX_NAME)
        nameColumn.cellRenderer = object : ColoredTableCellRenderer() {
            override fun customizeCellRenderer(table: JTable, value: Any?, selected: Boolean, hasFocus: Boolean, row: Int, column: Int) {
                val configuration = (value as RunnerAndConfigurationSettings).configuration
                icon = configuration.icon
                append(configuration.name)
            }
        }
        setUpCheckBoxColumn(table, table.columnModel.getColumn(RobotRunSettingModel.INDEX_TEST_SUITE))
        setUpCheckBoxColumn(table, table.columnModel.getColumn(RobotRunSettingModel.INDEX_TEST_CASE))
        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount >= 2)
                    editRunConfiguration()
                else
                    editSetting()
            }
        })
        populateSettingModel()

        val decorator = ToolbarDecorator.createDecorator(table)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .setAddAction {
                val configurationType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.first { it.id == ROBOT_RUN_CONFIGURATION_TYPE_ID }
                val factory = RobotRunConfigurationFactory(configurationType as RobotRunConfigurationType)
                ApplicationManager.getApplication().invokeLater(
                    {
                        if (EditConfigurationsDialog(project, factory).showAndGet()) {
                            ApplicationManager.getApplication().invokeLater({
                                RunManager.getInstance(project).selectedConfiguration?.let { addConfiguration(it) }
                            }, project.disposed)
                        }
                    }, project.disposed)
            }
        val panel = decorator.createPanel()
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel("Template Configuration to use with \"Run Robot ..\" actions"), 1)
            .addComponentFillVertically(panel, 0)
            .panel
    }

    private fun populateSettingModel() {
        val settingMap = RobotRunProjectSettingsState.getInstance(project).settingMap
        RunManager.getInstance(project).getConfigurationSettingsList(RobotRunConfigurationType::class.java)
            .filter { !it.isTemporary }
            .forEach { runConfigurationsSetting ->
                val setting = settingMap[runConfigurationsSetting.uniqueID] ?: RobotRunSetting()
                settingModel.addRow(setting, runConfigurationsSetting)
            }
    }

    private fun addConfiguration(runConfigurationsSetting: RunnerAndConfigurationSettings) {
        val setting =  RobotRunSetting(
            testSuiteEnable = false,
            testCaseEnable = false,
        )
        settingModel.addRow(setting, runConfigurationsSetting)
    }

    private fun editSetting() {
        val row: Int = table.selectedRow
        val col: Int = table.selectedColumn
        if (row >= 0 && settingModel.isCellEditable(row, col))
            settingModel.setValueAt(!(table.getValueAt(row, col) as Boolean), row, col)
    }

    private fun setUpCheckBoxColumn(table: JBTable, column: TableColumn) {
        val header = column.headerValue.toString()
        val fm: FontMetrics = table.getFontMetrics(table.tableHeader.font)
        val width = -JBCheckBox().preferredSize.width + fm.stringWidth(header + "ww")
        TableUtil.setupCheckboxColumn(column, width)
        column.cellRenderer = BooleanTableCellRenderer()
    }

    private fun editRunConfiguration() {
        val row: Int = table.selectedRow
        if (row < 0) return
        val selected: RunnerAndConfigurationSettings = settingModel.getConfiguration(row) ?: return
        val runManager = RunManager.getInstance(project)
        val was = runManager.selectedConfiguration
        try {
            runManager.selectedConfiguration = selected
            if(EditConfigurationsDialog(project).showAndGet()) {
                runManager.selectedConfiguration?.let { settingModel.updateRow(row, it) }
            }
        } finally {
            runManager.selectedConfiguration = was
        }
        settingModel.fireTableDataChanged()
    }

    fun getPreferredFocusedComponent(): JComponent {
        return mainPanel
    }

}