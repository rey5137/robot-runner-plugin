package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.*
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.*
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
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
    private lateinit var suffixWithConfigNameCheckBox: JBCheckBox
    private lateinit var logLevelBox: ComboBox<String>
    private lateinit var defaultLogLevelBox: ComboBox<String>
    private lateinit var dryRunCheckBox: JBCheckBox
    private lateinit var runEmptySuiteCheckBox: JBCheckBox
    private lateinit var extraArgumentsTextField: JBTextField
    private lateinit var showOutputViewCheckBox: JBCheckBox

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
        suffixWithConfigNameCheckBox.isSelected = options.suffixWithConfigName
        ((variablesModel.rowCount -1) downTo 0).forEach { variablesModel.removeRow(it) }
        options.variables.forEach { (key, value) -> variablesModel.addRow(arrayOf(key, value)) }
        logLevelBox.selectedItem = options.logLevel
        defaultLogLevelBox.selectedItem = options.defaultLogLevel
        dryRunCheckBox.isSelected = options.dryRun
        runEmptySuiteCheckBox.isSelected = options.runEmptySuite
        extraArgumentsTextField.text = options.extraArguments
        showOutputViewCheckBox.isSelected = options.showOutputView
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
        options.suffixWithConfigName = suffixWithConfigNameCheckBox.isSelected
        options.variables.clear()
        (0 until variablesModel.rowCount).forEach {
            val key = variablesModel.getValueAt(it, 0) as String?
            val value = variablesModel.getValueAt(it, 1) as String?
            if(!key.isNullOrEmpty() && !value.isNullOrEmpty())
                options.variables[key] = value
        }
        options.logLevel = logLevelBox.selectedItem as String
        options.defaultLogLevel = defaultLogLevelBox.selectedItem as String
        options.dryRun = dryRunCheckBox.isSelected
        options.runEmptySuite = runEmptySuiteCheckBox.isSelected
        options.extraArguments = extraArgumentsTextField.text
        options.showOutputView = showOutputViewCheckBox.isSelected
    }

    override fun createEditor(): JComponent = mainPanel

    private fun buildMainPanel(): JComponent {
        val tabs = JBTabbedPane()

        tabs.add(MyBundle.message("robot.run.configuration.section.suites"), buildTestSuitesPanel())
        tabs.add(MyBundle.message("robot.run.configuration.section.output"), buildOutputPanel())
        tabs.add(MyBundle.message("robot.run.configuration.section.variables"), buildVariablesPanel())
        tabs.add(MyBundle.message("robot.run.configuration.section.execution"), buildExecutionPanel())

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
                ).constraints(CCFlags.growX, CCFlags.pushX).component
            }

            row {
                tabs().constraints(CCFlags.growX, CCFlags.pushX)
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
                namePanel(testNameModel, "Test name", MyBundle.message("robot.run.configuration.desc.multi.test-names"), MyBundle.message("robot.run.configuration.desc.single.test-names"))().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.suite-names"))
                namePanel(suiteNameModel, "Suite name", MyBundle.message("robot.run.configuration.desc.multi.test-names"), MyBundle.message("robot.run.configuration.desc.single.test-names"))().constraints(CCFlags.pushX)
            }
        }
        row {
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.included-tags"))
                namePanel(includeTagModel, "Tag", MyBundle.message("robot.run.configuration.desc.multi.tags"), MyBundle.message("robot.run.configuration.desc.single.tags"))().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label(MyBundle.message("robot.run.configuration.label.excluded-tags"))
                namePanel(excludeTagModel, "Tag", MyBundle.message("robot.run.configuration.desc.multi.tags"), MyBundle.message("robot.run.configuration.desc.single.tags"))().constraints(CCFlags.pushX)
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
                suffixWithConfigNameCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.output-suffix-config-name")).component
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
        row {
            label(MyBundle.message("robot.run.configuration.label.extra-arguments"))
            extraArgumentsTextField = textField({ "" }, {}).constraints(CCFlags.pushX).component
        }
        row {
            showOutputViewCheckBox = checkBox(MyBundle.message("robot.run.configuration.label.show-output-view"), false, MyBundle.message("robot.run.configuration.desc.show-output-view")).component
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

    private fun namePanel(model: DefaultListModel<String>, title: String, addMessage: String, editMessage: String): JPanel {
        val list = JBList(model)
        val decorator = ToolbarDecorator.createDecorator(list)
        decorator.setPreferredSize(Dimension(20000, 50))
        decorator.setAddAction {
            val (name, wrapWord, escapeSpecialChars) = showMultilineInput(addMessage, title)
            if(name.isNotBlank())
                model.addAll(name.split("\n")
                    .filter { it.isNotBlank() }
                    .map { if(escapeSpecialChars) it.escapeCharsInTestName() else it }
                    .map { if(wrapWord) "*$it*" else it }
                )
        }
        decorator.setEditAction {
            val name = Messages.showInputDialog(null, editMessage, title, null, list.selectedValue, null) ?: ""
            if(name.isNotBlank())
                model.setElementAt(name, list.selectedIndex)
        }
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT)
        decorator.disableUpAction()
        decorator.disableDownAction()
        return decorator.createPanel()
    }

    private fun showMultilineInput(message: String, title: String): MultilineInput {
        val builder = DialogBuilder()
        val textArea = JBTextArea(5, 30)
        lateinit var wrapCheckbox: JBCheckBox
        lateinit var escapeCheckbox: JBCheckBox
        val panel = panel {
            row { label(message) }
            row {
                JBScrollPane(textArea).apply {
                    minimumSize = Dimension(textArea.preferredSize.width, textArea.preferredSize.height + 5)
                }()
            }
            row {
                wrapCheckbox = checkBox(MyBundle.message("robot.run.configuration.label.wrap-value")).component
            }
            row {
                escapeCheckbox = checkBox(MyBundle.message("robot.run.configuration.label.escape-special-chars")).component
            }
        }
        builder.setTitle(title)
        builder.setCenterPanel(panel)
        builder.removeAllActions()
        builder.addOkAction()
        builder.addCancelAction()
        return if(builder.show() == DialogWrapper.OK_EXIT_CODE)
            MultilineInput(
                text = textArea.text,
                wrapWord = wrapCheckbox.isSelected,
                escapeSpecialChars = escapeCheckbox.isSelected
            )
        else
            MultilineInput(
                text = "",
                wrapWord = false,
                escapeSpecialChars = false
            )
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

    data class MultilineInput(
        val text: String,
        val wrapWord: Boolean,
        val escapeSpecialChars: Boolean
    )
}