package com.github.rey5137.robotrunnerplugin.editors.json

import com.github.rey5137.robotrunnerplugin.editors.xml.*
import org.json.JSONObject


class JsonParser(val robotElement: RobotElement) {

    var currentElement: Element = robotElement
    private val stack: MutableList<Element> = ArrayList()
    private var messageIndex = 0L
    private var docIndex = 0L
    private val keywordNameMap = mutableMapOf<String, Int>()
    private val keywordLibMap = mutableMapOf<String, Int>()

    fun addData(method: String, payload: JSONObject) {
        try {
            when (method) {
                METHOD_START_SUITE -> payload.toSuiteElement().apply {
                    currentElement.addSuite(this)
                    pushElement(this)
                }
                METHOD_START_TEST -> payload.toTestElement().apply {
                    currentElement.addTest(this)
                    pushElement(this)
                }
                METHOD_START_KEYWORD -> payload.toKeywordElement(
                    keywordNameMap,
                    keywordLibMap,
                    docIndex++,
                    robotElement
                ).apply {
                    if (this.type == KEYWORD_TYPE_STEP) {
                        currentElement.getStepKeywords()?.let { stepKeywords ->
                            stepKeywords.forEach { it.updateStepStatus() }
                            stepKeywords.clear()
                            stepKeywords.add(this)
                        }
                        currentElement.addKeyword(this)
                    } else if (!currentElement.isStepKeyword()) {
                        if (this.type == KEYWORD_TYPE_TEARDOWN) {
                            currentElement.getStepKeywords()?.let { stepKeywords ->
                                stepKeywords.forEach { it.updateStepStatus() }
                                stepKeywords.clear()
                            }
                            currentElement.addKeyword(this)
                        } else {
                            val stepKeywords = currentElement.getStepKeywords()
                            if (stepKeywords != null && stepKeywords.isNotEmpty()) {
                                stepKeywords.last().addKeyword(this)
                            } else
                                currentElement.addKeyword(this)
                        }
                    } else
                        currentElement.addKeyword(this)

                    pushElement(this)
                }
                METHOD_LOG_MESSAGE -> payload.toMessageElement(messageIndex++, robotElement).apply {
                    currentElement.addMessage(this)
                }
                METHOD_END_SUITE -> payload.toStatusElement().apply {
                    (currentElement as SuiteElement).status = this
                    popElement()
                }
                METHOD_END_TEST -> payload.toStatusElement().apply {
                    (currentElement as TestElement).status = this
                    (currentElement as TestElement).tags.addAll(payload.extractStringArray(FIELD_TAGS))
                    if(!currentElement.hasTeardownKeywords()) {
                        currentElement.getStepKeywords()?.let { stepKeywords ->
                            stepKeywords.forEach { it.updateStepStatus() }
                            stepKeywords.clear()
                        }
                    }
                    popElement()
                }
                METHOD_END_KEYWORD -> payload.toStatusElement().apply {
                    val keywordElement = currentElement as KeywordElement
                    keywordElement.status = this
                    keywordElement.tags.addAll(payload.extractStringArray(FIELD_TAGS))
                    keywordElement.arguments.addAll(payload.extractStringArray(FIELD_ARGS))
                    keywordElement.assigns.addAll(payload.extractStringArray(FIELD_ASSIGN))
                    if(keywordElement.type == KEYWORD_TYPE_STEP) {
                        keywordElement.status.status = ""
                        keywordElement.status.endTime = ""
                    }
                    else if(!keywordElement.hasTeardownKeywords()) {
                        keywordElement.stepKeywords.forEach { it.updateStepStatus() }
                        keywordElement.stepKeywords.clear()
                    }
                    popElement()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun pushElement(element: Element) {
        currentElement = element
        stack.add(currentElement)
    }

    private fun popElement(): Element {
        val element = stack.removeAt(stack.size - 1)
        if (stack.isNotEmpty())
            currentElement = stack.last()
        return element
    }

}

private fun JSONObject.toSuiteElement() = SuiteElement(
    id = getJSONObject(FIELD_ATTRS).getString(FIELD_ID),
    name = getString(FIELD_NAME),
    source = getJSONObject(FIELD_ATTRS).getString(FIELD_SOURCE),
)

private fun JSONObject.toTestElement() = TestElement(
    id = getJSONObject(FIELD_ATTRS).getString(FIELD_ID),
    name = getString(FIELD_NAME),
)

private fun JSONObject.toKeywordElement(
    keywordNameMap: MutableMap<String, Int>,
    keywordLibMap: MutableMap<String, Int>,
    docIndex: Long,
    robotElement: RobotElement
): KeywordElement {
    val name = getJSONObject(FIELD_ATTRS).getString(FIELD_KWNAME)
    val library = getJSONObject(FIELD_ATTRS).getString(FIELD_LIBNAME)
    val isStepKeyword = library == STEP_LIBRARY && name.equals(STEP_KEYWORD, ignoreCase = true)
    val nameIndex = keywordNameMap[name] ?: keywordNameMap.size.apply {
        keywordNameMap[name] = this
        robotElement.keywordNames.add(name)
    }
    val libIndex = keywordLibMap[library] ?: keywordLibMap.size.apply {
        keywordLibMap[library] = this
        robotElement.keywordLibraries.add(library)
    }
    val type = when(isStepKeyword) {
        true -> KEYWORD_TYPE_STEP
        else -> getJSONObject(FIELD_ATTRS).getString(FIELD_TYPE).toUpperCase()
    }
    robotElement.docMap[docIndex] = getJSONObject(FIELD_ATTRS).getString(FIELD_DOC)
    return KeywordElement(
        nameIndex = nameIndex,
        libraryIndex = libIndex,
        docIndex = docIndex,
        type = type,
        robotElement = robotElement,
    )
}

private fun JSONObject.toMessageElement(index: Long, robotElement: RobotElement): MessageElement {
    val message = getString(FIELD_MESSAGE)
    robotElement.messageMap[index] = message
    return MessageElement(
        timestamp = getString(FIELD_TIMESTAMP),
        level = getString(FIELD_LEVEL).toUpperCase(),
        valueIndex = index,
        robotElement = robotElement,
        title = message.extractMessageTitle()
    )
}

private fun JSONObject.toStatusElement() = StatusElement(
    startTime = getJSONObject(FIELD_ATTRS).getString(FIELD_STARTTIME),
    endTime = getJSONObject(FIELD_ATTRS).getString(FIELD_ENDTIME),
    status = getJSONObject(FIELD_ATTRS).getString(FIELD_STATUS),
    message = try {
        getJSONObject(FIELD_ATTRS).getString(FIELD_MESSAGE)
    } catch (ex: Exception) {
        ""
    }
)

private fun KeywordElement.updateStepStatus() {
    if (this.type == KEYWORD_TYPE_STEP) {
        keywords.lastOrNull()?.let { keyword ->
            this.status.status = keyword.status.status
            this.status.endTime = keyword.status.endTime
        }
    }
}


private fun JSONObject.extractStringArray(field: String): List<String> {
    val array = getJSONObject(FIELD_ATTRS).getJSONArray(field)
    return array.map { it.toString() }
}

private fun String.extractMessageTitle(): String {
    val end = Math.min(31, length - 1)
    (end downTo (end - end / 3)).forEach {
        if (this[it] == ' ')
            return substring(0, it)
    }
    return substring(0, end + 1)
}