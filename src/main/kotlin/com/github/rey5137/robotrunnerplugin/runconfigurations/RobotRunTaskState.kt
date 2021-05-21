package com.github.rey5137.robotrunnerplugin.runconfigurations

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.VirtualFile

class RobotRunTaskState(private val options: RobotRunConfigurationOptions, environment: ExecutionEnvironment): CommandLineState(environment) {

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
        options.outputFilePath.ifNotEmpty { commands.addPair("-o", it) }
        options.logFilePath.ifNotEmpty { commands.addPair("-l", it) }
        options.logTitle.ifNotEmpty { commands.addPair("--logtitle", it) }
        options.reportFilePath.ifNotEmpty { commands.addPair("-r", it) }
        options.reportTitle.ifNotEmpty { commands.addPair("--reporttitle", it) }
        options.timestampOutputs.ifEnable { commands.add("-T") }
        options.splitLog.ifEnable { commands.add("--splitlog") }
        options.variables.forEach { (key, value) -> commands.addPair("-v", "$key:$value") }
        options.logLevel.ifNotEmpty { commands.addPair("-L", "${it}:${options.defaultLogLevel}") }
        options.dryRun.ifEnable { commands.add("--dryrun") }
        options.runEmptySuite.ifEnable { commands.add("--runemptysuite") }
        options.extraArguments.ifNotEmpty { value -> commands.addAll(value.split(" ").filter { it.isNotBlank() }) }

        commands.addAll(options.suitePaths)

        val commandLine = GeneralCommandLine(commands)
        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    private fun VirtualFile.findRobotRunFile(): VirtualFile? {
        if(!isDirectory || name != "site-packages")
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
        if(!isNullOrEmpty())
            func(this)
    }

    private inline fun Boolean.ifEnable(func: () -> Unit) {
        if(this)
            func()
    }
}