package com.github.rey5137.robotrunnerplugin.listeners

import com.github.rey5137.robotrunnerplugin.configurables.RobotRunProjectSettingsState
import com.github.rey5137.robotrunnerplugin.services.MyProjectService
import com.intellij.execution.RunManagerListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.util.messages.MessageBusConnection

internal class MyProjectManagerListener : ProjectManagerListener {

    private var connection: MessageBusConnection? = null

    override fun projectOpened(project: Project) {
        project.service<MyProjectService>()
        connection = project.messageBus.connect()
        connection!!.subscribe(RunManagerListener.TOPIC, RobotRunProjectSettingsState.getInstance(project))
    }

    override fun projectClosed(project: Project) {
        connection?.disconnect()
        connection?.dispose()
        connection = null
    }
}
