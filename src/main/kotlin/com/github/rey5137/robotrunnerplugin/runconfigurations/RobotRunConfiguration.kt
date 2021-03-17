package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@State(
    name = "RobotRunConfiguration",
    storages = [Storage("robot-runner-plugin__run-configuration.xml")]
)
class RobotRunConfiguration(
    project: @NotNull Project,
    factory: @NotNull RobotRunConfigurationFactory,
    name: @Nullable String?
) : RunConfigurationBase<RobotRunConfigurationOptions>(project, factory, name) {

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        RobotRunTaskState(options, environment)

    override fun getOptionsClass(): Class<out RunConfigurationOptions> =
        RobotRunConfigurationOptions::class.java

    public override fun getOptions(): RobotRunConfigurationOptions =
        super.getOptions() as RobotRunConfigurationOptions

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RobotSettingsEditor()

    override fun checkConfiguration() {}

}