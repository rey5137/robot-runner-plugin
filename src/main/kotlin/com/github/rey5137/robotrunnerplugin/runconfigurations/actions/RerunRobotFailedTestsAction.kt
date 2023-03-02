package com.github.rey5137.robotrunnerplugin.runconfigurations.actions

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.extractFailedTestCases
import com.github.rey5137.robotrunnerplugin.runconfigurations.*
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.vfs.VfsUtil
import org.jdom.Element
import java.io.File
import java.util.*

class RerunRobotFailedTestsAction(
    private val processHandler: ProcessHandler,
    private val console: ConsoleView,
    private val environment: ExecutionEnvironment,
    private val configuration: RobotRunConfiguration,
    private val rerunFailedCaseConfig: RerunFailedCaseConfig?,
) : AnAction(
    MyBundle.message("robot.run.configuration.label.rerun-failed-cases"),
    "",
    AllIcons.RunConfigurations.RerunFailedTests
) {

    override fun update(e: AnActionEvent) {
        if (processHandler.isProcessTerminated) {
            val count = console.text().findNumberOfFailedCases() ?: 0
            e.presentation.isEnabled = count > 0
        } else
            e.presentation.isEnabled = false
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (processHandler.isProcessTerminated) {
            val text = console.text()
            val outputFile = text.findOutputFilePath() ?: return
            val testcases = if(console is RobotOutputConsoleView)
                console.robotOutputView.getFailedTestcases()
            else
                VfsUtil.findFileByIoFile(File(outputFile), true)?.extractFailedTestCases() ?: emptyList()
            if (testcases.isEmpty())
                return

            val config = rerunFailedCaseConfig?.copy(
                rerunTime = rerunFailedCaseConfig.rerunTime + 1,
                testcases = testcases,
                suffix = "rerun_${rerunFailedCaseConfig.rerunTime + 1}",
                previousRerunConfig = rerunFailedCaseConfig
            ) ?: RerunFailedCaseConfig(
                outputFile = outputFile,
                logFile = text.findLogFilePath() ?: return,
                reportFile = text.findReportFilePath() ?: return,
                rerunTime = 1,
                testcases = testcases,
                suffix = "rerun_1"
            )

            val runProfile = MyRunProfile(configuration, config)
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
        private val config: RerunFailedCaseConfig,
    ) :
        RunConfigurationBase<Element?>(
            configuration.project,
            configuration.factory,
            "${configuration.name} - Rerun ${config.rerunTime}",
        ),
        ModuleRunProfile, WrappingRunConfiguration<RunConfigurationBase<*>> {

        override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
            return RobotRunTaskState(
                environment.project,
                configuration,
                environment,
                config
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