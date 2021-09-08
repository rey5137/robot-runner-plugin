package com.github.rey5137.robotrunnerplugin.editors.ui.filter

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.SuiteElement

class HidePassedSuiteFilter(override var isEnabled: Boolean) : ElementFilter {

    override fun accept(element: Element): Boolean = element !is SuiteElement || !element.status.isPassed

    override val title: String
        get() = MyBundle.message("robot.output.editor.label.hide-passed-suite")
}
