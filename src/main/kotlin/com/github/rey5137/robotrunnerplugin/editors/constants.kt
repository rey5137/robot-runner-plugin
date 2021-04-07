package com.github.rey5137.robotrunnerplugin.editors

import com.intellij.util.xml.*

const val TAG_ROBOT = "robot"

interface RobotDomElement : DomElement {

    @get: Attribute("generator")
    val generator: GenericAttributeValue<String>

    @get: Attribute("generated")
    val generated: GenericAttributeValue<String>

    @get: SubTagList("suite")
    val suites: List<SuiteDomElement>
}

interface SuiteDomElement: DomElement {

    @get: Attribute("id")
    val id: GenericAttributeValue<String>

    @get: Attribute("name")
    val name: GenericAttributeValue<String>

    @get: Attribute("source")
    val source: GenericAttributeValue<String>

    @get: SubTagList("suite")
    val suites: List<SuiteDomElement>

    @get: SubTagList("test")
    val tests: List<TestDomElement>

    @get: SubTag("status")
    val status: StatusDomElement
}

interface TestDomElement: DomElement {

    @get: Attribute("id")
    val id: GenericAttributeValue<String>

    @get: Attribute("name")
    val name: GenericAttributeValue<String>

    @get: SubTagList("kw")
    val keywords: List<KeywordDomElement>

    @get: SubTag("status")
    val status: StatusDomElement
}

interface KeywordDomElement: DomElement {

    @get: Attribute("name")
    val name: GenericAttributeValue<String>

    @get: Attribute("library")
    val library: GenericAttributeValue<String>

    @get: SubTag("arguments")
    val arguments: ArgumentsDomElement

    @get: SubTag("tags")
    val tags: TagsDomElement

    @get: SubTag("doc")
    val doc: DocDomElement

    @get: SubTag("msg")
    val message: MessageDomElement

    @get: SubTagList("kw")
    val keywords: List<KeywordDomElement>

    @get: SubTag("status")
    val status: StatusDomElement

}

interface ArgumentDomElement: DomElement {

    @get: TagValue
    val value: String

}

interface ArgumentsDomElement: DomElement {

    @get: SubTagList("arg")
    val items: List<ArgumentDomElement>

}

interface TagDomElement: DomElement {

    @get: TagValue
    val value: String

}

interface TagsDomElement: DomElement {

    @get: SubTagList("tag")
    val items: List<TagDomElement>

}

interface DocDomElement: DomElement {

    @get: TagValue
    val value: String

}

interface MessageDomElement: DomElement {

    @get: Attribute("timestamp")
    val timestamp: GenericAttributeValue<String>

    @get: Attribute("level")
    val level: GenericAttributeValue<String>

    @get: TagValue
    val value: String

}

interface StatusDomElement: DomElement {

    @get: Attribute("status")
    val status: GenericAttributeValue<String>

    @get: Attribute("starttime")
    val starttime: GenericAttributeValue<String>

    @get: Attribute("endtime")
    val endtime: GenericAttributeValue<String>

}