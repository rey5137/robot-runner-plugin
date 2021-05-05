package com.github.rey5137.robotrunnerplugin.editors.ui.message

import com.github.rey5137.robotrunnerplugin.editors.xml.MessageElement
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

class MessageCellRender : ColoredListCellRenderer<MessageElement>() {

    override fun customizeCellRenderer(
        list: JList<out MessageElement>,
        value: MessageElement,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        append("${value.level.appendSpaceIfNeed(6)} - ", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
        append("${value.title} ...", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

    private fun String.appendSpaceIfNeed(minLength: Int): String {
        if(length >= minLength)
            return this
        val builder = StringBuilder()
        repeat(length - minLength) { builder.append(' ') }
        builder.append(this)
        return builder.toString()
    }
}