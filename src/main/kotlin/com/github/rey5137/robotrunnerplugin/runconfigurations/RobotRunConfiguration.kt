package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable


private const val KEY_SDK = "sdk"

@State(
    name = "RobotRunConfiguration",
    storages = [Storage("robot-runner-plugin__run-configuration.xml")]
)
class RobotRunConfiguration(
    project: @NotNull Project,
    factory: @NotNull RobotRunConfigurationFactory,
    name: @Nullable String?
) : RunConfigurationBase<RobotRunConfigurationOptions>(project, factory, name) {

    private val robotOptions = options as RobotRunConfigurationOptions

    var sdk: Sdk?
        get() = robotOptions.sdkHomePath.toSdk()
        set(value) { robotOptions.sdkHomePath = value?.homePath ?: "" }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine("ls")
                val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
                ProcessTerminatedListener.attach(processHandler)
                return processHandler
            }
        }
    }

    override fun getOptionsClass(): Class<out RunConfigurationOptions> = RobotRunConfigurationOptions::class.java

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return RobotSettingsEditor()
    }

    override fun checkConfiguration() {}

    private fun String?.toSdk(): Sdk? = ProjectJdkTable.getInstance().allJdks.firstOrNull { it.homePath == this }
}