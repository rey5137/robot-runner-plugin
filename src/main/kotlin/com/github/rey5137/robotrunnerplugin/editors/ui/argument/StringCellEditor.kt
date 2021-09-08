package com.github.rey5137.robotrunnerplugin.editors.ui.argument

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.UIUtil
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.DefaultCellEditor

class StringCellEditor : DefaultCellEditor(JBTextField()) {

    init {
        val textField = editorComponent as JBTextField
        textField.border = UIUtil.getTableFocusCellHighlightBorder()
        textField.isEditable = false
        textField.addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                textField.selectAll()
            }

            override fun focusLost(e: FocusEvent?) {}
        })
    }
}
