package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.ArgumentTable
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.ArgumentModel
import com.github.rey5137.robotrunnerplugin.editors.ui.assignment.AssignmentModel
import com.github.rey5137.robotrunnerplugin.editors.ui.assignment.AssignmentTable
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTable
import kotlin.math.max
import kotlin.math.min

class ArgumentPanel(project: Project) : JPanel(BorderLayout()) {

    private val argumentModel = ArgumentModel()
    private val argumentTable = ArgumentTable(project, argumentModel).apply {
        cellSelectionEnabled = true
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
    }
    private val assignmentModel = AssignmentModel()
    private val assignmentTable = AssignmentTable(project, assignmentModel).apply {
        cellSelectionEnabled = true
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
    }
    private val argumentSplitter = JBSplitter(true, 0.7F)

    init {
        argumentSplitter.firstComponent = ToolbarDecorator.createDecorator(argumentTable)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .createPanel()

        argumentSplitter.secondComponent = ToolbarDecorator.createDecorator(assignmentTable)
            .disableUpAction()
            .disableDownAction()
            .disableRemoveAction()
            .createPanel()

        add(argumentSplitter, BorderLayout.CENTER)
    }

    fun populateData(element: KeywordElement, highlightInfo: HighlightInfo?) {
        argumentModel.populateModel(element, highlightInfo)
        argumentTable.adjustColumn(ArgumentModel.INDEX_ARGUMENT)
        argumentTable.adjustColumn(ArgumentModel.INDEX_INPUT)
        assignmentModel.populateModel(element, highlightInfo)
        assignmentTable.adjustColumn(AssignmentModel.INDEX_ASSIGNMENT)
        val argumentLine = argumentModel.countLine()
        val assigmentLine = assignmentModel.countLine()
        argumentSplitter.proportion = max(
            0.2F,
            min(0.9F,argumentLine * 1F / max(1, (argumentLine + assigmentLine)))
        )
    }

    private fun ArgumentModel.countLine(): Int =
        (0 until rowCount).fold(0) { acc, index -> acc + (getVariableModel(index)?.rowCount ?: 1) }

    private fun AssignmentModel.countLine(): Int =
        (0 until rowCount).fold(0) { acc, index -> acc + (getVariableModel(index)?.rowCount ?: 1) }

    private fun ArgumentModel.populateModel(element: KeywordElement, highlightInfo: HighlightInfo?) {
        val message = element.messages.asSequence()
            .filter { it.level == "TRACE" }
            .mapNotNull { it.value() }
            .find { it.isArgumentMessage() }
        if (message == null)
            setArguments(
                List(element.arguments.size) { ARGUMENT_EMPTY },
                element.arguments.map { listOf(InputArgument(value = it, rawInput = it)) },
                highlightInfo
            )
        else {
            try {
                val arguments = message.parseArguments()
                val inputArguments = arguments.parseArgumentInputs(element.arguments)
                setArguments(arguments, inputArguments, highlightInfo)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setArguments(emptyList(), emptyList(), highlightInfo)
            }
        }
    }

    private fun AssignmentModel.populateModel(element: KeywordElement, highlightInfo: HighlightInfo?) {
        val assigns = element.assigns

        if (assigns.isEmpty()) {
            val message = element.messages.asSequence()
                .filter { it.level == "TRACE" }
                .mapNotNull { it.value() }
                .find { it.isReturnMessage() }

            try {
                val variable = message?.parseReturn()
                if (variable == null || variable == VARIABLE_EMPTY)
                    setAssignments(emptyList(), highlightInfo)
                else
                    setAssignments(
                        listOf(
                            Assignment(
                                name = "",
                                value = variable.value,
                                dataType = variable.type,
                                assignmentType = AssignmentType.SINGLE
                            )
                        ),
                        highlightInfo
                    )
            } catch (ex: Exception) {
                ex.printStackTrace()
                setAssignments(emptyList(), highlightInfo)
            }
        } else {
            val message = element.messages.asSequence()
                .filter { it.level == "TRACE" }
                .mapNotNull { it.value() }
                .find { it.isReturnMessage() }

            try {
                setAssignments(assigns.parseAssignments(message?.parseReturn()), highlightInfo)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setAssignments(assigns.parseAssignments(null), highlightInfo)
            }
        }

    }

}