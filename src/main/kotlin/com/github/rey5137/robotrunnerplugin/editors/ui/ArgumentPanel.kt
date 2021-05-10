package com.github.rey5137.robotrunnerplugin.editors.ui

import com.github.rey5137.robotrunnerplugin.editors.ui.argument.ArgumentTable
import com.github.rey5137.robotrunnerplugin.editors.ui.argument.ArgumentModel
import com.github.rey5137.robotrunnerplugin.editors.ui.assignment.AssignmentModel
import com.github.rey5137.robotrunnerplugin.editors.ui.assignment.AssignmentTable
import com.github.rey5137.robotrunnerplugin.editors.xml.*
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTable

class ArgumentPanel : JPanel(BorderLayout()) {

    private val argumentModel = ArgumentModel()
    private val argumentTable = ArgumentTable(argumentModel).apply {
        cellSelectionEnabled = true
        autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS
    }
    private val assignmentModel = AssignmentModel()
    private val assignmentTable = AssignmentTable(assignmentModel).apply {
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

    fun populateData(element: KeywordElement) {
        argumentModel.populateModel(element)
        argumentTable.adjustColumn(ArgumentModel.INDEX_ARGUMENT)
        argumentTable.adjustColumn(ArgumentModel.INDEX_INPUT)
        assignmentModel.populateModel(element)
        assignmentTable.adjustColumn(AssignmentModel.INDEX_ASSIGNMENT)
    }

    private fun ArgumentModel.populateModel(element: KeywordElement) {
        val message = element.messages.asSequence()
            .filter { it.level == "TRACE" }
            .mapNotNull { it.value() }
            .find { it.isArgumentMessage() }
        if (message == null)
            setArguments(
                List(element.arguments.size) { ARGUMENT_EMPTY },
                element.arguments.map { listOf(InputArgument(value = it, rawInput = it)) }
            )
        else {
            try {
                val arguments = message.parseArguments()
                val inputArguments = arguments.parseArgumentInputs(element.arguments)
                setArguments(arguments, inputArguments)
            } catch (ex: Exception) {
                ex.printStackTrace()
                setArguments(emptyList(), emptyList())
            }
        }
    }

    private fun AssignmentModel.populateModel(element: KeywordElement) {
        val assigns = element.assigns

        if (assigns.isEmpty()) {
            val message = element.messages.asSequence()
                .filter { it.level == "TRACE" }
                .mapNotNull { it.value() }
                .find { it.isReturnMessage() }

            try {
                val variable = message?.parseReturn()
                if(variable == null || variable == VARIABLE_EMPTY)
                    setAssignments(emptyList())
                else
                    setAssignments(
                        listOf(
                            Assignment(
                                name = "",
                                value = variable.value,
                                dataType = variable.type,
                                assignmentType = AssignmentType.SINGLE
                            )
                        )
                    )
            } catch (ex: Exception) {
                ex.printStackTrace()
                setAssignments(emptyList())
            }
        } else {
            val message = element.messages.asSequence()
                .filter { it.level == "TRACE" }
                .mapNotNull { it.value() }
                .find { it.isReturnMessage() }

            try {
                setAssignments(assigns.parseAssignments(message?.parseReturn()))
            } catch (ex: Exception) {
                ex.printStackTrace()
                setAssignments(assigns.parseAssignments(null))
            }
        }

    }

}