package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.openFile
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ConsoleView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile

class RobotRunTaskState(
    private val project: Project,
    private val name: String,
    private val options: RobotRunConfigurationOptions,
    environment: ExecutionEnvironment
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commands = mutableListOf<String>()

        val sdk = options.sdkHomePath.toSdk() ?: throw ExecutionException("SDK is not configured")
        commands += sdk.homePath!!

        val robotRunFile = sdk.sdkModificator.getRoots(OrderRootType.CLASSES)
            .mapNotNull { it.findRobotRunFile() }
            .firstOrNull() ?: throw ExecutionException("Cannot find Robot's run.py file")
        commands += robotRunFile.path

        options.testNames.forEach { commands.addPair("-t", it) }
        options.suiteNames.forEach { commands.addPair("-s", it) }
        options.includeTags.forEach { commands.addPair("-i", it) }
        options.excludeTags.forEach { commands.addPair("-e", it) }
        options.outputDirPath.ifNotEmpty { commands.addPair("-d", it) }
        if (options.suffixWithConfigName) {
            commands.addPair("-o", (options.outputFilePath ?: "output").suffixFileName(name))
            commands.addPair("-l", (options.logFilePath ?: "log").suffixFileName(name))
            commands.addPair("-r", (options.reportFilePath ?: "report").suffixFileName(name))
        } else {
            options.outputFilePath.ifNotEmpty { commands.addPair("-o", it) }
            options.logFilePath.ifNotEmpty { commands.addPair("-l", it) }
            options.reportFilePath.ifNotEmpty { commands.addPair("-r", it) }
        }
        options.logTitle.ifNotEmpty { commands.addPair("--logtitle", it) }
        options.reportTitle.ifNotEmpty { commands.addPair("--reporttitle", it) }
        options.timestampOutputs.ifEnable { commands.add("-T") }
        options.splitLog.ifEnable { commands.add("--splitlog") }
        options.variables.forEach { (key, value) -> commands.addPair("-v", "$key:$value") }
        options.logLevel.ifNotEmpty { commands.addPair("-L", "${it}:${options.defaultLogLevel}") }
        options.dryRun.ifEnable { commands.add("--dryrun") }
        options.runEmptySuite.ifEnable { commands.add("--runemptysuite") }
        options.extraArguments.ifNotEmpty { value -> commands.addAll(value.parseCommandLineArguments()) }

        commands.addAll(options.suitePaths)

        val commandLine = GeneralCommandLine(commands)
        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    override fun createActions(
        console: ConsoleView,
        processHandler: ProcessHandler,
        executor: Executor
    ): Array<AnAction> {
        val openAction = object : DumbAwareAction(MyBundle.message("robot.run.configuration.label.open-output"), null, AllIcons.Actions.Menu_open), AnAction.TransparentUpdate {
            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = processHandler.isProcessTerminated
            }

            override fun actionPerformed(e: AnActionEvent) {
                if (processHandler.isProcessTerminated) {
                    val outputFilePath = (console as ConsoleViewImpl).text.findOutputFilePath()
                    if (outputFilePath != null)
                        project.openFile(outputFilePath)
                }
            }
        }
        return arrayOf(
            openAction
        )
    }

    private fun VirtualFile.findRobotRunFile(): VirtualFile? {
        if (!isDirectory || name != "site-packages")
            return null
        return this.findChild("robot")?.findChild("run.py")
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

    private fun String.suffixFileName(value: String): String {
        if (this.equals("None", ignoreCase = true))
            return this
        val index = this.lastIndexOf('.')
        return if (index >= 0)
            "${substring(0, index)}_$value${substring(index)}"
        else
            "${this}_$value"
    }

    private fun String.parseCommandLineArguments(): List<String> {
        val args = mutableListOf<String>()
        val builder = StringBuilder()
        val quoteChar = '"'
        val spaceChar = ' '
        var skipChar = spaceChar
        forEach { c ->
            if (c != skipChar) {
                if (c == quoteChar && builder.isEmpty())
                    skipChar = quoteChar
                else
                    builder.append(c)
            } else if (c == quoteChar) {
                if (builder.isNotEmpty() && builder.isNotBlank()) {
                    args.add(builder.toString())
                    builder.clear()
                }
                skipChar = spaceChar
            } else {
                if (builder.isNotEmpty() && builder.isNotBlank()) {
                    args.add(builder.toString())
                    builder.clear()
                }
            }
        }
        return args.toList()
    }

    private fun String.findOutputFilePath(): String? {
        val regex = "^.*Output:\\s+([^\\n]*).*$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
        val result = regex.matchEntire(this)
        return if (result == null)
            null
        else
            result.groupValues[1]
    }
}