package com.github.rey5137.robotrunnerplugin.editors.xml

data class KeywordElement(
    var nameIndex: Int,
    var libraryIndex: Int,
    var docIndex: Long,
    var type: String = "",
    var arguments: MutableList<String> = ArrayList(),
    var assigns: MutableList<String> = ArrayList(),
    override var tags: MutableList<String> = ArrayList(),
    var messages: MutableList<MessageElement> = ArrayList(),
    var keywords: MutableList<KeywordElement> = ArrayList(),
    val stepKeywords: MutableList<KeywordElement> = mutableListOf(),
    override var status: StatusElement = StatusElement(),
    val robotElement: RobotElement,
    var stepLevel: Int = 0,
    override var parent: Element? = null,
) : Element, HasCommonField, HasTagsField {

    override val name: String
        get() {
            val name = robotElement.keywordNames[nameIndex]
            if (type == KEYWORD_TYPE_STEP || type == KEYWORD_TYPE_END_STEP) {
                val num = findStepNum()
                val title = findStepTitle()
                return if(title.isNotEmpty())
                    "$num - $title"
                else
                    num
            }
            return name
        }

    val library: String
        get() {
            if (type == KEYWORD_TYPE_STEP || type == KEYWORD_TYPE_END_STEP)
                return ""
            return robotElement.keywordLibraries[libraryIndex]
        }

    val doc: String
        get() = robotElement.docMap[docIndex] ?: ""

    fun findStepNum(): String {
        val argument = arguments.find {
            it.startsWith("num=", ignoreCase = true)
        }
        return argument?.substring("num=".length) ?: if (arguments.isNotEmpty()) arguments[0] else ""
    }

    private fun findStepTitle(): String {
        val argument = arguments.find {
            it.startsWith("title=", ignoreCase = true)
        }
        return argument?.substring("title=".length) ?: if (arguments.size >= 2) arguments[1] else ""
    }

    override fun toString() = "KeywordElement[$name]"

}
