package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.editors.ui.DetailsPanel
import com.github.rey5137.robotrunnerplugin.editors.ui.NodeFilter
import com.github.rey5137.robotrunnerplugin.editors.ui.TreeNodeWrapper
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import icons.MyIcons
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
    private val detailsPanel by lazy { DetailsPanel(robotTreeNodeWrapper.node.userObject as RobotElement) }

    private var nodeFilter: NodeFilter = ShowAllFilter

    override fun dispose() {
        val robotElement = robotTreeNodeWrapper.node.userObject as RobotElement
        robotElement.db.close()
    }

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

    private fun getRootNodeWrapper(): TreeNodeWrapper = srcFile.parseXml().toNode()

    private fun buildComponent(): JComponent {
        val rootPanel = JPanel(BorderLayout())
        val splitter = JBSplitter(0.3F)
        val leftPanel = buildLeftPanel()
        val rightPanel = detailsPanel

        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
        splitter.firstComponent = leftPanel
        splitter.setHonorComponentsMinimumSize(true)
        rightPanel.border = JBUI.Borders.empty(10, 0, 0, 10)
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
                is SuiteElement -> userObject.name
                is TestElement -> userObject.name
                is KeywordElement -> userObject.name
                else -> o.toString()
            }
        }

        tree.addTreeSelectionListener {
            val selectionPath = tree.selectionPath
            if (selectionPath != null) {
                val node = selectionPath.lastPathComponent as DefaultMutableTreeNode
                detailsPanel.showDetails(node.userObject as Element)
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

    private fun RobotElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        suites.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun SuiteElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        suites.forEach { children.add(it.toNode()) }
        tests.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun TestElement.toNode(): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEach { children.add(it.toNode()) }
        return TreeNodeWrapper(node = DefaultMutableTreeNode(this), children = children)
    }

    private fun KeywordElement.toNode(): TreeNodeWrapper {
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
                is SuiteElement -> {
                    icon = if(userObject.suites.isEmpty())
                        if(userObject.status.isPassed) MyIcons.SuitePass else MyIcons.SuiteFail
                    else
                        if(userObject.status.isPassed) MyIcons.FolderPass else MyIcons.FolderFail
                    append(userObject.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is TestElement -> {
                    icon = if(userObject.status.isPassed) MyIcons.TestPass else MyIcons.TestFail
                    append(userObject.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is KeywordElement -> {
                    icon = if(userObject.status.isPassed) MyIcons.KeywordPass else MyIcons.KeywordFail
                    if(userObject.assigns.isNotEmpty()) {
                        append( userObject.assigns.joinToString(separator = ", "), SimpleTextAttributes.REGULAR_ATTRIBUTES)
                        append(" = ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    }
                    if(userObject.library.isNotBlank())
                        append("${userObject.library}.", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                    append(userObject.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    if(userObject.arguments.isNotEmpty()) {
                        append(" ")
                        append(
                            userObject.arguments.joinToString(separator = ", "),
                            SimpleTextAttributes.GRAY_ATTRIBUTES
                        )
                    }
                }
            }
        }

    }

    object ShowAllFilter : NodeFilter {

        override fun accept(nodeWrapper: TreeNodeWrapper) = true

    }

    object HideKeywordFilter : NodeFilter {

        override fun accept(nodeWrapper: TreeNodeWrapper) = nodeWrapper.node.userObject !is KeywordElement

    }


}