package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.runconfigurations.actions.OpenOutputFileAction
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile

class MergeOutputRunTaskState(
    private val project: Project,
    private val configuration: RobotRunConfiguration,
    environment: ExecutionEnvironment,
    private val rerunFailedCaseConfig: RerunFailedCaseConfig
) : CommandLineState(environment) {


    override fun startProcess(): ProcessHandler {
        val options = configuration.options
        val commands = mutableListOf<String>()
        val sdk = options.sdkHomePath.toSdk() ?: throw ExecutionException("SDK is not configured")
        commands += sdk.homePath!!
        val rebotRunFile = sdk.sdkModificator.getRoots(OrderRootType.CLASSES)
            .mapNotNull { it.findRebotRunFile() }
            .firstOrNull() ?: throw ExecutionException("Cannot find Robot's rebot.py file")
        commands += rebotRunFile.path
        commands += "-R"

        val suffix = "merged_${rerunFailedCaseConfig.rerunTime + 1}_files"
        commands.addPair("-o", rerunFailedCaseConfig.outputFile.suffixFileName(suffix))
        commands.addPair("-l", rerunFailedCaseConfig.logFile.suffixFileName(suffix))
        commands.addPair("-r", rerunFailedCaseConfig.reportFile.suffixFileName(suffix))

        options.logTitle.ifNotEmpty { commands.addPair("--logtitle", it) }
        options.reportTitle.ifNotEmpty { commands.addPair("--reporttitle", it) }
        options.timestampOutputs.ifEnable { commands.add("-T") }
        options.splitLog.ifEnable { commands.add("--splitlog") }
        options.logLevel.ifNotEmpty { commands.addPair("-L", "${it}:${options.defaultLogLevel}") }

        val outputFiles = mutableListOf<String>()
        var rerunConfig: RerunFailedCaseConfig? = rerunFailedCaseConfig
        while (rerunConfig != null) {
            outputFiles.add(0, rerunConfig.outputFile.suffixFileName(rerunConfig.suffix))
            rerunConfig = rerunConfig.previousRerunConfig
        }
        outputFiles.add(0, rerunFailedCaseConfig.outputFile)
        commands.addAll(outputFiles)

        val commandLine = GeneralCommandLine(commands)
        commandLine.setWorkDirectory(project.basePath)

        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    override fun createActions(
        console: ConsoleView,
        processHandler: ProcessHandler,
        executor: Executor
    ): Array<AnAction> = arrayOf(
        OpenOutputFileAction(project, processHandler, console)
    )

    private fun VirtualFile.findRebotRunFile(): VirtualFile? {
        if (!isDirectory || name != "site-packages")
            return null
        return this.findChild("robot")?.findChild("rebot.py")
    }

    private fun String?.toSdk(): Sdk? = ProjectJdkTable.getInstance().allJdks.firstOrNull { it.homePath == this }

    private fun <T> MutableList<T>.addPair(first: T, second: T): MutableList<T> {
        add(first)
        add(second)
        return this
    }

    private inline fun String?.ifNotEmpty(func: (String) -> Unit) {
        if (!isNullOrEmpty())
            func(this)
    }

    private inline fun Boolean.ifEnable(func: () -> Unit) {
        if (this)
            func()
    }

}