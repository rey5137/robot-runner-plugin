package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
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
    private val suitePathModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val testNameModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val suiteNameModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val includeTagModel: DefaultListModel<String> by lazy { DefaultListModel() }
    private val excludeTagModel: DefaultListModel<String> by lazy { DefaultListModel() }

    private var configuration: RobotRunConfiguration? = null

    private val pythonSdks = ProjectJdkTable.getInstance().allJdks.filter { it.sdkType.name == "Python SDK" }

    override fun resetEditorFrom(configuration: RobotRunConfiguration) {
        this.configuration = configuration
        sdkComboBox.selectedItem = configuration.sdk
        suitePathModel.clear()
        suitePathModel.addAll(configuration.suitePaths)
        testNameModel.clear()
        testNameModel.addAll(configuration.testNames)
        suiteNameModel.clear()
        suiteNameModel.addAll(configuration.suiteNames)
        includeTagModel.clear()
        includeTagModel.addAll(configuration.includeTags)
        excludeTagModel.clear()
        excludeTagModel.addAll(configuration.excludeTags)
    }

    override fun applyEditorTo(configuration: RobotRunConfiguration) {
        configuration.sdk = sdkComboBox.selectedItem as Sdk?
        configuration.suitePaths = suitePathModel.elements().toList()
        configuration.testNames = testNameModel.elements().toList()
        configuration.suiteNames = suiteNameModel.elements().toList()
        configuration.includeTags = includeTagModel.elements().toList()
        configuration.excludeTags = excludeTagModel.elements().toList()
    }

    override fun createEditor(): JComponent = mainPanel

    private fun buildMainPanel(): DialogPanel {
        val disposable = Disposer.newDisposable()
        val tabs = JBTabsFactory.createEditorTabs(null, disposable)

        tabs.addTab(TabInfo(buildTestSuitesPanel()).setText("Test Suites") )
        tabs.addTab(TabInfo(panel {

        }).setText("Output"))

        return panel {
            row(label = "Python interpreter") {
                sdkComboBox = comboBox(
                    DefaultComboBoxModel(pythonSdks.toTypedArray()),
                    { configuration?.sdk },
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

            row { tabs.component() }
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
}