package com.github.rey5137.robotrunnerplugin.editors.ui

data class HighlightInfo(
    val value: String,
    val ignoreCase: Boolean
) {
    fun match(text: String?) : Boolean {
        if(text == null)
            return false
        val result = text.contains(value, ignoreCase = ignoreCase)
        return result
    }
}
