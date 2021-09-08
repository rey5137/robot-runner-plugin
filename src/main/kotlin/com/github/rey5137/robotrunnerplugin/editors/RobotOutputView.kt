package com.github.rey5137.robotrunnerplugin.editors

import com.github.rey5137.robotrunnerplugin.MyBundle
import com.github.rey5137.robotrunnerplugin.editors.json.*
import com.github.rey5137.robotrunnerplugin.editors.ui.*
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HideKeywordFilter
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HidePassedSuiteFilter
import com.github.rey5137.robotrunnerplugin.editors.ui.filter.HidePassedTestFilter
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.JBPopupFactory
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
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.*
import kotlin.collections.ArrayList


class RobotOutputView(project: Project, private val srcFile: VirtualFile? = null) : JPanel(BorderLayout()) {

    private var robotTreeNodeWrapper: TreeNodeWrapper? = null
    private var jsonParser: JsonParser? = null
    private var nodeWrapperStack: MutableList<TreeNodeWrapper>? = null

    private val elementFilters = mutableListOf(
        HideKeywordFilter(false),
        HidePassedSuiteFilter(false),
        HidePassedTestFilter(false),
    )

    private var highlightInfo: HighlightInfo? = null

    private val treeModel = DefaultTreeModel(null)
    private val tree = MyTree(treeModel)
    private val detailsPanel = DetailsPanel(project)

    private lateinit var filterActionButton: AnActionButton
    private lateinit var refreshActionButton: AnActionButton
    private lateinit var searchActionButton: AnActionButton

    init {
        val splitter = JBSplitter(0.3F)
        val leftPanel = buildLeftPanel()
        val rightPanel = detailsPanel

        leftPanel.border = IdeBorderFactory.createBorder(SideBorder.RIGHT)
        splitter.firstComponent = leftPanel
        splitter.setHonorComponentsMinimumSize(true)
        rightPanel.border = JBUI.Borders.empty(10, 10, 0, 10)
        splitter.secondComponent = rightPanel

        this.add(splitter, BorderLayout.CENTER)

        if(srcFile == null) {
            jsonParser = JsonParser(RobotElement())
            robotTreeNodeWrapper = TreeNodeWrapper(
                node = DefaultMutableTreeNode(HighlightHolder(jsonParser!!.robotElement)),
                children = mutableListOf(),
            )
            nodeWrapperStack = ArrayList()
            nodeWrapperStack!!.add(robotTreeNodeWrapper!!)
            treeModel.setRoot(robotTreeNodeWrapper!!.node)
        }
        else
            refresh()
    }

    fun addEvent(method: String, payload: JSONObject) {
        jsonParser?.let { parser ->
            parser.addData(method, payload)
            when (method) {
                METHOD_START_SUITE, METHOD_START_TEST, METHOD_START_KEYWORD -> {
                    val currentNodeWrapper = nodeWrapperStack!!.last()
                    val childNodeWrapper = TreeNodeWrapper(
                        node = DefaultMutableTreeNode(HighlightHolder(parser.currentElement)),
                        children = mutableListOf()
                    )
                    currentNodeWrapper.children.add(childNodeWrapper)
                    nodeWrapperStack!!.add(childNodeWrapper)
                    UIUtil.invokeAndWaitIfNeeded(Runnable {
                        treeModel.insertNodeInto(
                            childNodeWrapper.node,
                            currentNodeWrapper.node,
                            currentNodeWrapper.children.size - 1
                        )
                        if (currentNodeWrapper == robotTreeNodeWrapper)
                            treeModel.reload(currentNodeWrapper.node)
                        if (method == METHOD_START_SUITE || method == METHOD_START_TEST)
                            tree.expandPath(TreePath(currentNodeWrapper.node.path))
                    })
                }
                METHOD_END_SUITE, METHOD_END_TEST, METHOD_END_KEYWORD -> {
                    val nodeWrapper = nodeWrapperStack!!.removeAt(nodeWrapperStack!!.size - 1)
                    UIUtil.invokeAndWaitIfNeeded(Runnable {
                        treeModel.nodeChanged(nodeWrapper.node)
                        val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
                        if (nodeWrapper.node == selectedNode)
                            detailsPanel.showDetails(selectedNode.getElement(), highlightInfo)
                    })
                }
                METHOD_CLOSE -> {
                    UIUtil.invokeAndWaitIfNeeded(Runnable {
                        filterActionButton.isEnabled = true
                        refreshActionButton.isEnabled = true
                        searchActionButton.isEnabled = true
                    })
                }
            }
        }

    }

    fun dispose() {
        cleanUp()
    }

