package com.github.rey5137.robotrunnerplugin.editors.xml

data class TestElement(
    var id: String = "",
    override var name: String = "",
    var keywords: MutableList<KeywordElement> = ArrayList(),
    override var tags: MutableList<String> = ArrayList(),
    override var status: StatusElement = StatusElement(),
    override var parent: Element? = null,
) : Element, HasCommonField, HasTagsField {

    override fun toString() = "TestElement[$name]"
}
