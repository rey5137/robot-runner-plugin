package com.github.rey5137.robotrunnerplugin.editors.ui

interface NodeFilter {

    fun accept(nodeWrapper: TreeNodeWrapper): Boolean

}