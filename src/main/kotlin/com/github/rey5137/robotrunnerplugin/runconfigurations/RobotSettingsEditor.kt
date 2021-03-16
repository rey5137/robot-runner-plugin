package com.github.rey5137.robotrunnerplugin.runconfigurations

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
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import java.awt.Dimension
import javax.swing.*

class RobotSettingsEditor : SettingsEditor<RobotRunConfiguration>() {

    private val mainPanel by lazy { buildMainPanel() }

    private lateinit var sdkComboBox: ComboBox<Sdk>
    private lateinit var outputDirTextField: TextFieldWithBrowseButton
    private lateinit var outputFileTextField: TextFieldWithBrowseButton
    private val suitePathModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val testNameModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val suiteNameModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val includeTagModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val excludeTagModel: DefaultListModel<String> by lazy { DefaultListModel() }

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
    }

    override fun createEditor(): JComponent = mainPanel

    private fun buildMainPanel(): DialogPanel {
        val disposable = Disposer.newDisposable()
        val tabs = JBTabsFactory.createEditorTabs(null, disposable)

        tabs.addTab(TabInfo(buildTestSuitesPanel()).setText("Test Suites"))
        tabs.addTab(TabInfo(buildOutputPanel()).setText("Output"))

        return panel {
            row(label = "Python interpreter") {
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
                label("Paths")
                suitePanel()()
            }
        }
        row {
            cell(isVerticalFlow = true) {
                label("Test names")
                namePanel(testNameModel, "Test name", "Name is case- and space-insensitive")().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label("Suite names")
                namePanel(suiteNameModel, "Suite name", "Name is case- and space-insensitive")().constraints(CCFlags.pushX)
            }
        }
        row {
            cell(isVerticalFlow = true) {
                label("Include tags")
                namePanel(includeTagModel, "Tag", "Use tag pattern to match more tag")().constraints(CCFlags.pushX)
            }
            cell(isVerticalFlow = true) {
                label("Exclude tags")
                namePanel(excludeTagModel, "Tag", "Use tag pattern to match more tag")().constraints(CCFlags.pushX)
            }
        }
    }

    private fun buildOutputPanel() = panel {
        row {
            label("")
        }
        row {
            label("Output directory")
            outputDirTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Output directory",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
        }
        row {
            label("Output file")
            outputFileTextField = textFieldWithBrowseButton(
                browseDialogTitle = "Output file",
                value = null,
                project = null,
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ).component
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
        val table = JBList(model)
        val decorator = ToolbarDecorator.createDecorator(table)
        decorator.setPreferredSize(Dimension(20000, 50))
        decorator.setAddAction {
            val name = Messages.showInputDialog(null, "Name is case- and space-insensitive", title, null) ?: ""
            if(name.isNotBlank())
                model.addElement(name)
        }
        decorator.setEditAction {
            val name = Messages.showInputDialog(null, "Name is case- and space-insensitive", title, null, table.selectedValue, null) ?: ""
            if(name.isNotBlank())
                model.setElementAt(name, table.selectedIndex)
        }
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT)
        decorator.disableUpAction()
        decorator.disableDownAction()
        return decorator.createPanel()
    }

    private fun String?.toSdk(): Sdk? = ProjectJdkTable.getInstance().allJdks.firstOrNull { it.homePath == this }
}