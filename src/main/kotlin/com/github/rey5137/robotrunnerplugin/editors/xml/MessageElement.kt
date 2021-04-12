package com.github.rey5137.robotrunnerplugin.editors.xml

data class MessageElement(
    var timestamp: String = "",
    var level: String = "",
    var value: String = "",
) : Element
