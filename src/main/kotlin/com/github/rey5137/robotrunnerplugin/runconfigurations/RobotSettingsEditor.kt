package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.Disposer
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableModel

class RobotSettingsEditor : SettingsEditor<RobotRunConfiguration>() {

    private val mainPanel by lazy { buildMainPanel() }

    private lateinit var sdkComboBox: ComboBox<Sdk>
    private lateinit var outputDirTextField: TextFieldWithBrowseButton
    private lateinit var outputFileTextField: TextFieldWithBrowseButton
    private lateinit var logFileTextField: TextFieldWithBrowseButton
    private lateinit var logTitleTextField: JBTextField
    private lateinit var reportFileTextField: TextFieldWithBrowseButton
    private lateinit var reportTitleTextField: JBTextField
    private lateinit var timestampOutputsCheckBox: JBCheckBox
    private lateinit var splitLogsCheckBox: JBCheckBox
    private lateinit var logLevelBox: ComboBox<String>
    private lateinit var defaultLogLevelBox: ComboBox<String>
    private lateinit var dryRunCheckBox: JBCheckBox
    private lateinit var runEmptySuiteCheckBox: JBCheckBox

    private val suitePathModel = DefaultListModel<String>()
    private val testNameModel = DefaultListModel<String>()
    private val suiteNameModel = DefaultListModel<String>()
    private val includeTagModel = DefaultListModel<String>()
    private val excludeTagModel = DefaultListModel<String>()
    private val variablesModel = DefaultTableModel()

    private val pythonSdks = ProjectJdkTable.getInstance().allJdks.filter { it.sdkType.name == "Python SDK" }

    override fun resetEditorFrom(configuration: RobotRunConfiguration) {
        val options = configuration.options
        sdkComboBox.selectedItem = options.sdkHomePath.toSdk()
        suitePathModel.clear()
        suitePathModel.addAll(options.suitePaths)
        testNameModel.clear()
        testNameModel.addAll(options.testNames)
        suiteNameModel.clear()
        suiteNameModel.addAll(options.suiteNames)
        includeTagModel.clear()
        includeTagModel.addAll(options.includeTags)
        excludeTagModel.clear()
        excludeTagModel.addAll(options.excludeTags)
        outputDirTextField.text = options.outputDirPath ?: ""
        outputFileTextField.text = options.outputFilePath ?: ""
        logFileTextField.text = options.logFilePath ?: ""
        logTitleTextField.text = options.logTitle ?: ""
        reportFileTextField.text = options.reportFilePath ?: ""
        reportTitleTextField.text = options.reportTitle ?: ""
        timestampOutputsCheckBox.isSelected = options.timestampOutputs
        splitLogsCheckBox.isSelected = options.splitLog
        ((variablesModel.rowCount -1) downTo 0).forEach { variablesModel.removeRow(it) }
        options.variables.forEach { (key, value) -> variablesModel.addRow(arrayOf(key, value)) }
        logLevelBox.selectedItem = options.logLevel
        defaultLogLevelBox.selectedItem = options.defaultLogLevel
        dryRunCheckBox.isSelected = options.dryRun
        runEmptySuiteCheckBox.isSelected = options.runEmptySuite
    }

    override fun applyEditorTo(configuration: RobotRunConfiguration) {
        val options = configuration.options
        options.sdkHomePath = (sdkComboBox.selectedItem as Sdk?)?.homePath
        options.suitePaths = suitePathModel.elements().toList().toMutableList()
        options.testNames = testNameModel.elements().toList().toMutableList()
        options.suiteNames = suiteNameModel.elements().toList().toMutableList()
        options.includeTags = includeTagModel.elements().toList().toMutableList()
        options.excludeTags = excludeTagModel.elements().toList().toMutableList()
        options.outputDirPath = outputDirTextField.text
        options.outputFilePath = outputFileTextField.text
        options.logFilePath = logFileTextField.text
        options.logTitle = logTitleTextField.text
        options.reportFilePath = reportFileTextField.text
        options.reportTitle = reportTitleTextField.text
        options.timestampOutputs = timestampOutputsCheckBox.isSelected
        options.splitLog = splitLogsCheckBox.isSelected
        options.variables.clear()
        (0 until variablesModel.rowCount).forEach {
            val key = variablesModel.getValueAt(it, 0) as String?
            val value = variablesModel.getValueAt(it, 1) as String?
            if(!key.isNullOrEmpty() && !value.isNullOrEmpty())
                options.variables[key] = value
        }
        options.logLevel = logLevelBox.selectedItem as String ?: "INFO"
        options.defaultLogLevel = defaultLogLevelBox.selectedItem as String ?: "INFO"
        options.dryRun = dryRunCheckBox.isSelected
        options.runEmptySuite = runEmptySuiteCheckBox.isSelected
    }

    override fun createEditor(): JComponent = mainPanel

    private fun buildMainPanel(): DialogPanel {
        val disposable = Disposer.newDisposable()
        val tabs = JBTabsFactory.createEditorTabs(null, disposable)

        tabs.addTab(TabInfo(buildTestSuitesPanel()).setText(MyBundle.message("robot.run.configuration.section.suites")))
        tabs.addTab(TabInfo(buildOutputPanel()).setText(MyBundle.message("robot.run.configuration.section.output")))
        tabs.addTab(TabInfo(buildVariablesPanel()).setText(MyBundle.message("robot.run.configuration.section.variables")))
        tabs.addTab(TabInfo(buildExecutionPanel()).setText(MyBundle.message("robot.run.configuration.section.execution")))

        return panel {
            row(label = MyBundle.message("robot.run.configuration.label.interpreter")) {
                sdkComboBox = comboBox(
                    DefaultComboBoxModel(pythonSdks.toTypedArray()),
                    { null },
                    { },
                    object : SimpleListCellRenderer<Sdk>() {
                        override fun customize(
                            list: JList<out Sdk>,
                            sdk: Sdk?,
                            index: Int,
                            selected: Boolean,
                            hasFocus: Boolean
                        ) {
                            text = if (sdk == null) "" else "${sdk.name} (${sdk.homePath})"
                        }
                    }
                ).component
            }

            row {
                tabs.component().constraints(CCFlags.pushX)
            }
        }
    }

