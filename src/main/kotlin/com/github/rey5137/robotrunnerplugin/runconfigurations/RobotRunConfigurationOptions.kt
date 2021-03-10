package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class RobotRunConfigurationOptions : RunConfigurationOptions() {

    private val sdkHomePathProperty: StoredProperty<String?> = string("").provideDelegate(this, "sdkHomePath")

    var sdkHomePath: String?
        get() = sdkHomePathProperty.getValue(this)
        set(value) {
            sdkHomePathProperty.setValue(this, value)
        }
}