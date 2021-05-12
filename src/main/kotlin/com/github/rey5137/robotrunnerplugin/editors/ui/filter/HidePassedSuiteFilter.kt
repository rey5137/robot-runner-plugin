package com.github.rey5137.robotrunnerplugin.editors.ui.filter

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.xml.Element
import com.github.rey5137.robotrunnerplugin.editors.xml.SuiteElement

class HidePassedSuiteFilter(override var isEnabled: Boolean) : ElementFilter {

    override fun accept(element: Element): Boolean = element !is SuiteElement || !element.status.isPassed

    override fun getTitle(): String = MyBundle.message("robot.output.editor.label.hide-passed-suite")

}