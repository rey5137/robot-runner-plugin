package com.github.rey5137.robotrunnerplugin.editors.xml

import org.junit.Test
import kotlin.test.assertEquals

class AssignmentParserTest {

    @Test
    fun parseAssignments__withSingleAssignment() {
        val names = listOf(
            "\${a1}",
        )
        val variable = Variable(
            name = "",
            value = 2L,
            type = DataType.INTEGER
        )
        val result = names.parseAssignments(variable)

        assertEquals(
            listOf(
                Assignment(
                    name = "a1",
                    value = 2L,
                    dataType = DataType.INTEGER,
                    assignmentType = AssignmentType.SINGLE
                )
            ),
            result
        )
    }

    @Test
    fun parseAssignments__withMultiAssignments() {
        val names = listOf(
            "\${a1}",
            "\${a2}",
            "\${a3}",
            "\${a4}"
        )
        val variable = Variable(
            name = "",
            value = listOf(
                Variable(
                    name = "",
                    value = "abc",
                    type = DataType.STRING,
                ),
                Variable(
                    name = "",
                    value = null,
                    type = DataType.NONE,
                ),
                Variable(
                    name = "",
                    value = false,
                    type = DataType.BOOL,
                ),
                Variable(
                    name = "",
                    value = 2.3,
                    type = DataType.NUMBER,
                ),
            ),
            type = DataType.ARRAY
        )
        val result = names.parseAssignments(variable)

        assertEquals(
            listOf(
                Assignment(
                    name = "a1",
                    value = "abc",
                    dataType = DataType.STRING,
                    assignmentType = AssignmentType.SINGLE
                ),
                Assignment(
                    name = "a2",
                    value = null,
                    dataType = DataType.NONE,
                    assignmentType = AssignmentType.SINGLE
                ),
                Assignment(
                    name = "a3",
                    value = false,
                    dataType = DataType.BOOL,
                    assignmentType = AssignmentType.SINGLE
                ),
                Assignment(
                    name = "a4",
                    value = 2.3,
                    dataType = DataType.NUMBER,
                    assignmentType = AssignmentType.SINGLE
                ),
            ),
            result
        )
    }

    @Test
    fun parseAssignments__withArrayAssignment() {
        val names = listOf(
            "\${a1}",
            "@{a2}",
            "\${a3}",
            "\${a4}",
        )
        val variable = Variable(
            name = "",
            value = listOf(
                Variable(
                    name = "",
                    value = "abc",
                    type = DataType.STRING,
                ),
                Variable(
                    name = "",
                    value = null,
                    type = DataType.NONE,
                ),
                Variable(
                    name = "",
                    value = false,
                    type = DataType.BOOL,
                ),
                Variable(
                    name = "",
                    value = 2.3,
                    type = DataType.NUMBER,
                ),
                Variable(
                    name = "",
                    value = 4L,
                    type = DataType.INTEGER,
                ),
            ),
            type = DataType.ARRAY
        )
        val result = names.parseAssignments(variable)

        assertEquals(
            listOf(
                Assignment(
                    name = "a1",
                    value = "abc",
                    dataType = DataType.STRING,
                    assignmentType = AssignmentType.SINGLE
                ),
                Assignment(
                    name = "a2",
                    value = listOf(
                        Variable(
                            name = "",
                            value = null,
                            type = DataType.NONE,
                        ),
                        Variable(
                            name = "",
                            value = false,
                            type = DataType.BOOL,
                        ),
                    ),
                    dataType = DataType.ARRAY,
                    assignmentType = AssignmentType.ARRAY
                ),
                Assignment(
                    name = "a3",
                    value = 2.3,
                    dataType = DataType.NUMBER,
                    assignmentType = AssignmentType.SINGLE
                ),
                Assignment(
                    name = "a4",
                    value = 4L,
                    dataType = DataType.INTEGER,
                    assignmentType = AssignmentType.SINGLE
                ),
            ),
            result
        )
    }
}
