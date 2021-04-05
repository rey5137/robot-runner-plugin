package com.github.rey5137.robotrunnerplugin.editors

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.LanguageUtil
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile

class RobotOutputFileEditorProvider : WeighedFileEditorProvider() {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        val isXml = file.fileType == XmlFileType.INSTANCE || (ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file) == XmlFileType.INSTANCE)
        if(!isXml)
            return false
        val xmlFile = PsiManager.getInstance(project).findFile(file) as XmlFile
        return xmlFile.rootTag?.name == TAG_ROBOT
    }

    override fun createEditor(project: Project, file: VirtualFile) = RobotOutputFileEditor(project, file)

    override fun getEditorTypeId() = "robot-output-editor"

    override fun getPolicy() = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR
}