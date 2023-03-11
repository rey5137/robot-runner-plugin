package com.github.rey5137.robotrunnerplugin.provider

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.util.containers.toArray


private const val KEYWORD_DEFINITION_CLASS = "KeywordDefinitionImpl"
private const val KEYWORD_DEFINITION_ID_CLASS = "KeywordDefinitionIdImpl"
private const val KEYWORD_STATEMENT_CLASS = "KeywordStatementImpl"
private const val KEYWORD_INVOKABLE_CLASS = "KeywordInvokableImpl"
private const val ARGUMENT_CLASS = "ArgumentImpl"
private const val BRACKET_SETTING_CLASS = "BracketSettingImpl"

private const val TYPE_FILE = "FILE"
private const val TYPE_KEYWORD_DEFINITION = "KEYWORD_DEFINITION"

class IntellibotStepFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val actualRoot = findActualRoot(root)
        val stepRoots = mutableListOf<StepRoot>()

        actualRoot.visitElement { element ->
            if (element.getSimpleClass() == KEYWORD_DEFINITION_CLASS) {
                element.toStepRoot()?.let { stepRoots.add(it) }
                true
            } else
                false
        }

//        printStepTree(stepRoots)

        val descriptors: MutableList<FoldingDescriptor> = mutableListOf()
        stepRoots.forEach { root ->
            root.children.forEach { step -> buildDescriptor(step, descriptors) }
        }
        return descriptors.toArray(emptyArray())
    }

    private fun buildDescriptor(step: Step, descriptors: MutableList<FoldingDescriptor>) {
        descriptors.add(
            FoldingDescriptor(
                step.element.node,
                TextRange(step.textStart, step.textEnd),
                null,
                "Step ${step.num} - ${step.title}"
            )
        )
        step.children.forEach { buildDescriptor(it, descriptors) }
    }

    private fun findActualRoot(root: PsiElement): PsiElement {
        var result = root
        val types = listOf(TYPE_KEYWORD_DEFINITION, TYPE_FILE)
        while (!types.contains(result.getType())) {
            result = result.parent
        }
        return result
    }

    private fun PsiElement.toStepRoot(): StepRoot? {
        var stepRoot: StepRoot? = null
        this.visitElement { element ->
            if (element.getSimpleClass() == KEYWORD_STATEMENT_CLASS) {
                if (element.isStepStatement()) {
                    if (stepRoot == null) {
                        stepRoot = StepRoot(element = this)
                    }
                    val stack = stepRoot!!.stack
                    val newStep = element.toStep()
                    while (stack.isNotEmpty() && stack.last().level >= newStep.level) {
                        val removedStep = stack.removeAt(stack.size - 1)
                        removedStep.textEnd = element.findPreviousKeywordElement()?.textRange?.endOffset ?: removedStep.textEnd
                    }
                    if (stack.isNotEmpty())
                        stack.last().children.add(newStep)
                    else
                        stepRoot?.children?.add(newStep)
                    stack.add(newStep)
                }
                true
            } else
                false
        }

        var element = this.lastChild
        var endOffset = this.textRange.endOffset
        if(element != null) {
            if(element.getSimpleClass() == BRACKET_SETTING_CLASS) {
                element = element.findPreviousNonWhiteSpaceElement()
            }
            val previousElement = element.findPreviousNonWhiteSpaceElement()
            if(previousElement != null && previousElement.getSimpleClass() == BRACKET_SETTING_CLASS)
                element = previousElement.findPreviousKeywordElement()
            if(element != null)
                endOffset = element.textRange.endOffset
        }

        stepRoot?.stack?.asReversed()?.forEach { step ->
            step.textEnd = endOffset
        }
        return stepRoot
    }

    private fun PsiElement.findPreviousKeywordElement(): PsiElement? {
        var element: PsiElement? = this
        do {
            element = element?.findPreviousNonWhiteSpaceElement()
            val previousElement = element?.findPreviousNonWhiteSpaceElement()
            if(previousElement != null && previousElement.getSimpleClass() == BRACKET_SETTING_CLASS) {
                element = previousElement
            }
            else
                break
        } while(element != null)
        return element
    }

    private fun PsiElement.findPreviousNonWhiteSpaceElement(): PsiElement? {
        var element: PsiElement? = this
        do {
            element = element?.prevSibling
        }
        while(element is PsiWhiteSpace)
        return element
    }

    private fun PsiElement.toStep(): Step {
        val arguments = this.getArguments()
        val num = arguments.findStepNum()
        return Step(
            num = num,
            title = arguments.findStepTitle(),
            level = num.split(".").count { it.isNotEmpty() },
            element = this,
            textStart = this.textRange.startOffset,
            textEnd = this.textRange.endOffset
        )
    }

    private fun PsiElement.isStepStatement(): Boolean {
        val child = this.children.firstOrNull { e ->
            e.getSimpleClass() == KEYWORD_INVOKABLE_CLASS
                    && (e.text.equals("Step", ignoreCase = true)
                    || e.text.equals("RobotStepLibrary.Step", ignoreCase = true))
        }
        return child != null
    }

    private fun PsiElement.getKeywordId(): String {
        val child = this.children.firstOrNull { e -> e.getSimpleClass() == KEYWORD_DEFINITION_ID_CLASS }
        return child?.text ?: ""
    }

    private fun PsiElement.getArguments(): List<String> {
        return this.children.filter { e -> e.getSimpleClass() == ARGUMENT_CLASS }
            .map { e -> e.text }
    }

    private fun PsiElement.visitElement(skipChildren: (PsiElement) -> Boolean) {
        if (!skipChildren(this))
            this.children.forEach { it.visitElement(skipChildren) }
    }

    private fun PsiElement.getSimpleClass(): String {
        return javaClass.name.split(".").last()
    }

    private fun PsiElement.getType(): String {
        return this.elementType?.toString() ?: ""
    }

    private fun List<String>.findStepNum(): String {
        val argument = this.find {
            it.startsWith("num=", ignoreCase = true)
        }
        return argument?.substring("num=".length) ?: if (this.isNotEmpty()) this[0] else ""
    }

    private fun List<String>.findStepTitle(): String {
        val argument = this.find {
            it.startsWith("title=", ignoreCase = true)
        }
        return argument?.substring("title=".length) ?: if (this.size >= 2) this[1] else ""
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

    private fun printStepTree(roots: List<StepRoot>) {
        val sb = StringBuilder()
        roots.forEach {
            sb.append("Root: ").append(it.element.getKeywordId()).append("\n")
            it.children.forEach { step -> printStep(sb, step, 1) }
        }
        println(sb.toString())
    }

    private fun printStep(builder: StringBuilder, step: Step, level: Int) {
        repeat(level) { builder.append(" ") }
        builder.append("Step ${step.num} - ${step.title}: ${step.textStart} - ${step.textEnd}\n")
        step.children.forEach { printStep(builder, it, level + 1) }
    }

    data class Step(
        val num: String,
        val title: String = "",
        val level: Int,
        val element: PsiElement,
        val children: MutableList<Step> = mutableListOf(),
        var textStart: Int = 0,
        var textEnd: Int = 0,
    )

    data class StepRoot(
        val element: PsiElement,
        val children: MutableList<Step> = mutableListOf(),
        val stack: MutableList<Step> = mutableListOf(),
    )
}
