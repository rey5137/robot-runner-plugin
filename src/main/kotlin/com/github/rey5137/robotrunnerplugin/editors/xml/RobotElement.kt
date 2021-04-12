package com.github.rey5137.robotrunnerplugin.editors.xml

data class RobotElement(
    var generator: String = "",
    var generated: String = "",
    var suites: MutableList<SuiteElement> = ArrayList(),
) : Element
