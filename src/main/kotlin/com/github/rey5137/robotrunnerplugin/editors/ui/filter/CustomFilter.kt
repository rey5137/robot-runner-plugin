package com.github.rey5137.robotrunnerplugin.editors.ui.filter

import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.HasCommonField
import com.github.rey5137.robotrunnerplugin.editors.xml.KeywordElement

class CustomFilter(
    override var title: String,
    override var isEnabled: Boolean,
    var nameMatcher: TextMatcher = TextMatcher(),
    var parentNameMatcher: TextMatcher = TextMatcher(),
    var libraryMatcher: TextMatcher = TextMatcher(),
    var statusPassed: Boolean? = null
) : ElementFilter {

    override fun accept(element: Element): Boolean {
        if (!element.hasMatchedParent())
            return true

        if (element is HasCommonField) {
            if (!nameMatcher.isMatched(element.name))
                return false
            if (statusPassed != null && statusPassed != element.status.isPassed)
                return false
        }
        if (element is KeywordElement) {
            if (!libraryMatcher.isMatched(element.library))
                return false
        }

        return true
    }

    private fun Element.hasMatchedParent(): Boolean {
        if (!parentNameMatcher.isEnabled)
            return false
        if (this !is HasCommonField)
            return false
        val parent = this.parent
        return parent != null && parent is HasCommonField && parentNameMatcher.isMatched(parent.name)
    }

    data class TextMatcher(
        var pattern: String? = null,
        var isRegex: Boolean = false,
        var isOpposite: Boolean = false
    ) {

        val isEnabled: Boolean
            get() = pattern != null

        fun isMatched(text: String): Boolean {
            val pattern = this.pattern ?: return true
            val result = if (isRegex)
                pattern.toRegex().matches(text)
            else
                pattern.equals(text, true)
            return if (isOpposite) !result else result
        }
    }
}
