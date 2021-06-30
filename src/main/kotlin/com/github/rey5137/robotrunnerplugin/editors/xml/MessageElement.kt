package com.github.rey5137.robotrunnerplugin.editors.xml

data class MessageElement(
    var timestamp: String = "",
    var level: String = "",
    var valueIndex: Long,
    var title: String = "",
    val robotElement: RobotElement,
) : Element {

    fun value() = robotElement.messageMap[valueIndex]

    override fun toString() = "MessageElement"
}
