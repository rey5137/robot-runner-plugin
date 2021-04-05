package com.github.rey5137.robotrunnerplugin.editors

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel

class RobotOutputFileEditor(private val project: Project, private val srcFile: VirtualFile) : UserDataHolderBase(), FileEditor {

    override fun dispose() {}

    override fun getComponent(): JComponent {
        return JPanel()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun getName(): String = "Robot Result"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }
}