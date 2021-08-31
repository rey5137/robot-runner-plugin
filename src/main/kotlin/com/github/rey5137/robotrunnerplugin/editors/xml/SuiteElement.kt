package com.github.rey5137.robotrunnerplugin.editors.xml

data class SuiteElement(
    var id: String = "",
    override var name: String = "",
    var source: String = "",
    var children: MutableList<Element> = ArrayList(),
    override var status: StatusElement = StatusElement(),
    override var parent: Element? = null,
) : Element, HasCommonField {

    override fun toString() = "SuiteElement[$name]"
}
