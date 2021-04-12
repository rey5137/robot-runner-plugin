package com.github.rey5137.robotrunnerplugin.editors.xml

data class SuiteElement(
    var id: String = "",
    override var name: String = "",
    var source: String = "",
    var suites: MutableList<SuiteElement> = ArrayList(),
    var tests: MutableList<TestElement> = ArrayList(),
    override var status: StatusElement = StatusElement(),
) : Element, HasCommonField
