package com.github.rey5137.robotrunnerplugin.configurables

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
            val oldSettings = RobotRunProjectSettingsState.getInstance(project).settings
            val newSettings = component.getSettings()
            if(newSettings.size != oldSettings.size)
                return true
            newSettings.forEach {
                if(oldSettings.indexOf(it) < 0)
                    return true
            }
        }
        return false
    }

    override fun apply() {
        settingsComponent?.let {
            val state = RobotRunProjectSettingsState.getInstance(project)
            state.settings.clear()
            state.settings.addAll(it.getSettings())
        }
    }

    override fun reset() {
        settingsComponent?.let {
            val state = RobotRunProjectSettingsState.getInstance(project)
            it.resetSettings(state.settings)
        }
    }

    override fun getDisplayName(): String = "Robot Runner Settings"

    override fun disposeUIResources() {
        settingsComponent = null
    }

}