package com.github.rey5137.robotrunnerplugin.editors.xml

import org.junit.Test
import kotlin.test.assertEquals

class ArgumentsInputParserTest {

    @Test
    fun parseArgumentInputs__withSingleAndDict__withNamedArgument() {
        val arguments = listOf<Argument<*>>(
            mockArgument("a1", ArgumentType.SINGLE),
            mockArgument("a2", ArgumentType.SINGLE),
            mockArgument("dic", ArgumentType.DICT)
        )
        val inputs = listOf(
            "a2=abc",
            "a1=qwe",
            "a3=\${4}",
            "a'4=\${5.1}",
            "a'\"5=\${True}"
        )
        val result = arguments.parseArgumentInputs(inputs)

        assertEquals(
            listOf(
                listOf(
                    InputArgument(value = "qwe", rawInput = "a1=qwe")
                ),
                listOf(
                    InputArgument(value = "abc", rawInput = "a2=abc")
                ),
                listOf(
                    InputArgument(name = "a3", value = "\${4}", rawInput = "a3=\${4}"),
                    InputArgument(name = "a'4", value = "\${5.1}", rawInput = "a'4=\${5.1}"),
                    InputArgument(name = "a'\"5", value = "\${True}", rawInput = "a'\"5=\${True}")
                )
            ),
            result
        )
    }

    @Test
    fun parseArgumentInputs__withSingleAndDict__withPositionalArgument() {
        val arguments = listOf<Argument<*>>(
            mockArgument("a1", ArgumentType.SINGLE),
            mockArgument("a2", ArgumentType.SINGLE),
            mockArgument("dic", ArgumentType.DICT)
        )
        val inputs = listOf(
            "a'b | c \"q",
            "\${None}",
        )
        val result = arguments.parseArgumentInputs(inputs)

        assertEquals(
            listOf(
                listOf(
                    InputArgument(value = "a'b | c \"q", rawInput = "a'b | c \"q")
                ),
                listOf(
                    InputArgument(value = "\${None}", rawInput = "\${None}")
                ),
                emptyList()
            ),
            result
        )
    }

    @Test
    fun parseArgumentInputs__withSingleAndArray__withPositionalArgument() {
        val arguments = listOf<Argument<*>>(
            mockArgument("a1", ArgumentType.SINGLE),
            mockArgument("array", ArgumentType.ARRAY)
        )
        val inputs = listOf(
            "123",
            "\${Empty}",
            "\${7}",
            "a2=\${None}",
            "\${dict}"
        )
        val result = arguments.parseArgumentInputs(inputs)

        assertEquals(
            listOf(
                listOf(
                    InputArgument(value = "123", rawInput = "123")
                ),
                listOf(
                    InputArgument(value = "\${Empty}", rawInput = "\${Empty}"),
                    InputArgument(value = "\${7}", rawInput = "\${7}"),
                    InputArgument(value = "a2=\${None}", rawInput = "a2=\${None}"),
                    InputArgument(value = "\${dict}", rawInput = "\${dict}"),
                )
            ),
            result
        )
    }

    @Test
    fun parseArgumentInputs__withArrayAndDict() {
        val arguments = listOf<Argument<*>>(
            mockArgument("a", ArgumentType.SINGLE),
            mockArgument("array", ArgumentType.ARRAY),
            mockArgument("dic", ArgumentType.DICT)
        )
        val inputs = listOf(
            "1",
            "a",
            "b",
            "c",
            "k1=1",
            "k2=2"
        )
        val result = arguments.parseArgumentInputs(inputs)

        assertEquals(
            listOf(
                listOf(
                    InputArgument(value = "1", rawInput = "1")
                ),
                listOf(
                    InputArgument(value = "a", rawInput = "a"),
                    InputArgument(value = "b", rawInput = "b"),
                    InputArgument(value = "c", rawInput = "c"),
                ),
                listOf(
                    InputArgument(name = "k1", value = "1", rawInput = "k1=1"),
                    InputArgument(name = "k2", value = "2", rawInput = "k2=2"),
                ),
            ),
            result
        )
    }

    private fun mockArgument(name: String, argumentType: ArgumentType): Argument<*> {
        return Argument(
            name = name,
            value = "",
            dataType = DataType.STRING,
            argumentType = argumentType,
            rawValue = "",
        )
    }

}