    private fun buildTestSuitesPanel() = panel {
        row {
            cell(isVerticalFlow = true, isFullWidth = true) {
                label("")
                label(MyBundle.message("robot.run.configuration.label.paths"))
                suitePanel()()
            }
        }
        row {
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.test-names"))
                namePanel(testNameModel, "Test name", "Name is case- and space-insensitive")().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.suite-names"))
                namePanel(suiteNameModel, "Suite name", "Name is case- and space-insensitive")().constraints(CCFlags.pushX)
            }
        }
        row {
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.included-tags"))
                namePanel(includeTagModel, "Tag", "Use tag pattern to match more tag")().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.excluded-tags"))
                namePanel(excludeTagModel, "Tag", "Use tag pattern to match more tag")().constraints(CCFlags.pushX)
            }
        }
    }

    private fun buildOutputPanel() = panel {
        row {
            label("")
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.output-dir"))
            outputDirTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Output directory",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.output-file"))
            outputFileTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Output file",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.log-file"))
            logFileTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Log file",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.log-title"))
            logTitleTextField = textField({ "" }, {}).component
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.report-file"))
            reportFileTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Report file",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
        }
        row {
            label(MyBundle.message("robot.run.configuration.label.report-title"))
            reportTitleTextField = textField({ "" }, {}).component
        }
        row {
            cell(isVerticalFlow = true) {
                timestampOutputsCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.output-timestamp")).component
                splitLogsCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.log-split")).component
            }
        }
    }

    private fun buildVariablesPanel() = panel {
        row {
            cell(isVerticalFlow = true, isFullWidth = true) {
                label("")
                variablesPanel(variablesModel)().constraints(CCFlags.pushX, CCFlags.pushY)
            }
        }
    }

    private fun buildExecutionPanel() = panel {
        row { label("") }
        row {
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.log-level"))
                logLevelBox = comboBox(
                    DefaultComboBoxModel(arrayOf("INFO", "DEBUG", "TRACE")),
                    { "INFO" },
                    { }
                ).component
            }
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.default-log-level"))
                defaultLogLevelBox = comboBox(
                    DefaultComboBoxModel(arrayOf("INFO", "DEBUG", "TRACE")),
                    { "INFO" },
                    { }
                ).component
            }
        }
        row {
            dryRunCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.dry-run"), false, MyBundle.message("robot.run.configuration.desc.dry-run")).component
        }
        row {
            runEmptySuiteCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.run-empty"), false, MyBundle.message("robot.run.configuration.desc.run-empty")).component
        }
    }

    private fun suitePanel(): JPanel {
        val suitesPathList = JBList<String>()
        suitesPathList.setEmptyText("There is no suites to run")
        suitesPathList.model = suitePathModel

        val decorator = ToolbarDecorator.createDecorator(suitesPathList)
        decorator.setPreferredSize(Dimension(20000, 100))
        decorator.setMinimumSize(Dimension(300, 50))
        decorator.setAddAction {
            val fileDescriptor =
                FileChooserDescriptor(true, true, false, false, false, true).withTitle("Select Folders/Files")
            FileChooser.chooseFiles(fileDescriptor, null, null) { files ->
                files.forEach {
                    if (!suitePathModel.contains(it.path))
                        suitePathModel.addElement(it.path)
                }
            }
        }
        return decorator.createPanel()
    }

    private fun namePanel(model: DefaultListModel<String>, title: String, message: String): JPanel {
        val list = JBList(model)
        val decorator = ToolbarDecorator.createDecorator(list)
        decorator.setPreferredSize(Dimension(20000, 50))
        decorator.setAddAction {
            val name = Messages.showInputDialog(null, message, title, null) ?: ""
            if(name.isNotBlank())
                model.addElement(name)
        }
        decorator.setEditAction {
            val name = Messages.showInputDialog(null, message, title, null, list.selectedValue, null) ?: ""
            if(name.isNotBlank())
                model.setElementAt(name, list.selectedIndex)
        }
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT)
        decorator.disableUpAction()
        decorator.disableDownAction()
        return decorator.createPanel()
    }

    private fun variablesPanel(model: DefaultTableModel): JPanel {
        val table = JBTable(model)
        model.addColumn(MyBundle.message("robot.run.configuration.label.key"))
        model.addColumn(MyBundle.message("robot.run.configuration.label.value"))
        val decorator = ToolbarDecorator.createDecorator(table)
        decorator.setAddAction { model.addRow(arrayOf("", "")) }
        decorator.setRemoveAction { table.selectedRows.reversed().forEach { model.removeRow(it) } }
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT)
        decorator.setPreferredSize(Dimension(200000, 200))
        return decorator.createPanel()
    }

    private fun String?.toSdk(): Sdk? = ProjectJdkTable.getInstance().allJdks.firstOrNull { it.homePath == this }

    private fun <T> DefaultListModel<T>.addAll(values: List<T>) {
        values.forEach { this.addElement(it) }
    }
}