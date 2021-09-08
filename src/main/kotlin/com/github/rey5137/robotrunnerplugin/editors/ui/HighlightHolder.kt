package com.github.rey5137.robotrunnerplugin.editors.ui

data class HighlightHolder<T>(
    val value: T,
    val highlight: HighlightType = HighlightType.UNMATCHED,
)

fun <T> T.toHighlightHolder(isHighlight: Boolean) = HighlightHolder<T>(
    value = this,
    highlight = if (isHighlight) HighlightType.MATCHED else HighlightType.UNMATCHED
)

enum class HighlightType {
    UNMATCHED,
    CONTAINED,
    MATCHED
}
