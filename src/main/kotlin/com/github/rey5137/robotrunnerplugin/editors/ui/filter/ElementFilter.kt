package com.github.rey5137.robotrunnerplugin.editors.ui.filter

import com.github.rey5137.robotrunnerplugin.editors.xml.Element

interface ElementFilter {

    fun accept(element: Element): Boolean

    fun getTitle(): String

    var isEnabled: Boolean
}