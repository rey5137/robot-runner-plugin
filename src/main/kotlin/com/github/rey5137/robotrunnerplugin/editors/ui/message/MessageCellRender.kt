package com.github.rey5137.robotrunnerplugin.editors.ui.message

import com.github.rey5137.robotrunnerplugin.editors.ui.ElementHolder
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import icons.MyIcons
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JList

class MessageCellRender : ColoredListCellRenderer<ElementHolder<MessageElement>>() {

    override fun customizeCellRenderer(
        list: JList<out ElementHolder<MessageElement>>,
        value: ElementHolder<MessageElement>,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        border = if (value.highlight)
            BorderFactory.createLineBorder(Color.RED)
        else
            BorderFactory.createEmptyBorder()

        val messageElement = value.element
        icon = when(messageElement.level) {
            LOG_LEVEL_INFO -> MyIcons.LevelInfo
            LOG_LEVEL_DEBUG -> MyIcons.LevelDebug
            LOG_LEVEL_TRACE -> MyIcons.LevelTrace
            LOG_LEVEL_FAIL -> MyIcons.LevelFail
            LOG_LEVEL_ERROR -> MyIcons.LevelError
            else -> null
        }
        isIconOnTheRight = false
        append("${messageElement.title} ...", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

}