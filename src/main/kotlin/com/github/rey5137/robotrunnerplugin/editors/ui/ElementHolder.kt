package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.xml.Element

data class ElementHolder<T: Element>(
    val element: T,
    val highlight: Boolean = false,
)
