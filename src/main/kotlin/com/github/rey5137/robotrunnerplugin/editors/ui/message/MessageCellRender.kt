package com.github.rey5137.robotrunnerplugin.editors.ui.message

import com.github.rey5137.robotrunnerplugin.editors.xml.LOG_LEVEL_DEBUG
import com.github.rey5137.robotrunnerplugin.editors.xml.LOG_LEVEL_INFO
import com.github.rey5137.robotrunnerplugin.editors.xml.LOG_LEVEL_TRACE
import com.github.rey5137.robotrunnerplugin.editors.xml.MessageElement
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import icons.MyIcons
import javax.swing.JList

class MessageCellRender : ColoredListCellRenderer<MessageElement>() {

    override fun customizeCellRenderer(
        list: JList<out MessageElement>,
        value: MessageElement,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        icon = when(value.level) {
            LOG_LEVEL_INFO -> MyIcons.LevelInfo
            LOG_LEVEL_DEBUG -> MyIcons.LevelDebug
            LOG_LEVEL_TRACE -> MyIcons.LevelTrace
            else -> null
        }
        isIconOnTheRight = false
        append("${value.title} ...", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

}