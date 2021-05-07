package com.github.rey5137.robotrunnerplugin.editors.ui.message

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.LayeredIcon
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
            LOG_LEVEL_INFO -> LayeredIcon(MyIcons.ElementPass, MyIcons.LabelInfo)
            LOG_LEVEL_DEBUG -> LayeredIcon(MyIcons.ElementPass, MyIcons.LabelDebug)
            LOG_LEVEL_TRACE -> LayeredIcon(MyIcons.ElementPass, MyIcons.LabelTrace)
            LOG_LEVEL_FAIL -> LayeredIcon(MyIcons.ElementFail, MyIcons.LabelFail)
            else -> null
        }
        isIconOnTheRight = false
        append("${value.title} ...", SimpleTextAttributes.REGULAR_ATTRIBUTES)
    }

}