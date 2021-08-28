package com.github.rey5137.robotrunnerplugin.editors.ui

import javax.swing.tree.DefaultMutableTreeNode

data class TreeNodeWrapper(val node: DefaultMutableTreeNode, val children: MutableList<TreeNodeWrapper>)