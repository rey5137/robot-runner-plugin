package com.github.rey5137.robotrunnerplugin.editors.tree

import javax.swing.tree.DefaultMutableTreeNode

data class TreeNodeWrapper(val node: DefaultMutableTreeNode, val children: List<TreeNodeWrapper>)