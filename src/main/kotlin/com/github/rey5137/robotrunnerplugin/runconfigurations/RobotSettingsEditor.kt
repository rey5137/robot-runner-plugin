package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.*

class RobotSettingsEditor : SettingsEditor<RobotRunConfiguration>() {

    private val mainPanel by lazy { buildMainPanel() }

    private lateinit var sdkComboBox: ComboBox<Sdk>
    private val suitesPathList: JBList<String> by lazy { JBList<String>() }
    private val suitesPathModel: DefaultListModel<String> by lazy { DefaultListModel() }

    private var configuration: RobotRunConfiguration? = null

    private val pythonSdks = ProjectJdkTable.getInstance().allJdks.filter { it.sdkType.name == "Python SDK" }

    override fun resetEditorFrom(configuration: RobotRunConfiguration) {
        this.configuration = configuration
        sdkComboBox.selectedItem = configuration.sdk
        suitesPathModel.clear()
        suitesPathModel.addAll(configuration.suitePaths)
    }

    override fun applyEditorTo(configuration: RobotRunConfiguration) {
        configuration.sdk = sdkComboBox.selectedItem as Sdk?
        configuration.suitePaths = suitesPathModel.elements().toList()
    }

    override fun createEditor(): JComponent = mainPanel

    private fun buildMainPanel() = panel {
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

        row (label = "Test Suites") { }
        row {
            suitePanel()
        }
    }

    private fun Row.suitePanel(): CellBuilder<JPanel> {
        suitesPathList.setEmptyText("There is no suites to run")
        suitesPathList.model = suitesPathModel

        val decorator = ToolbarDecorator.createDecorator(suitesPathList)
        decorator.setPreferredSize(Dimension(20000, 150))
        decorator.setMinimumSize(Dimension(300, 100))
        decorator.setAddAction {
            val fileDescriptor =
                FileChooserDescriptor(true, true, false, false, false, true).withTitle("Select Folders/Files")
            FileChooser.chooseFiles(fileDescriptor, null, null) { files ->
                files.forEach {
                    if (!suitesPathModel.contains(it.path))
                        suitesPathModel.addElement(it.path)
                }
            }
        }
        val panel = decorator.createPanel()
        return panel()
    }
}