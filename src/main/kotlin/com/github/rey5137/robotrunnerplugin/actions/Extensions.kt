package com.github.rey5137.robotrunnerplugin.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.ArchiveFileSystem

val AnActionEvent.files : List<VirtualFile>
    get() = this.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.toList()?.mapNotNull { findLocalFile(it) } ?: emptyList()

fun findLocalFile(file: VirtualFile?): VirtualFile? {
    if (file == null || file.isInLocalFileSystem) {
        return file
    }
    val fs = file.fileSystem
    return if (fs is ArchiveFileSystem && file.parent == null) {
        fs.getLocalByEntry(file)
    } else null
}