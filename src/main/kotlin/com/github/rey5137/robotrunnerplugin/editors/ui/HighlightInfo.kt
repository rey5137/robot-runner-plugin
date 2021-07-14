package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement
import com.github.rey5137.robotrunnerplugin.editors.xml.MessageElement
import com.github.rey5137.robotrunnerplugin.editors.xml.SuiteElement
import com.github.rey5137.robotrunnerplugin.editors.xml.TestElement

data class HighlightInfo(
    val value: String,
    val ignoreCase: Boolean
)

fun HighlightInfo?.match(text: String?) : Boolean {
    val info = this ?: return false
    if (text == null)
        return false
    return text.contains(info.value, ignoreCase = info.ignoreCase)
}

fun HighlightInfo?.match(element: SuiteElement) : Boolean {
    val info = this ?: return false
    if (info.match(element.name))
        return true
    return false
}

fun HighlightInfo?.match(element: TestElement) : Boolean {
    val info = this ?: return false
    if (info.match(element.name))
        return true
    element.tags.forEach { if (info.match(it)) return true }
    return false
}

fun HighlightInfo?.match(element: KeywordElement) : Boolean {
    val info = this ?: return false
    if (info.match(element.name))
        return true
    element.arguments.forEach { if (info.match(it)) return true }
    element.assigns.forEach { if (info.match(it)) return true }
    element.tags.forEach { if (info.match(it)) return true }
    element.messages.forEach { if (info.match(it.value())) return true }
    return false
}

fun HighlightInfo?.match(element: MessageElement) : Boolean {
    val info = this ?: return false
    if (info.match(element.value()))
        return true
    return false
}