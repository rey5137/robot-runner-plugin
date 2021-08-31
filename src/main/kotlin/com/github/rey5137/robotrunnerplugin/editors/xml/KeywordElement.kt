package com.github.rey5137.robotrunnerplugin.editors.xml

data class KeywordElement(
    var nameIndex: Int,
    var libraryIndex: Int,
    var docIndex: Long,
    var type: String = "",
    var arguments: MutableList<String> = ArrayList(),
    var assigns: MutableList<String> = ArrayList(),
    override var tags: MutableList<String> = ArrayList(),
    var messages: MutableList<MessageElement> = ArrayList(),
    var keywords: MutableList<KeywordElement> = ArrayList(),
    override var status: StatusElement = StatusElement(),
    val robotElement: RobotElement,
    override var parent: Element? = null,
) : Element, HasCommonField, HasTagsField {

    override val name: String
        get() = robotElement.keywordNames[nameIndex]

    val library: String
        get() = robotElement.keywordLibraries[libraryIndex]

    val doc: String
        get() = robotElement.docMap[docIndex] ?: ""

    override fun toString() = "KeywordElement[$name]"
}
