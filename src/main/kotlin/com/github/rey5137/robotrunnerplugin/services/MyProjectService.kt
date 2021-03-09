package com.github.rey5137.robotrunnerplugin.services

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
