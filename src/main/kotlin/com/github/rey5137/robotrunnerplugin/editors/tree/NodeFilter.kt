package com.github.rey5137.robotrunnerplugin.editors.tree

interface NodeFilter {

    fun accept(nodeWrapper: TreeNodeWrapper): Boolean

}