package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.layout.panel
import javax.swing.*

class RobotSettingsEditor : SettingsEditor<RobotRunConfiguration>() {

    private lateinit var mainPanel: JPanel
    private lateinit var sdkComboBox: ComboBox<Sdk>

    private var configuration: RobotRunConfiguration? = null

    private val pythonSdks = ProjectJdkTable.getInstance().allJdks.filter { it.sdkType.name == "Python SDK" }

    init {
        createUI()
    }

    override fun resetEditorFrom(configuration: RobotRunConfiguration) {
        this.configuration = configuration
        sdkComboBox.selectedItem = configuration.sdk
    }

    override fun applyEditorTo(configuration: RobotRunConfiguration) {
        configuration.sdk = sdkComboBox.selectedItem as Sdk?
    }

    override fun createEditor(): JComponent = mainPanel

    private fun createUI() {
        mainPanel = panel {
            row {
                label("Python interpreter")
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
                            text = if(sdk == null) "" else "${sdk.name} (${sdk.homePath})"
                        }
                    }
                ).component
            }
        }
    }

}