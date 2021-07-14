package com.github.rey5137.robotrunnerplugin.editors.ui

import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent

fun JComponent.setHighlightBorder(type: HighlightType) {
    border = when(type) {
        HighlightType.UNMATCHED -> BorderFactory.createEmptyBorder()
        HighlightType.MATCHED -> BorderFactory.createLineBorder(Color.RED)
        HighlightType.CONTAINED -> BorderFactory.createLineBorder(Color(116, 181, 102))
    }
}