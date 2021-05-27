package com.github.rey5137.robotrunnerplugin.editors.ui

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.NativeFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import java.io.File

fun Project.openFile(filePath: String) {
    try {
        val virtualFile = VfsUtil.findFileByIoFile(File(filePath), true)
        FileEditorManager.getInstance(this).openFile(virtualFile!!, true)
    } catch (ex: Exception) {
        try {
            val virtualFile = VfsUtil.findFileByIoFile(File(filePath), true)
            NativeFileType.openAssociatedApplication(virtualFile!!)
        }
        catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}