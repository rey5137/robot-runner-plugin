package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent


class RobotOutputFileEditor(project: Project, srcFile: VirtualFile) : UserDataHolderBase(), FileEditor {

    private val myComponent = RobotOutputView(project, srcFile)

    override fun dispose() {
        myComponent.dispose()
    }

    override fun getComponent(): JComponent = myComponent

    override fun getPreferredFocusedComponent(): JComponent = myComponent

    override fun getName(): String = MyBundle.message("robot.output.editor.label.robot-result")

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? = null

}