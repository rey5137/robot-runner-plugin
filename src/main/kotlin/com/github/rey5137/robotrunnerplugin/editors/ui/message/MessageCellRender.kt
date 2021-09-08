package com.github.rey5137.robotrunnerplugin.editors.ui.message

import com.github.rey5137.robotrunnerplugin.editors.ui.HighlightHolder
import com.github.rey5137.robotrunnerplugin.editors.ui.setHighlightBorder
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import icons.MyIcons
import javax.swing.JList

class MessageCellRender : ColoredListCellRenderer<HighlightHolder<MessageElement>>() {

    override fun customizeCellRenderer(
        list: JList<out HighlightHolder<MessageElement>>,
        value: HighlightHolder<MessageElement>,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        setHighlightBorder(value.highlight)

        val messageElement = value.value
        icon = when (messageElement.level) {
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
