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
    var skipCount = 0
    while (reader.hasNext()) {
        val nextEvent = reader.nextEvent()
        if (nextEvent.isStartElement) {
            if (skipCount > 0)
                skipCount++
            else {
                val startElement = nextEvent.asStartElement()
                val newElement = when (startElement.name.localPart) {
                    TAG_ROBOT -> startElement.toRobotElement()
                    TAG_SUITE -> startElement.toSuiteElement().apply {
                        currentElement.addSuite(this)
                    }
                    TAG_TEST -> startElement.toTestElement().apply {
                        currentElement.addTest(this)
                    }
                    TAG_KEYWORD -> startElement.toKeywordElement().apply {
                        currentElement.addKeyword(this)
                    }
                    TAG_STATUS -> startElement.toStatusElement().apply {
                        currentElement.addStatus(this)
                    }
                    TAG_MESSAGE -> startElement.toMessageElement().apply {
                        this.value = reader.nextEvent().asCharacters().data
                    }
                    TAG_ARGUMENTS -> ArgumentsElement()
                    TAG_ASSIGN -> AssignsElement()
                    TAG_TAGS -> TagsElement()
                    TAG_ARGUMENT, TAG_VAR, TAG_TAG -> StringElement(value = reader.nextEvent().asCharacters().data)
                    else -> {
                        skipCount++
                        continue
                    }
                }
                currentElement = newElement
                stack.add(currentElement)
            }
        }
        if (nextEvent.isEndElement) {
            if (skipCount > 0)
                skipCount--
            else {
                val element = stack.removeAt(stack.size - 1)
                if (stack.isNotEmpty()) {
                    currentElement = stack.last()
                    when(element) {
                        is StringElement -> currentElement.addString(element)
                        is ArgumentsElement -> (currentElement as KeywordElement).arguments = element.arguments
                        is AssignsElement -> (currentElement as KeywordElement).assigns = element.vars
                        is TagsElement -> currentElement.addTags(element)
                        is MessageElement -> (currentElement as KeywordElement).messages.add(element)
                    }
                }

            }
        }
    }
    return currentElement as RobotElement
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

private fun StartElement.toKeywordElement() = KeywordElement(
    name = getAttributeByName(QName(TAG_NAME))?.value ?: "",
    library = getAttributeByName(QName(TAG_LIBRARY))?.value ?: "",
    document = getAttributeByName(QName(TAG_DOC))?.value ?: "",
)

private fun StartElement.toStatusElement() = StatusElement(
    status = getAttributeByName(QName(TAG_STATUS))?.value ?: "",
    startTime = getAttributeByName(QName(TAG_START_TIME))?.value ?: "",
    endTime = getAttributeByName(QName(TAG_END_TIME))?.value ?: "",
)

private fun StartElement.toMessageElement() = MessageElement(
    timestamp = getAttributeByName(QName(TAG_TIMESTAMP))?.value ?: "",
    level = getAttributeByName(QName(TAG_LEVEL))?.value ?: "",
)

private fun Element.addSuite(suite: SuiteElement) {
    when (this) {
        is SuiteElement -> suites.add(suite)
        is RobotElement -> suites.add(suite)
    }
}

private fun Element.addTest(test: TestElement) {
    when (this) {
        is SuiteElement -> tests.add(test)
    }
}

private fun Element.addKeyword(keyword: KeywordElement) {
    when (this) {
        is TestElement -> keywords.add(keyword)
        is KeywordElement -> keywords.add(keyword)
    }
}

private fun Element.addStatus(status: StatusElement) {
    when (this) {
        is SuiteElement -> this.status = status
        is TestElement -> this.status = status
        is KeywordElement -> this.status = status
    }
}

private fun Element.addString(element: StringElement) {
    when (this) {
        is ArgumentsElement -> arguments.add(element.value)
        is TagsElement -> tags.add(element.value)
        is AssignsElement -> vars.add(element.value)
    }
}

private fun Element.addTags(element: TagsElement) {
    when (this) {
        is TestElement -> this.tags = element.tags
        is KeywordElement -> this.tags = element.tags
    }
}

data class ArgumentsElement (
    val arguments: MutableList<String> = ArrayList()
) : Element

data class TagsElement (
    val tags: MutableList<String> = ArrayList()
) : Element

data class AssignsElement (
    val vars: MutableList<String> = ArrayList()
) : Element

data class StringElement (
    var value: String = ""
) : Element