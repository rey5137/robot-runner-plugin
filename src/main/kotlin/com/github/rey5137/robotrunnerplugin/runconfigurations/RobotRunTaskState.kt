package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.github.rey5137.robotrunnerplugin.editors.RobotOutputView
import com.github.rey5137.robotrunnerplugin.http.Server
import com.github.rey5137.robotrunnerplugin.runconfigurations.actions.OpenOutputFileAction
import com.github.rey5137.robotrunnerplugin.runconfigurations.actions.RerunRobotFailedTestsAction
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.FileOutputStream

class RobotRunTaskState(
    private val project: Project,
    private val configuration: RobotRunConfiguration,
    environment: ExecutionEnvironment,
    private val rerunFailedCaseConfig: RerunFailedCaseConfig? = null
) : CommandLineState(environment) {

    lateinit var server : Server
    lateinit var robotOutputView: RobotOutputView

    override fun startProcess(): ProcessHandler {
        val options = configuration.options
        if(options.showOutputView) {
            robotOutputView = RobotOutputView(project)
            val port = findAvailablePort()
            server = Server(port) { method, payload ->
                if (method != null && payload != null)
                    robotOutputView.addEvent(method, payload)
                null
            }
            server.start()
        }

        val commands = mutableListOf<String>()

        val sdk = options.sdkHomePath.toSdk() ?: throw ExecutionException("SDK is not configured")
        commands += sdk.homePath!!

        val robotRunFile = sdk.sdkModificator.getRoots(OrderRootType.CLASSES)
            .mapNotNull { it.findRobotRunFile() }
            .firstOrNull() ?: throw ExecutionException("Cannot find Robot's run.py file")
        commands += robotRunFile.path

        if (rerunFailedCaseConfig == null)
            options.testNames.forEach { commands.addPair("-t", it) }
        else
            rerunFailedCaseConfig.testcases.forEach { commands.addPair("-t", it.escapeCharsInTestName()) }

        options.suiteNames.forEach { commands.addPair("-s", it) }
        options.includeTags.forEach { commands.addPair("-i", it) }
        options.excludeTags.forEach { commands.addPair("-e", it) }
        options.outputDirPath.ifNotEmpty { commands.addPair("-d", it) }
        if (rerunFailedCaseConfig == null) {
            if (options.suffixWithConfigName) {
                commands.addPair("-o", (options.outputFilePath ?: "output").suffixFileName(configuration.name))
                commands.addPair("-l", (options.logFilePath ?: "log").suffixFileName(configuration.name))
                commands.addPair("-r", (options.reportFilePath ?: "report").suffixFileName(configuration.name))
            } else {
                options.outputFilePath.ifNotEmpty { commands.addPair("-o", it) }
                options.logFilePath.ifNotEmpty { commands.addPair("-l", it) }
                options.reportFilePath.ifNotEmpty { commands.addPair("-r", it) }
            }
        } else {
            val suffix = "rerun_${rerunFailedCaseConfig.rerunTime}"
            commands.addPair("-o", rerunFailedCaseConfig.outputFile.suffixFileName(suffix))
            commands.addPair("-l", rerunFailedCaseConfig.logFile.suffixFileName(suffix))
            commands.addPair("-r", rerunFailedCaseConfig.reportFile.suffixFileName(suffix))
        }
        options.logTitle.ifNotEmpty { commands.addPair("--logtitle", it) }
        options.reportTitle.ifNotEmpty { commands.addPair("--reporttitle", it) }
        options.timestampOutputs.ifEnable { commands.add("-T") }
        options.splitLog.ifEnable { commands.add("--splitlog") }
        options.variables.forEach { (key, value) -> commands.addPair("-v", "$key:$value") }
        options.logLevel.ifNotEmpty { commands.addPair("-L", "$it:${options.defaultLogLevel}") }
        options.dryRun.ifEnable { commands.add("--dryrun") }
        options.runEmptySuite.ifEnable { commands.add("--runemptysuite") }
        options.extraArguments.ifNotEmpty { value -> commands.addAll(value.parseCommandLineArguments()) }

        if(options.showOutputView) {
            commands.add("--listener")
            commands.add("${getListenerFilePath()}:${server.port}")
        }

        commands.addAll(options.suitePaths)

        val commandLine = GeneralCommandLine(commands)
        commandLine.setWorkDirectory(project.basePath)

        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun getListenerFilePath(): String {
        val folder = File(FileUtilRt.getTempDirectory(), "RobotRunnerPlugin")
        if(!folder.exists())
            folder.mkdirs()
        val file = File(folder, "RobotRunnerPluginListener.py")
        if(!file.exists()) {
            val inputStream = javaClass.getResourceAsStream("/scripts/RobotRunnerPluginListener.py")
            val fileOutputStream = FileOutputStream(file)
            val buffer = ByteArray(32768)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                fileOutputStream.write(buffer, 0, length)
            }
            fileOutputStream.close()
            inputStream.close()
        }
        return file.absolutePath
    }

    override fun createActions(
        console: ConsoleView,
        processHandler: ProcessHandler,
        executor: Executor
    ): Array<AnAction> = arrayOf(
        OpenOutputFileAction(project, processHandler, console)
    )

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        val options = configuration.options
        val processHandler = startProcess()
        val console = if(options.showOutputView)
            RobotOutputConsoleView(project, createConsole(executor)!!, robotOutputView)
        else
            createConsole(executor)!!
        console.attachToProcess(processHandler)
        val result = DefaultExecutionResult(console, processHandler, *createActions(console, processHandler, executor))
        result.setRestartActions(
            RerunRobotFailedTestsAction(
                processHandler,
                console,
                environment,
                configuration,
                rerunFailedCaseConfig,
            )
        )
        if(options.showOutputView) {
            processHandler.addProcessListener(object : ProcessListener {
                override fun startNotified(event: ProcessEvent) {}

                override fun processTerminated(event: ProcessEvent) {
                    server.stop()
                }

                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {}
            })
        }
        return result
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
}
