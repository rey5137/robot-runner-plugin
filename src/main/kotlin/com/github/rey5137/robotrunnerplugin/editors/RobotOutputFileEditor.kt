package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.ui.*
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HideKeywordFilter
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HidePassedSuiteFilter
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HidePassedTestFilter
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.ui.tree.TreeVisitor
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import icons.MyIcons
import org.eclipse.collections.impl.list.mutable.FastList
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.beans.PropertyChangeListener
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.*


class RobotOutputFileEditor(private val project: Project, private val srcFile: VirtualFile) : UserDataHolderBase(),
    FileEditor {

    private var robotTreeNodeWrapper: TreeNodeWrapper? = null

    private val elementFilters = mutableListOf(
        HideKeywordFilter(false),
        HidePassedSuiteFilter(false),
        HidePassedTestFilter(false),
    )

    private var highlightInfo: HighlightInfo? = null

    private val treeModel = DefaultTreeModel(null)
    private val tree = MyTree(treeModel)
    private val detailsPanel = DetailsPanel(project)
    private val myComponent = buildComponent()


    override fun dispose() {
        cleanUp()
    }

    override fun getComponent(): JComponent = myComponent

    override fun getPreferredFocusedComponent(): JComponent = tree

    override fun getName(): String = MyBundle.message("robot.output.editor.label.robot-result")

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? = null

    private fun buildComponent(): JComponent {
        val rootPanel = JPanel(BorderLayout())
        val splitter = JBSplitter(0.3F)
        val leftPanel = buildLeftPanel()
        val rightPanel = detailsPanel

        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
        splitter.firstComponent = leftPanel
        splitter.setHonorComponentsMinimumSize(true)
        rightPanel.border = JBUI.Borders.empty(10, 10, 0, 10)
        splitter.secondComponent = rightPanel

        rootPanel.add(splitter, BorderLayout.CENTER)

        refreshFile()

        return rootPanel
    }

    private fun buildLeftPanel(): JPanel {
        tree.ui = MyTreeUI()
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.cellRenderer = MyTreeCellRenderer()

        TreeUtil.installActions(tree)
        TreeSpeedSearch(tree) { o ->
            val node = o.lastPathComponent as DefaultMutableTreeNode
            when (val element = node.getElement<Element>()) {
                is SuiteElement -> element.name
                is TestElement -> element.name
                is KeywordElement -> element.name
                else -> o.toString()
            }
        }

        val component = tree.cellRenderer.getTreeCellRendererComponent(tree, "", false, false, false, 0, false)
        tree.rowHeight = component.preferredSize.height + 2

        tree.addTreeSelectionListener {
            val selectionPath = tree.selectionPath
            if (selectionPath != null) {
                val node = selectionPath.lastPathComponent as DefaultMutableTreeNode
                detailsPanel.showDetails(node.getElement(), highlightInfo)
            }
        }

        val toolbarDecorator = ToolbarDecorator.createDecorator(tree)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .setMinimumSize(JBDimension(200, 200))
            .setForcedDnD()
            .addExtraAction(object : DumbAwareActionButton(
                MyBundle.message("robot.output.editor.label.filter-element"),
                AllIcons.General.Filter
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    JBPopupFactory.getInstance().createActionGroupPopup(null, DefaultActionGroup().apply {
                        elementFilters.map { filter ->
                            add(object : ToggleAction(filter.title) {
                                override fun isSelected(e: AnActionEvent): Boolean = filter.isEnabled

                                override fun setSelected(e: AnActionEvent, state: Boolean) {
                                    filter.isEnabled = state
                                    populateTree(true)
                                }
                            })
                        }
                    }, e.dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
                        .show(preferredPopupPoint!!)
                }
            })
            .addExtraAction(object :
                AnActionButton(MyBundle.message("robot.output.editor.label.refresh"), AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    refreshFile()
                }
            })
            .addExtraAction(object : AnActionButton(
                MyBundle.message("robot.output.editor.label.collapse-all"),
                AllIcons.Actions.Collapseall
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    TreeUtil.collapseAll(tree, 0)
                }
            })
            .addExtraAction(object :
                AnActionButton(MyBundle.message("robot.output.editor.label.expand-all"), AllIcons.Actions.Expandall) {
                override fun actionPerformed(e: AnActionEvent) {
                    tree.ui = null
                    TreeUtil.expandAll(tree) {
                        tree.ui = MyTreeUI()
                    }
                }
            })
            .addExtraAction(object : AnActionButton(
                MyBundle.message("robot.output.editor.label.copy-test-case-name"),
                AllIcons.Actions.Copy
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val tests = mutableListOf<String>()
                    TreeUtil.treeNodeTraverser(robotTreeNodeWrapper!!.node).forEach { node ->
                        val elementHolder = (node as DefaultMutableTreeNode).getElementHolder<Element>()
                        if (elementHolder.element is TestElement)
                            tests.add(elementHolder.element.name)
                    }
                    val stringSelection = StringSelection(tests.joinToString(separator = "\n"))
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
                }
            })
            .addExtraAction(object : AnActionButton(
                MyBundle.message("robot.output.editor.label.deep-search"),
                AllIcons.Actions.Search
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val info = showSearchInput()
                    if(info != null) {
                        highlightInfo = info
                        refreshNodes(robotTreeNodeWrapper!!.node.getElement())
                    }
                }
            })
            .addExtraAction(object : AnActionButton(
                MyBundle.message("robot.output.editor.label.clear-deep-search"),
                AllIcons.Actions.Restart
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    highlightInfo = null
                    refreshNodes(robotTreeNodeWrapper!!.node.getElement())
                }
            })
        val panel = JPanel(BorderLayout())
        panel.background = JBColor.background()
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER)

        return panel
    }

    private fun refreshFile() {
        cleanUp()
        refreshNodes(srcFile.parseXml())
    }

    private fun refreshNodes(robotElement: RobotElement) {
        val keepExpanded = robotTreeNodeWrapper != null
        val rootNodeWrapper = robotElement.toNode(robotTreeNodeWrapper)

        robotTreeNodeWrapper = rootNodeWrapper
        if (treeModel.root == null)
            treeModel.setRoot(rootNodeWrapper.node)

        populateTree(keepExpanded)
    }

    private fun cleanUp() {
        robotTreeNodeWrapper?.let {
            val robotElement = it.node.getElement<RobotElement>()
            robotElement.db.close()
        }
    }

    private fun populateTree(keepExpanded: Boolean) {
        var expandedPaths = emptyList<TreePath>()
        if (keepExpanded)
            expandedPaths = TreeUtil.collectExpandedPaths(tree)

        val selectedPaths = tree.selectionPaths

        robotTreeNodeWrapper?.rebuildNode()
        treeModel.reload()

        if (keepExpanded) {
            tree.ui = null
            TreeUtil.restoreExpandedPaths(tree, expandedPaths)
            tree.ui = MyTreeUI()
            restoreSelectionNode(selectedPaths)
        } else {
            TreeUtil.promiseExpand(tree) { path ->
                val element = (path.lastPathComponent as DefaultMutableTreeNode).getElement<Element>()
                if (element is KeywordElement || element is TestElement)
                    TreeVisitor.Action.SKIP_CHILDREN
                else
                    TreeVisitor.Action.CONTINUE
            }
            UIUtil.invokeLaterIfNeeded {
                restoreSelectionNode(selectedPaths)
            }
        }
    }

    private fun restoreSelectionNode(paths: Array<TreePath>?) {
        tree.selectionPaths = paths
        if (tree.selectionPaths == null) {
            val nodes = treeModel.getPathToRoot(robotTreeNodeWrapper.childAt(0)?.node)
            tree.selectionPath = TreePath(nodes)
        }
    }

    private fun TreeNodeWrapper.rebuildNode(): DefaultMutableTreeNode {
        node.removeAllChildren()
        val filters = elementFilters.filter { it.isEnabled }
        children.forEach { nodeWrapper ->
            if (filters.firstOrNull { !it.accept(nodeWrapper.node.getElement()) } == null)
                node.add(nodeWrapper.rebuildNode())
        }
        return node
    }

    private fun RobotElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        val children = mutableListOf<TreeNodeWrapper>()
        suites.forEachIndexed { index, suite -> children.add(suite.toNode(oldNodeWrapper.childAt(index))) }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(ElementHolder(this, false)), children = children)
    }

    private fun SuiteElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = shouldHighlight(highlightInfo)
        val children = mutableListOf<TreeNodeWrapper>()
        this.children.forEachIndexed { index, element ->
            val nodeWrapper = when (element) {
                is SuiteElement -> element.toNode(oldNodeWrapper.childAt(index))
                is TestElement -> element.toNode(oldNodeWrapper.childAt(index))
                is KeywordElement -> element.toNode(oldNodeWrapper.childAt(index))
                else -> null
            }
            if (nodeWrapper != null) {
                highlight = highlight || nodeWrapper.node.getElementHolder<Element>().highlight
                children.add(nodeWrapper)
            }
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(ElementHolder(this, highlight)), children = children)
    }

    private fun TestElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = shouldHighlight(highlightInfo)
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEachIndexed { index, keyword ->
            val nodeWrapper = keyword.toNode(oldNodeWrapper.childAt(index))
            highlight = highlight || nodeWrapper.node.getElementHolder<Element>().highlight
            children.add(nodeWrapper)
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(ElementHolder(this, highlight)), children = children)
    }

    private fun KeywordElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = shouldHighlight(highlightInfo)
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEachIndexed { index, keyword ->
            val nodeWrapper = keyword.toNode(oldNodeWrapper.childAt(index))
            highlight = highlight || nodeWrapper.node.getElementHolder<Element>().highlight
            children.add(nodeWrapper)
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(ElementHolder(this, highlight)), children = children)
    }

    private fun TreeNodeWrapper?.copyNode(obj: Any) =
        this?.node?.apply { userObject = obj } ?: DefaultMutableTreeNode(obj)

    private fun TreeNodeWrapper?.childAt(index: Int): TreeNodeWrapper? {
        if (this == null)
            return null
        if (index >= children.size)
            return null
        return children[index]
    }


    private fun <T: Element> DefaultMutableTreeNode.getElementHolder() = userObject as ElementHolder<T>

    private fun <T: Element> DefaultMutableTreeNode.getElement() = (userObject as ElementHolder<T>).element

    private fun showSearchInput(): HighlightInfo? {
        val builder = DialogBuilder()
        lateinit var textField: JBTextField
        lateinit var caseCheckbox: JBCheckBox
        val panel = panel {
            row {
                textField = textField({""}, {}, 30).component
            }
            row {
                caseCheckbox = checkBox(MyBundle.message("robot.output.editor.label.case-sensitive")).component
            }
        }
        textField.text = highlightInfo?.value ?: ""
        caseCheckbox.isSelected = highlightInfo?.ignoreCase?.not() ?: false
        builder.setTitle("Search Tree")
        builder.setCenterPanel(panel)
        builder.removeAllActions()
        builder.addOkAction()
        builder.addCancelAction()
        return if(builder.show() == DialogWrapper.OK_EXIT_CODE && textField.text.isNotEmpty())
            HighlightInfo(
                value = textField.text,
                ignoreCase = !caseCheckbox.isSelected
            )
        else
            null
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
            setFocusBorderAroundIcon(true)
            val elementHolder = value.userObject as ElementHolder<Element>
            border = if (elementHolder.highlight)
                BorderFactory.createLineBorder(Color.RED)
            else
                BorderFactory.createEmptyBorder()

            when (val element = elementHolder.element) {
                is SuiteElement -> {
                    icon = if (element.status.isPassed) MyIcons.SuitePass else MyIcons.SuiteFail
                    append(element.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is TestElement -> {
                    icon = if (element.status.isPassed) MyIcons.TestPass else MyIcons.TestFail
                    append(element.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is KeywordElement -> {
                    icon = when (element.type) {
                        KEYWORD_TYPE_SETUP -> if (element.status.isPassed) MyIcons.SetupPass else MyIcons.SetupFail
                        KEYWORD_TYPE_TEARDOWN -> if (element.status.isPassed) MyIcons.TeardownPass else MyIcons.TeardownFail
                        KEYWORD_TYPE_FOR -> if (element.status.isPassed) MyIcons.ForPass else MyIcons.ForFail
                        KEYWORD_TYPE_FORITEM -> if (element.status.isPassed) MyIcons.ForitemPass else MyIcons.ForitemFail
                        else -> if (element.status.isPassed) MyIcons.KeywordPass else MyIcons.KeywordFail
                    }
                    if (element.assigns.isNotEmpty()) {
                        append(
                            element.assigns.joinToString(separator = ", "),
                            SimpleTextAttributes.REGULAR_ATTRIBUTES
                        )
                        append(" = ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    }
                    if (element.library.isNotBlank())
                        append("${element.library}.", SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES)
                    append(element.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    if (element.arguments.isNotEmpty()) {
                        append(" ")
                        append(
                            element.arguments.joinToString(separator = ", "),
                            SimpleTextAttributes.GRAY_ATTRIBUTES
                        )
                    }
                }
            }
        }

    }

    internal class MyTree(treeModel: TreeModel) : Tree(treeModel) {

        override fun getPreferredSize(): Dimension {
            if (rowCount == 0)
                return minimumSize
            return Dimension(1000, rowCount * rowHeight + 2)
        }

        override fun getExpandedDescendants(parent: TreePath): Enumeration<TreePath>? {
            return if (!isExpanded(parent))
                null
            else Collections.enumeration(getOpenedChild(parent, FastList()))
        }

        private fun getOpenedChild(paramTreeNode: TreePath, list: MutableList<TreePath>): List<TreePath> {
            val parent = paramTreeNode.lastPathComponent
            val model = model
            val nbChild = model.getChildCount(parent)
            for (i in 0 until nbChild) {
                val child = model.getChild(parent, i)
                val childPath = paramTreeNode.pathByAddingChild(child)
                if (!model.isLeaf(child) && isExpanded(childPath)) {
                    list.add(childPath)
                    getOpenedChild(childPath, list)
                }
            }
            return list
        }
    }

    internal class MyTreeUI : BasicTreeUI() {

        override fun createLayoutCache(): AbstractLayoutCache {
            return FixedHeightLayoutCache()
        }

    }

}