    private fun buildLeftPanel(): JPanel {
        tree.setUI(MyTreeUI())
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

        filterActionButton = object : DumbAwareActionButton(
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
        }
        refreshActionButton = object : AnActionButton(
            MyBundle.message("robot.output.editor.label.refresh"),
            AllIcons.Actions.Refresh
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                refresh()
            }
        }
        searchActionButton = object : DumbAwareActionButton(
            MyBundle.message("robot.output.editor.label.deep-search"),
            AllIcons.Actions.Search
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                JBPopupFactory.getInstance().createActionGroupPopup(null, DefaultActionGroup().apply {
                    addAll(
                        object : AnAction(MyBundle.message("robot.output.editor.label.deep-search")) {
                            override fun actionPerformed(e: AnActionEvent) {
                                val info = showSearchInput()
                                if (info != null) {
                                    highlightInfo = info
                                    refreshNodes(robotTreeNodeWrapper!!.node.getElement())
                                    detailsPanel.updateHighlightInfo(highlightInfo)
                                }
                            }
                        },
                        object : AnAction(MyBundle.message("robot.output.editor.label.clear-deep-search")) {
                            override fun actionPerformed(e: AnActionEvent) {
                                highlightInfo = null
                                refreshNodes(robotTreeNodeWrapper!!.node.getElement())
                                detailsPanel.updateHighlightInfo(highlightInfo)
                            }
                        }
                    )
                }, e.dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false)
                    .show(preferredPopupPoint!!)
            }
        }
        filterActionButton.isEnabled = srcFile != null
        refreshActionButton.isEnabled = srcFile != null
        searchActionButton.isEnabled = srcFile != null
        val toolbarDecorator = ToolbarDecorator.createDecorator(tree)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())
            .setScrollPaneBorder(JBUI.Borders.empty())
            .setMinimumSize(JBDimension(200, 200))
            .setForcedDnD()
            .addExtraAction(filterActionButton)
            .addExtraAction(refreshActionButton)
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
                    tree.setUI(null)
                    TreeUtil.expandAll(tree) {
                        tree.setUI(MyTreeUI())
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
                        if (elementHolder.value is TestElement)
                            tests.add(elementHolder.value.name)
                    }
                    val stringSelection = StringSelection(tests.joinToString(separator = "\n"))
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
                }
            })
            .addExtraAction(searchActionButton)
        val panel = JPanel(BorderLayout())
        panel.background = JBColor.background()
        panel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER)

        return panel
    }

    private fun refresh() {
        if(srcFile == null)
            refreshNodes(jsonParser!!.robotElement)
        else {
            cleanUp()
            refreshNodes(srcFile.parseXml())
        }
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

        val selectedPaths = tree.selectionPaths ?: emptyArray()

        robotTreeNodeWrapper?.rebuildNode()
        treeModel.reload()

        if (keepExpanded) {
            tree.setUI(null)
            TreeUtil.restoreExpandedPaths(tree, expandedPaths)
            tree.setUI(MyTreeUI())
            restoreSelectionNode(selectedPaths)
        } else {
            TreeUtil.promiseExpand(tree) { path ->
                val element = (path.lastPathComponent as DefaultMutableTreeNode).getElement<Element>()
                if (element is KeywordElement || element is TestElement)
                    TreeVisitor.Action.SKIP_CHILDREN
                else
                    TreeVisitor.Action.CONTINUE
            }
            if (selectedPaths.isNotEmpty())
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
        return TreeNodeWrapper(
            node = oldNodeWrapper.copyNode(HighlightHolder(this, HighlightType.UNMATCHED)),
            children = children
        )
    }

    private fun SuiteElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = if (highlightInfo.match(this)) HighlightType.MATCHED else HighlightType.UNMATCHED
        val children = mutableListOf<TreeNodeWrapper>()
        this.children.forEachIndexed { index, element ->
            val nodeWrapper = when (element) {
                is SuiteElement -> element.toNode(oldNodeWrapper.childAt(index))
                is TestElement -> element.toNode(oldNodeWrapper.childAt(index))
                is KeywordElement -> element.toNode(oldNodeWrapper.childAt(index))
                else -> null
            }
            if (nodeWrapper != null) {
                if (highlight == HighlightType.UNMATCHED && nodeWrapper.node.getElementHolder<Element>().highlight != HighlightType.UNMATCHED)
                    highlight = HighlightType.CONTAINED
                children.add(nodeWrapper)
            }
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(HighlightHolder(this, highlight)), children = children)
    }

    private fun TestElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = if (highlightInfo.match(this)) HighlightType.MATCHED else HighlightType.UNMATCHED
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEachIndexed { index, keyword ->
            val nodeWrapper = keyword.toNode(oldNodeWrapper.childAt(index))
            if (highlight == HighlightType.UNMATCHED && nodeWrapper.node.getElementHolder<Element>().highlight != HighlightType.UNMATCHED)
                highlight = HighlightType.CONTAINED
            children.add(nodeWrapper)
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(HighlightHolder(this, highlight)), children = children)
    }

    private fun KeywordElement.toNode(oldNodeWrapper: TreeNodeWrapper? = null): TreeNodeWrapper {
        var highlight = if (highlightInfo.match(this)) HighlightType.MATCHED else HighlightType.UNMATCHED
        val children = mutableListOf<TreeNodeWrapper>()
        keywords.forEachIndexed { index, keyword ->
            val nodeWrapper = keyword.toNode(oldNodeWrapper.childAt(index))
            if (highlight == HighlightType.UNMATCHED && nodeWrapper.node.getElementHolder<Element>().highlight != HighlightType.UNMATCHED)
                highlight = HighlightType.CONTAINED
            children.add(nodeWrapper)
        }
        return TreeNodeWrapper(node = oldNodeWrapper.copyNode(HighlightHolder(this, highlight)), children = children)
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


    private fun <T : Element> DefaultMutableTreeNode.getElementHolder() = userObject as HighlightHolder<T>

    private fun <T : Element> DefaultMutableTreeNode.getElement() = (userObject as HighlightHolder<T>).value

    private fun showSearchInput(): HighlightInfo? {
        val builder = DialogBuilder()
        lateinit var textField: JBTextField
        lateinit var caseCheckbox: JBCheckBox
        lateinit var regexCheckbox: JBCheckBox
        val panel = panel {
            row {
                textField = textField({ "" }, {}, 30).component
            }
            row {
                caseCheckbox = checkBox(MyBundle.message("robot.output.editor.label.case-sensitive")).component
                regexCheckbox = checkBox(MyBundle.message("robot.output.editor.label.is-regex")).component
            }
        }
        textField.text = highlightInfo?.value ?: ""
        caseCheckbox.isSelected = highlightInfo?.ignoreCase?.not() ?: false
        regexCheckbox.isSelected = highlightInfo?.isRegex ?: false
        builder.setTitle("Search Tree")
        builder.setCenterPanel(panel)
        builder.removeAllActions()
        builder.addOkAction()
        builder.addCancelAction()
        return if (builder.show() == DialogWrapper.OK_EXIT_CODE && textField.text.isNotEmpty())
            HighlightInfo(
                value = textField.text,
                ignoreCase = !caseCheckbox.isSelected,
                isRegex = regexCheckbox.isSelected
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
            val elementHolder = value.userObject as HighlightHolder<Element>
            setHighlightBorder(elementHolder.highlight)

            when (val element = elementHolder.value) {
                is SuiteElement -> {
                    icon = when {
                        element.status.isPassed -> MyIcons.SuitePass
                        element.status.isRunning -> MyIcons.SuiteRunning
                        else -> MyIcons.SuiteFail
                    }
                    append(element.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is TestElement -> {
                    icon = when {
                        element.status.isPassed -> MyIcons.TestPass
                        element.status.isRunning -> MyIcons.TestRunning
                        else -> MyIcons.TestFail
                    }
                    append(element.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                }
                is KeywordElement -> {
                    icon = when (element.type) {
                        KEYWORD_TYPE_SETUP -> when {
                            element.status.isPassed -> MyIcons.SetupPass
                            element.status.isRunning -> MyIcons.SetupRunning
                            else -> MyIcons.SetupFail
                        }
                        KEYWORD_TYPE_TEARDOWN -> when {
                            element.status.isPassed -> MyIcons.TeardownPass
                            element.status.isRunning -> MyIcons.TeardownRunning
                            else -> MyIcons.TeardownFail
                        }
                        KEYWORD_TYPE_FOR -> when {
                            element.status.isPassed -> MyIcons.ForPass
                            element.status.isRunning -> MyIcons.ForRunning
                            else -> MyIcons.ForFail
                        }
                        KEYWORD_TYPE_FORITEM -> when {
                            element.status.isPassed -> MyIcons.ForitemPass
                            element.status.isRunning -> MyIcons.ForitemRunning
                            else -> MyIcons.ForitemFail
                        }
                        else -> when {
                            element.status.isPassed -> MyIcons.KeywordPass
                            element.status.isRunning -> MyIcons.KeywordRunning
                            else -> MyIcons.KeywordFail
                        }
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