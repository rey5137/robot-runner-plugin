package com.github.rey5137.robotrunnerplugin

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nullable


object MyNotifier {

    private val NOTIFICATION_GROUP =
        NotificationGroup("Robot Runner Notification Group", NotificationDisplayType.BALLOON, true)

    fun notify(@Nullable project: Project?, content: String, type: NotificationType) {
        NOTIFICATION_GROUP.createNotification(content, type)
            .notify(project)
    }

}