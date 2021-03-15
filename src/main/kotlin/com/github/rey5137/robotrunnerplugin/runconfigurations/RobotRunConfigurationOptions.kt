package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class RobotRunConfigurationOptions : RunConfigurationOptions() {

    private val sdkHomePathProperty: StoredProperty<String?> = string("").provideDelegate(this, "sdkHomePath")
    private val suitePathsProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "suitePaths")
    private val testNamesProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "testNames")
    private val suiteNamesProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "suiteNames")
    private val includeTagsProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "includeTags")
    private val excludeTagsProperty: StoredProperty<MutableList<String>> = list<String>().provideDelegate(this, "excludeTags")

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

    var testNames: List<String>
        get() = testNamesProperty.getValue(this)
        set(value) {
            testNamesProperty.setValue(this, value.toMutableList())
        }

    var suiteNames: List<String>
        get() = suiteNamesProperty.getValue(this)
        set(value) {
            suiteNamesProperty.setValue(this, value.toMutableList())
        }

    var includeTags: List<String>
        get() = includeTagsProperty.getValue(this)
        set(value) {
            includeTagsProperty.setValue(this, value.toMutableList())
        }

    var excludeTags: List<String>
        get() = excludeTagsProperty.getValue(this)
        set(value) {
            excludeTagsProperty.setValue(this, value.toMutableList())
        }
}