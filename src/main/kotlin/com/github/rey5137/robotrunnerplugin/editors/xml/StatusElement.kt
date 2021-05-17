package com.github.rey5137.robotrunnerplugin.editors.xml

data class StatusElement(
    var status: String = "",
    var startTime: String = "",
    var endTime: String = "",
    var message: String = "",
) : Element {

    val isPassed = "PASS".equals(status, ignoreCase = true)

}
