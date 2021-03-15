package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class RobotRunConfigurationOptions : RunConfigurationOptions() {

    private val sdkHomePathProperty: StoredProperty<String?> = string("").provideDelegate(this, "sdkHomePath")
    private val suitePathsProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "suitePaths")

    var sdkHomePath: String?
        get() = sdkHomePathProperty.getValue(this)
        set(value) {
            sdkHomePathProperty.setValue(this, value)
        }

    var suitePaths: List<String>
        get() = suitePathsProperty.getValue(this)
        set(value) {
            suitePathsProperty.setValue(this, value.toMutableList())
        }
}