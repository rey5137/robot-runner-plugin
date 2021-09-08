package com.github.rey5137.robotrunnerplugin.configurables

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class RobotRunProjectSettingsConfigurable(private val project: Project) : Configurable {

    private var settingsComponent: RobotRunProjectSettingsComponent? = null

    override fun createComponent(): JComponent {
        settingsComponent = RobotRunProjectSettingsComponent(project)
        return settingsComponent!!.mainPanel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.getPreferredFocusedComponent()
    }

    override fun isModified(): Boolean {
        settingsComponent?.let { component ->
            val oldSettingMap = RobotRunProjectSettingsState.getInstance(project).settingMap
            val newSettingMap = component.getSettings()
            if (newSettingMap.size != oldSettingMap.size)
                return true
            newSettingMap.forEach { (key, value) ->
                if (value != oldSettingMap[key])
                    return true
            }
        }
        return false
    }

    override fun apply() {
        settingsComponent?.let {
            val state = RobotRunProjectSettingsState.getInstance(project)
            state.settingMap = it.getSettings()
        }
    }

    override fun reset() {
        settingsComponent?.let {
            val state = RobotRunProjectSettingsState.getInstance(project)
            it.resetSettings(state.settingMap)
        }
    }

    override fun getDisplayName(): String = MyBundle.getMessage("robot.run.settings.name")

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
