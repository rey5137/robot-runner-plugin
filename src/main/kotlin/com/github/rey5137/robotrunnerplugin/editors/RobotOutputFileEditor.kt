package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.editors.tree.NodeFilter
import com.github.rey5137.robotrunnerplugin.editors.tree.TreeNodeWrapper
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.ui.*
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import com.intellij.util.xml.DomManager
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath


class RobotOutputFileEditor(private val project: Project, private val srcFile: VirtualFile) : UserDataHolderBase(),
    FileEditor {

    private val robotTreeNodeWrapper by lazy { getRootNodeWrapper() }

    private val myComponent by lazy { buildComponent() }
    private val treeModel by lazy { DefaultTreeModel(robotTreeNodeWrapper.node) }
    private val tree by lazy { Tree(treeModel) }

    private var nodeFilter: NodeFilter = ShowAllFilter

    override fun dispose() {}

    override fun getComponent(): JComponent {
        return myComponent
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun getName(): String = "Robot Result"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    private fun getRootNodeWrapper(): TreeNodeWrapper {
        val manager = DomManager.getDomManager(project)
        val xmlFile = PsiManager.getInstance(project).findFile(srcFile) as XmlFile
        val robotDomElement = manager.getFileElement(xmlFile, RobotDomElement::class.java)!!.rootElement
        return robotDomElement.toNode()
    }

    private fun buildComponent(): JComponent {
        val rootPanel = JPanel(BorderLayout())
        val splitter = JBSplitter(0.3F)
        val leftPanel = buildLeftPanel()
        val rightPanel = buildRightPanel()

        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
        splitter.firstComponent = leftPanel
        splitter.setHonorComponentsMinimumSize(true)
        rightPanel.border = JBUI.Borders.empty(15, 5, 0, 15)
        splitter.secondComponent = rightPanel

        rootPanel.add(splitter, BorderLayout.CENTER)
        return rootPanel
    }

    private fun buildLeftPanel(): JPanel {
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.cellRenderer = MyTreeCellRenderer()

        populateTree(false)

        TreeUtil.installActions(tree)
        TreeSpeedSearch(tree) { o ->
            val node = o.lastPathComponent as DefaultMutableTreeNode
            when (val userObject = node.userObject) {
                is SuiteDomElement -> userObject.name.value ?: ""
                is TestDomElement -> userObject.name.value ?: ""
                is KeywordDomElement -> userObject.name.value ?: ""
                else -> o.toString()
            }
        }

        val toolbarDecorator = ToolbarDecorator.createDecorator(tree)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .setMinimumSize(JBDimension(200, 200))
            .setForcedDnD()
            .addExtraAction(object : ToggleActionButton("Show keyword", AllIcons.Nodes.Method) {
                override fun isSelected(e: AnActionEvent?): Boolean = nodeFilter == ShowAllFilter

                override fun setSelected(e: AnActionEvent?, state: Boolean) {
                    nodeFilter = if(state) ShowAllFilter else HideKeywordFilter
                    populateTree(true)
                }

            })
        val panel = JPanel(BorderLayout())
        panel.background = JBColor.background()
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER)

        return panel
    }

    private fun buildRightPanel(): JPanel {
        return JPanel()
    }

    private fun populateTree(keepExpanded: Boolean) {
        var expandedPaths = emptyList<TreePath>()
        if(keepExpanded)
            expandedPaths = TreeUtil.collectExpandedPaths(tree)

        robotTreeNodeWrapper.rebuildNode()
        treeModel.reload()

        if(keepExpanded)
            TreeUtil.restoreExpandedPaths(tree, expandedPaths)
        else
            TreeUtil.expandAll(tree)
    }

    private fun TreeNodeWrapper.rebuildNode(): DefaultMutableTreeNode {
        node.removeAllChildren()
        children.forEach {
            if(nodeFilter.accept(it))
                node.add(it.rebuildNode())
        }
        return node
    }

    private fun RobotDomElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        suites.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun SuiteDomElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        suites.forEach { children.add(it.toNode()) }
        tests.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun TestDomElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun KeywordDomElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    internal class MyTreeCellRenderer : ColoredTreeCellRenderer() {

        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ) {
            if (value !is DefaultMutableTreeNode) {
                return
            }
            when (val userObject = value.userObject) {
                is RobotDomElement -> {
                    append("Robot", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    icon = AllIcons.Nodes.Package
                }
                is SuiteDomElement -> {
                    append(userObject.name.value ?: "", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    icon = AllIcons.Nodes.Package
                }
                is TestDomElement -> {
                    append(userObject.name.value ?: "", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    icon = AllIcons.Nodes.Class
                }
                is KeywordDomElement -> {
                    append(userObject.name.value ?: "", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                    icon = AllIcons.Nodes.Method
                }
            }
        }

    }

    object ShowAllFilter : NodeFilter {

        override fun accept(nodeWrapper: TreeNodeWrapper) = true

    }

    object HideKeywordFilter : NodeFilter {

        override fun accept(nodeWrapper: TreeNodeWrapper) = nodeWrapper.node.userObject !is KeywordDomElement

    }


}