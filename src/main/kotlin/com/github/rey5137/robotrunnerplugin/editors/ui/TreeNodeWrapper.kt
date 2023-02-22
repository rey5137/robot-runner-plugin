package com.github.rey5137.robotrunnerplugin.editors.ui

import javax.swing.tree.DefaultMutableTreeNode

data class TreeNodeWrapper(
    val node: DefaultMutableTreeNode,
    val children: MutableList<TreeNodeWrapper>,
    var stepChildren: MutableList<TreeNodeWrapper>? = null
) {

    val hasStepChild
        get() = stepChildren?.isNotEmpty() ?: false

    val lastStepChild
        get() = stepChildren?.lastOrNull()

}