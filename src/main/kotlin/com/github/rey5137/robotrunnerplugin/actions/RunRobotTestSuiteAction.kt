package com.github.rey5137.robotrunnerplugin.actions

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RunRobotTestSuiteAction : AnAction {

    constructor() : super()

    constructor(runConfigurationSetting: RunnerAndConfigurationSettings) : super(runConfigurationSetting.name, runConfigurationSetting.name, null)

    override fun actionPerformed(e: AnActionEvent) {

    }

}