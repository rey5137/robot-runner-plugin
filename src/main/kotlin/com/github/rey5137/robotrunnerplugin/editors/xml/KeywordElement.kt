package com.github.rey5137.robotrunnerplugin.editors.xml

data class KeywordElement(
    override var name: String = "",
    var library: String = "",
    var document: String = "",
    var arguments: MutableList<String> = ArrayList(),
    var assigns: MutableList<String> = ArrayList(),
    override var tags: MutableList<String> = ArrayList(),
    var messages: MutableList<MessageElement> = ArrayList(),
    var keywords: MutableList<KeywordElement> = ArrayList(),
    override var status: StatusElement = StatusElement(),
) : Element, HasCommonField, HasTagsField
