package com.github.rey5137.robotrunnerplugin.configurables

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.project.Project

class RobotRunProjectSettingsConfigurableProvider(private val project: Project) : ConfigurableProvider() {

    override fun createConfigurable(): Configurable = RobotRunProjectSettingsConfigurable(project)

    override fun canCreateConfigurable(): Boolean = true
}
