package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.editors.xml.TAG_ROBOT
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.Language
import com.intellij.lang.LanguageUtil
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.xml.stream.XMLInputFactory

class RobotOutputFileEditorProvider : WeighedFileEditorProvider() {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        val isXml = file.fileType == XmlFileType.INSTANCE || (ScratchUtil.isScratch(file) && LanguageUtil.getLanguageForPsi(project, file) == XMLLanguage.INSTANCE)
        if (!isXml)
            return false

        return file.findRootTag() == TAG_ROBOT
    }

    override fun createEditor(project: Project, file: VirtualFile) = RobotOutputFileEditor(project, file)

    override fun getEditorTypeId() = "robot-output-editor"

    override fun getPolicy() = FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR

    private fun VirtualFile.findRootTag(): String? {
        val xmlInputFactory = XMLInputFactory.newInstance()
        val reader = xmlInputFactory.createXMLEventReader(this.inputStream)
        while (reader.hasNext()) {
            val nextEvent = reader.nextEvent()
            if (nextEvent.isStartElement)
                return nextEvent.asStartElement().name.localPart
        }
        return null
    }
}
