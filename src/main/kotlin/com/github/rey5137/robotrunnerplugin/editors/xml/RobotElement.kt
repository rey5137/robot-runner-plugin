package com.github.rey5137.robotrunnerplugin.editors.xml

import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.util.concurrent.ConcurrentMap

data class RobotElement(
    var generator: String = "",
    var generated: String = "",
    var suites: MutableList<SuiteElement> = ArrayList(),
    val db: DB = DBMaker.memoryDirectDB().closeOnJvmShutdown().make()
) : Element {

    val messageMap: ConcurrentMap<Long, String> by lazy { db.hashMap("message", Serializer.LONG, Serializer.STRING).createOrOpen() }

    val keywordNames = mutableListOf<String>()

    val keywordLibraries = mutableListOf<String>()

    val docMap: ConcurrentMap<Long, String> by lazy { db.hashMap("doc", Serializer.LONG, Serializer.STRING).createOrOpen() }

}

