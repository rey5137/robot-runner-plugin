package com.github.rey5137.robotrunnerplugin.runconfigurations.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.runconfigurations.MergeOutputRunTaskState
import com.github.rey5137.robotrunnerplugin.runconfigurations.RerunFailedCaseConfig
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.runners.ProgramRunner
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.SettingsEditor
import org.jdom.Element

class MergeRobotOutputAction(
    private val processHandler: ProcessHandler,
    private val environment: ExecutionEnvironment,
    private val configuration: RobotRunConfiguration,
    private val rerunFailedCaseConfig: RerunFailedCaseConfig,
) : AnAction(
    MyBundle.message("robot.run.configuration.label.merge-output"),
    "",
    AllIcons.Actions.GroupByModule
) {

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = processHandler.isProcessTerminated
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (processHandler.isProcessTerminated) {
            val runProfile = MyRunProfile(configuration, rerunFailedCaseConfig)
            val environmentBuilder = ExecutionEnvironmentBuilder(environment)
                .runProfile(runProfile)
                .runner(ProgramRunner.getRunner(DefaultRunExecutor.getRunExecutorInstance().id, runProfile)!!)

            val environment: ExecutionEnvironment = environmentBuilder.build()
            try {
                environment.runner.execute(environment)
            } finally {
                (environment.runProfile as MyRunProfile).clear()
            }
        }
    }

    private class MyRunProfile(
        private val configuration: RobotRunConfiguration,
        private val rerunFailedCaseConfig: RerunFailedCaseConfig,
    ) :
        RunConfigurationBase<Element?>(
            configuration.project,
            configuration.factory,
            "${configuration.name} - Merge ${rerunFailedCaseConfig.rerunTime + 1} output files",
        ),
        ModuleRunProfile, WrappingRunConfiguration<RunConfigurationBase<*>> {

        override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
            return MergeOutputRunTaskState(
                environment.project,
                configuration,
                environment,
                rerunFailedCaseConfig
            )
        }

        override fun getPeer(): RunConfigurationBase<*> {
            return configuration
        }

        fun clear() {}

        override fun readExternal(element: Element) {
            configuration.readExternal(element)
        }

        override fun writeExternal(element: Element) {
            configuration.writeExternal(element)
        }

        override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
            return configuration.configurationEditor
        }

        override fun createRunnerSettings(provider: ConfigurationInfoProvider): ConfigurationPerRunnerSettings? {
            return configuration.createRunnerSettings(provider)
        }

        override fun getRunnerSettingsEditor(runner: ProgramRunner<*>?): SettingsEditor<ConfigurationPerRunnerSettings>? {
            return configuration.getRunnerSettingsEditor(runner)
        }

        override fun clone(): RunConfiguration {
            return configuration.clone()
        }

        override fun getUniqueID(): Int {
            return configuration.uniqueID
        }

        override fun getOptionsForPredefinedLogFile(predefinedLogFile: PredefinedLogFile): LogFileOptions? {
            return configuration.getOptionsForPredefinedLogFile(predefinedLogFile)
        }

        override fun getPredefinedLogFiles(): List<PredefinedLogFile> {
            return configuration.predefinedLogFiles
        }

        override fun getAllLogFiles(): ArrayList<LogFileOptions> {
            return configuration.allLogFiles
        }

        override fun getLogFiles(): List<LogFileOptions> {
            return configuration.logFiles
        }
    }
}