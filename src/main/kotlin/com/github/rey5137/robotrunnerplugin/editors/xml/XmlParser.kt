package com.github.rey5137.robotrunnerplugin.editors.xml

import com.intellij.openapi.vfs.VirtualFile
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement

fun VirtualFile.parseXml(): RobotElement {
    val xmlInputFactory = XMLInputFactory.newInstance()
    val reader = xmlInputFactory.createXMLEventReader(this.inputStream)
    val stack: MutableList<Element> = ArrayList()
    lateinit var currentElement: Element
    lateinit var robotElement: RobotElement
    var skipCount = 0
    var messageIndex = 0L
    var docIndex = 0L
    val keywordNameMap = mutableMapOf<String, Int>()
    val keywordLibMap = mutableMapOf<String, Int>()
    while (reader.hasNext()) {
        val nextEvent = reader.nextEvent()
        if (nextEvent.isStartElement) {
            if (skipCount > 0)
                skipCount++
            else {
                val startElement = nextEvent.asStartElement()
                val newElement = when (startElement.name.localPart) {
                    TAG_ROBOT -> startElement.toRobotElement().apply {
                        robotElement = this
                    }
                    TAG_SUITE -> startElement.toSuiteElement().apply {
                        currentElement.addSuite(this)
                    }
                    TAG_TEST -> startElement.toTestElement().apply {
                        currentElement.addTest(this)
                    }
                    TAG_KEYWORD -> startElement.toKeywordElement(keywordNameMap, keywordLibMap, docIndex++, robotElement).apply {
                        if (this.type == KEYWORD_TYPE_STEP) {
                            currentElement.getStepKeywords()?.add(this)
                            currentElement.addKeyword(this)
                        } else if (!currentElement.isStepKeyword()) {
                            if (this.type == KEYWORD_TYPE_TEARDOWN) {
                                currentElement.getStepKeywords()?.let { stepKeywords ->
                                    stepKeywords.asReversed().forEach { it.updateStepStatus() }
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
                    }
                    TAG_STATUS -> startElement.toStatusElement().apply {
                        currentElement.addStatus(this)
                    }
                    TAG_MESSAGE -> startElement.toMessageElement(messageIndex++, robotElement).apply {
                        currentElement.addMessage(this)
                    }
                    TAG_ARGUMENTS -> ArgumentsElement()
                    TAG_ASSIGN -> AssignsElement()
                    TAG_TAGS -> TagsElement()
                    TAG_ARGUMENT, TAG_VAR, TAG_TAG -> StringElement(xmlTag = startElement.name.localPart)
                    else -> {
                        skipCount++
                        continue
                    }
                }
                currentElement = newElement
                stack.add(currentElement)
            }
        } else if (nextEvent.isCharacters) {
            val data = nextEvent.asCharacters().data
            when (currentElement) {
                is StringElement -> currentElement.value.append(data)
                is MessageElement,
                is StatusElement -> {
                    val newElement = StringElement(xmlTag = TAG_STATUS)
                    newElement.value.append(data)
                    currentElement = newElement
                    stack.add(currentElement)
                }
            }
        } else if (nextEvent.isEndElement) {
            if (skipCount > 0)
                skipCount--
            else {
                val element = stack.removeAt(stack.size - 1)
                if (stack.isNotEmpty()) {
                    currentElement = stack.last()
                    when (element) {
                        is StringElement -> {
                            when (currentElement) {
                                is MessageElement -> {
                                    val text = element.value.toString().trim()
                                    currentElement.title = text.extractMessageTitle()
                                    robotElement.messageMap[currentElement.valueIndex] = text
                                    stack.removeAt(stack.size - 1)
                                    currentElement = stack.last()
                                }
                                is StatusElement -> {
                                    val text = element.value.toString().trim()
                                    currentElement.message = text
                                    stack.removeAt(stack.size - 1)
                                    currentElement = stack.last()
                                }
                                is KeywordElement -> {
                                    when (element.xmlTag) {
                                        TAG_ARGUMENT -> currentElement.arguments.add(element.value.toString())
                                        TAG_VAR -> currentElement.assigns.add(element.value.toString())
                                        TAG_TAG -> currentElement.tags.add(element.value.toString())
                                    }
                                }
                                else -> currentElement.addString(element)
                            }
                        }
                        is ArgumentsElement -> (currentElement as KeywordElement).arguments.addAll(element.arguments)
                        is AssignsElement -> (currentElement as KeywordElement).assigns.addAll(element.vars)
                        is TagsElement -> currentElement.addTags(element)
                        is TestElement -> {
                            if(!element.hasTeardownKeywords()) {
                                element.getStepKeywords()?.let { stepKeywords ->
                                    stepKeywords.asReversed().forEach { it.updateStepStatus() }
                                    stepKeywords.clear()
                                }
                            }
                        }
                        is KeywordElement -> {
                            if(element.type == KEYWORD_TYPE_STEP) {
                                element.updateStepLevel()
                                val parentElement = element.parent!!
                                val stepKeywords = parentElement.getStepKeywords()!!
                                stepKeywords.remove(element)
                                while(stepKeywords.isNotEmpty() && stepKeywords.last().stepLevel >= element.stepLevel) {
                                    val lastStep = stepKeywords.removeAt(stepKeywords.size - 1)
                                    lastStep.updateStepStatus()
                                }
                                if(stepKeywords.isNotEmpty()) {
                                    parentElement.removeKeyword(element)
                                    stepKeywords.last().addKeyword(element)
                                }
                                stepKeywords.add(element)
                            }
                            else if(element.type == KEYWORD_TYPE_END_STEP) {
                                if(element.parent!!.isStepKeyword()) {
                                    val stepElement = element.parent as KeywordElement
                                    stepElement.updateStepStatus()
                                    stepElement.findStepRoot()?.getStepKeywords()?.remove(stepElement)
                                }
                            }
                            else if(!element.hasTeardownKeywords()) {
                                element.getStepKeywords()?.let { stepKeywords ->
                                    stepKeywords.asReversed().forEach { it.updateStepStatus() }
                                    stepKeywords.clear()
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    return robotElement
}

fun VirtualFile.extractFailedTestCases(): List<String> {
    val xmlInputFactory = XMLInputFactory.newInstance()
    val reader = xmlInputFactory.createXMLEventReader(this.inputStream)
    val result: MutableList<String> = ArrayList()
    var currentTestName = ""
    var skipCount = 0
    while (reader.hasNext()) {
        val nextEvent = reader.nextEvent()
        if (nextEvent.isStartElement) {
            if (skipCount > 0)
                skipCount++
            else {
                val startElement = nextEvent.asStartElement()
                when (startElement.name.localPart) {
                    TAG_ROBOT, TAG_SUITE -> {
                    }
                    TAG_TEST -> {
                        currentTestName = startElement.getAttributeByName(QName(TAG_NAME))?.value ?: ""
                    }
                    TAG_STATUS -> {
                        if (currentTestName.isNotEmpty()) {
                            val status = startElement.getAttributeByName(QName(TAG_STATUS))?.value ?: ""
                            val isPassed = "PASS".equals(status, ignoreCase = true)
                            if (!isPassed)
                                result.add(currentTestName)
                            currentTestName = ""
                        }
                    }
                    else -> {
                        skipCount++
                        continue
                    }
                }
            }
        } else if (nextEvent.isEndElement) {
            if (skipCount > 0)
                skipCount--
        }
    }
    return result
}

private fun StartElement.toRobotElement() = RobotElement(
    generator = getAttributeByName(QName(TAG_GENERATOR))?.value ?: "",
    generated = getAttributeByName(QName(TAG_GENERATED))?.value ?: "",
)

private fun StartElement.toSuiteElement() = SuiteElement(
    id = getAttributeByName(QName(TAG_ID))?.value ?: "",
    name = getAttributeByName(QName(TAG_NAME))?.value ?: "",
    source = getAttributeByName(QName(TAG_SOURCE))?.value ?: "",
)

private fun StartElement.toTestElement() = TestElement(
    id = getAttributeByName(QName(TAG_ID))?.value ?: "",
    name = getAttributeByName(QName(TAG_NAME))?.value ?: "",
)

private fun StartElement.toKeywordElement(keywordNameMap: MutableMap<String, Int>, keywordLibMap: MutableMap<String, Int>, docIndex: Long, robotElement: RobotElement): KeywordElement {
    val name = getAttributeByName(QName(TAG_NAME))?.value ?: ""
    val library = getAttributeByName(QName(TAG_LIBRARY))?.value ?: (getAttributeByName(QName(TAG_OWNER))?.value ?: "")
    val isStepKeyword = library == STEP_LIBRARY && name.equals(STEP_KEYWORD, ignoreCase = true)
    val isEndStepKeyword = library == STEP_LIBRARY && name.equals(END_STEP_KEYWORD, ignoreCase = true)
    val nameIndex = keywordNameMap[name] ?: keywordNameMap.size.apply {
        keywordNameMap[name] = this
        robotElement.keywordNames.add(name)
    }
    val libIndex = keywordLibMap[library] ?: keywordLibMap.size.apply {
        keywordLibMap[library] = this
        robotElement.keywordLibraries.add(library)
    }
    val type = when {
        isStepKeyword -> KEYWORD_TYPE_STEP
        isEndStepKeyword -> KEYWORD_TYPE_END_STEP
        else -> getAttributeByName(QName(TAG_TYPE))?.value?.uppercase() ?: ""
    }

    robotElement.docMap[docIndex] = getAttributeByName(QName(TAG_DOC))?.value ?: ""
    return KeywordElement(
        nameIndex = nameIndex,
        libraryIndex = libIndex,
        docIndex = docIndex,
        type = type,
        robotElement = robotElement
    )
}

private fun StartElement.toStatusElement() = StatusElement(
    status = getAttributeByName(QName(TAG_STATUS))?.value ?: "",
    startTime = getAttributeByName(QName(TAG_START_TIME))?.value ?: (getAttributeByName(QName(TAG_START))?.value ?: ""),
    endTime = getAttributeByName(QName(TAG_END_TIME))?.value ?: "",
    elapsed = getAttributeByName(QName(TAG_ELAPSED))?.value ?: "",
)

private fun StartElement.toMessageElement(index: Long, robotElement: RobotElement) = MessageElement(
    timestamp = getAttributeByName(QName(TAG_TIMESTAMP))?.value ?: (getAttributeByName(QName(TAG_TIME))?.value ?: ""),
    level = getAttributeByName(QName(TAG_LEVEL))?.value?.uppercase() ?: "",
    valueIndex = index,
    robotElement = robotElement
)

private fun KeywordElement.findStepRoot(): Element? {
    var element: Element? = this
    do {
        element = element?.getParent()
    } while(element != null && element.isStepKeyword())
    return element
}

private fun String.extractMessageTitle(): String {
    val end = Math.min(31, length - 1)
    (end downTo (end - end / 3)).forEach {
        if (this[it] == ' ')
            return substring(0, it)
    }
    return substring(0, end + 1)
}

data class ArgumentsElement(
    val arguments: MutableList<String> = ArrayList()
) : Element

data class TagsElement(
    val tags: MutableList<String> = ArrayList()
) : Element

data class AssignsElement(
    val vars: MutableList<String> = ArrayList()
) : Element

data class StringElement(
    var value: StringBuilder = StringBuilder(),
    val xmlTag: String
) : Element