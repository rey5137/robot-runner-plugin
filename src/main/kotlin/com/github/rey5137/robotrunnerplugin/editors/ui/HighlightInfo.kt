package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement
import com.github.rey5137.robotrunnerplugin.editors.xml.MessageElement
import com.github.rey5137.robotrunnerplugin.editors.xml.SuiteElement
import com.github.rey5137.robotrunnerplugin.editors.xml.TestElement

data class HighlightInfo(
    val value: String,
    val ignoreCase: Boolean
) {
    fun match(text: String?): Boolean {
        if (text == null)
            return false
        return text.contains(value, ignoreCase = ignoreCase)
    }
}

fun SuiteElement.shouldHighlight(highlightInfo: HighlightInfo?): Boolean {
    val info = highlightInfo ?: return false
    if (info.match(name))
        return true
    return false
}

fun TestElement.shouldHighlight(highlightInfo: HighlightInfo?): Boolean {
    val info = highlightInfo ?: return false
    if (info.match(name))
        return true
    tags.forEach { if (info.match(it)) return true }
    return false
}

fun KeywordElement.shouldHighlight(highlightInfo: HighlightInfo?): Boolean {
    val info = highlightInfo ?: return false
    if (info.match(name))
        return true
    arguments.forEach { if (info.match(it)) return true }
    assigns.forEach { if (info.match(it)) return true }
    tags.forEach { if (info.match(it)) return true }
    messages.forEach { if (info.match(it.value())) return true }
    return false
}

fun MessageElement.shouldHighlight(highlightInfo: HighlightInfo?): Boolean {
    val info = highlightInfo ?: return false
    if (info.match(this.value()))
        return true
    return false
}