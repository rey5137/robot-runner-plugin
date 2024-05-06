package com.github.rey5137.robotrunnerplugin.configurables

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.MyNotifier
import com.github.rey5137.robotrunnerplugin.runconfigurations.*
import com.google.gson.Gson
import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.EditConfigurationsDialog
import com.intellij.icons.AllIcons.ToolbarDecorator.Export
import com.intellij.icons.AllIcons.ToolbarDecorator.Import
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import java.awt.FontMetrics
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.*
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
            override fun customizeCellRenderer(
                table: JTable,
                value: Any?,
                selected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ) {
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
                val configurationType =
                    ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.first { it.id == ROBOT_RUN_CONFIGURATION_TYPE_ID }
                val factory = RobotRunConfigurationFactory(configurationType as RobotRunConfigurationType)
                ApplicationManager.getApplication().invokeLater(
                    {
                        if (EditConfigurationsDialog(project, factory).showAndGet()) {
                            ApplicationManager.getApplication().invokeLater({
                                RunManager.getInstance(project).selectedConfiguration?.let {
                                    addConfiguration(
                                        RobotRunSetting(),
                                        it
                                    )
                                }
                            }, project.disposed)
                        }
                    }, project.disposed
                )
            }
            .addExtraAction(object : DumbAwareActionButton(
                MyBundle.message("robot.run.settings.label.export-settings"),
                MyBundle.message("robot.run.settings.desc.export-settings"),
                Export
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val descriptor = FileSaverDescriptor(
                        MyBundle.message("robot.run.settings.label.export-settings"),
                        MyBundle.message("robot.run.settings.desc.export-settings"),
                        "json"
                    )
                    val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, null)
                    val fileWrapper = dialog.save(null as VirtualFile?, "robot_run_configurations.json")
                    if (fileWrapper != null)
                        exportSettings(fileWrapper.file)
                }

                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
            })
            .addExtraAction(object : DumbAwareActionButton(
                MyBundle.message("robot.run.settings.label.import-settings"),
                MyBundle.message("robot.run.settings.desc.import-settings"),
                Import
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
                    val dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null)
                    val files = dialog.choose(null)
                    if (files.isNotEmpty())
                        importSettings(File(files[0].path))
                }

                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
            })
        val panel = decorator.createPanel()
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel(MyBundle.message("robot.run.settings.label")), 1)
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

    private fun addConfiguration(setting: RobotRunSetting, runConfigurationsSetting: RunnerAndConfigurationSettings) {
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
            if (EditConfigurationsDialog(project).showAndGet()) {
                runManager.selectedConfiguration?.let { settingModel.updateRow(row, it) }
            }
        } finally {
            runManager.selectedConfiguration = was
        }
        settingModel.fireTableDataChanged()
    }

    private fun exportSettings(file: File) {
        try {
            val settings = mutableListOf<RobotRunConfigurationExportData>()
            (0 until settingModel.rowCount).forEach {
                val configuration = settingModel.getConfiguration(it)!!.configuration
                val name = configuration.name
                val options = (configuration as RobotRunConfiguration).options
                val setting = settingModel.getSetting(it)
                settings.add(buildRobotRunConfigurationExportData(name, setting, options))
            }
            val gson = Gson()
            val value = gson.toJson(RobotRunSettingExportData(settings = settings))
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file)))
            writer.write(value)
            writer.flush()
            writer.close()
            MyNotifier.notify(project, MyBundle.message("robot.run.settings.message.export-settings"), NotificationType.INFORMATION)
        } catch (ex: Exception) {
            MyNotifier.notify(project, MyBundle.message("robot.run.settings.message.error-export-settings"), NotificationType.ERROR)
        }
    }

    private fun importSettings(file: File) {
        try {
            val gson = Gson()
            val exportData =
                gson.fromJson(InputStreamReader(FileInputStream(file)), RobotRunSettingExportData::class.java)
            exportData.settings.forEach { data ->
                val runManager = RunManagerEx.getInstanceEx(project)
                val configurationType =
                    ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.first { it.id == ROBOT_RUN_CONFIGURATION_TYPE_ID } as RobotRunConfigurationType
                if (runManager.findConfigurationByTypeAndName(configurationType, data.name) == null) {
                    val runConfiguration =
                        RobotRunConfiguration(project, configurationType.configurationFactory, data.name)
                    data.setRobotRunConfigurationOptions(runConfiguration.options)
                    val newRunConfigurationSetting =
                        runManager.createConfiguration(runConfiguration, runConfiguration.factory!!)
                    runManager.addConfiguration(newRunConfigurationSetting)
                    val setting = RobotRunSetting(
                        testSuiteEnable = data.testSuiteEnable,
                        testCaseEnable = data.testCaseEnable
                    )
                    addConfiguration(setting, newRunConfigurationSetting)
                }
            }
            MyNotifier.notify(project, MyBundle.message("robot.run.settings.message.import-settings"), NotificationType.INFORMATION)
        } catch (ex: Exception) {
            MyNotifier.notify(project, MyBundle.message("robot.run.settings.message.error-import-settings"), NotificationType.ERROR)
        }
    }

    fun getPreferredFocusedComponent(): JComponent {
        return mainPanel
    }

}