package com.github.rey5137.robotrunnerplugin.editors.xml

import org.junit.Test
import kotlin.test.assertEquals

class MessageParserTest {

    @Test
    fun parseArguments__withSingleValue() {
        val arguments =
            "Arguments: [ \${a1}='a\\'b | c \"q' | \${a2}=None | \${a3}=234 | \${a4}=5.67 | \${a5}=True | \${a6}=False ]".parseArguments()
        assertEquals(
            Argument(
                name = "a1",
                value = "a'b | c \"q",
                dataType = DataType.STRING,
                argumentType = ArgumentType.SINGLE,
                rawValue = "'a\\'b | c \"q'"
            ), arguments[0]
        )
        assertEquals(
            Argument(
                name = "a2",
                value = null,
                dataType = DataType.NONE,
                argumentType = ArgumentType.SINGLE,
                rawValue = "None"
            ), arguments[1]
        )
        assertEquals(
            Argument(
                name = "a3",
                value = 234L,
                dataType = DataType.INTEGER,
                argumentType = ArgumentType.SINGLE,
                rawValue = "234"
            ), arguments[2]
        )
        assertEquals(
            Argument(
                name = "a4",
                value = 5.67,
                dataType = DataType.NUMBER,
                argumentType = ArgumentType.SINGLE,
                rawValue = "5.67"
            ), arguments[3]
        )
        assertEquals(
            Argument(
                name = "a5",
                value = true,
                dataType = DataType.BOOL,
                argumentType = ArgumentType.SINGLE,
                rawValue = "True"
            ), arguments[4]
        )
        assertEquals(
            Argument(
                name = "a6",
                value = false,
                dataType = DataType.BOOL,
                argumentType = ArgumentType.SINGLE,
                rawValue = "False"
            ), arguments[5]
        )
    }

    @Test
    fun parseArguments__withDictValue() {
        val arguments =
            "Arguments: [ \${a1}={'k1': 'abc', 'k2': {'k3': None}} | &{a2}={'a\\'\"3': True, \"a'4\": 5.1, 'a5': 4} ]".parseArguments()
        assertEquals(
            Argument<List<Variable<*>>>(
                name = "a1",
                value = listOf(
                    Variable(
                        name = "k1",
                        value = "abc",
                        type = DataType.STRING,
                    ),
                    Variable<List<Variable<*>>>(
                        name = "k2",
                        value = listOf(
                            Variable(
                                name = "k3",
                                value = null,
                                type = DataType.NONE,
                            ),
                        ),
                        type = DataType.DICT,
                    ),
                ),
                dataType = DataType.DICT,
                argumentType = ArgumentType.SINGLE,
                rawValue = "{'k1': 'abc', 'k2': {'k3': None}}"
            ), arguments[0]
        )
        assertEquals(
            Argument<List<Variable<*>>>(
                name = "a2",
                value = listOf(
                    Variable(
                        name = "a'\"3",
                        value = true,
                        type = DataType.BOOL
                    ),
                    Variable(
                        name = "a'4",
                        value = 5.1,
                        type = DataType.NUMBER
                    ),
                    Variable(
                        name = "a5",
                        value = 4L,
                        type = DataType.INTEGER
                    )
                ),
                dataType = DataType.DICT,
                argumentType = ArgumentType.DICT,
                rawValue = "{'a\\'\"3': True, \"a'4\": 5.1, 'a5': 4}"
            ), arguments[1]
        )
    }

    @Test
    fun parseArguments__withArrayValue() {
        val arguments =
            "Arguments: [ \${a1}=[2.3, {'k1': 'abc'}] | @{a2}=['', None, [1]] ]".parseArguments()

        assertEquals(Argument<List<Variable<*>>>(
            name = "a1",
            value = listOf(
                Variable(
                    name = "[0]",
                    value = 2.3,
                    type = DataType.NUMBER,
                ),
                Variable<List<Variable<*>>>(
                    name = "[1]",
                    value = listOf(
                        Variable(
                            name = "k1",
                            value = "abc",
                            type = DataType.STRING
                        )
                    ),
                    type = DataType.DICT
                ),
            ),
            dataType = DataType.ARRAY,
            argumentType = ArgumentType.SINGLE,
            rawValue = "[2.3, {'k1': 'abc'}]"
        ), arguments[0])
        assertEquals(Argument<List<Variable<*>>>(
            name = "a2",
            value = listOf(
                Variable(
                    name = "[0]",
                    value = "",
                    type = DataType.STRING,
                ),
                Variable(
                    name = "[1]",
                    value = null,
                    type = DataType.NONE,
                ),
                Variable<List<Variable<*>>>(
                    name = "[2]",
                    value = listOf(
                        Variable(
                            name = "[0]",
                            value = 1L,
                            type = DataType.INTEGER
                        )
                    ),
                    type = DataType.ARRAY,
                )
            ),
            dataType = DataType.ARRAY,
            argumentType = ArgumentType.ARRAY,
            rawValue = "['', None, [1]]"
        ), arguments[1])
    }

    @Test
    fun parseArguments__withPythonArgument() {
        val arguments =
            "Arguments: [ '123' | a3={'k1': 'abc', 'k2': 1} | a2='456' ]".parseArguments()

        assertEquals(Argument(
            name = "",
            value = "123",
            dataType = DataType.STRING,
            argumentType = ArgumentType.PYTHON,
            rawValue = "'123'"
        ), arguments[0])
        assertEquals(Argument(
            name = "a3",
            value = listOf(
                Variable(
                    name="k1",
                    value = "abc",
                    type = DataType.STRING,
                ),
                Variable(
                    name="k2",
                    value = 1L,
                    type = DataType.INTEGER,
                )
            ),
            dataType = DataType.DICT,
            argumentType = ArgumentType.PYTHON,
            rawValue = "{'k1': 'abc', 'k2': 1}"
        ), arguments[1])
        assertEquals(Argument(
            name = "a2",
            value = "456",
            dataType = DataType.STRING,
            argumentType = ArgumentType.PYTHON,
            rawValue = "'456'"
        ), arguments[2])
    }

    @Test
    fun parseReturn__withSingleVariable() {
        "Return: 'qwe'".parseReturn().let {
            assertEquals(Variable(
                name = "",
                value = "qwe",
                type = DataType.STRING,
            ), it)
        }

        "Return: None".parseReturn().let {
            assertEquals(Variable(
                name = "",
                value = null,
                type = DataType.NONE,
            ), it)
        }

        "Return: True".parseReturn().let {
            assertEquals(Variable(
                name = "",
                value = true,
                type = DataType.BOOL,
            ), it)
        }

        "Return: 1".parseReturn().let {
            assertEquals(Variable(
                name = "",
                value = 1L,
                type = DataType.INTEGER,
            ), it)
        }

        "Return: 2.3".parseReturn().let {
            assertEquals(Variable(
                name = "",
                value = 2.3,
                type = DataType.NUMBER,
            ), it)
        }
    }

    @Test
    fun parseReturn_withDictVariable() {
        val variable = "Return: {'a2': 2, 'a3': '3'}".parseReturn()
        assertEquals(Variable(
            name = "",
            value = listOf(
                Variable(
                    name = "a2",
                    value = 2L,
                    type = DataType.INTEGER,
                ),
                Variable(
                    name = "a3",
                    value = "3",
                    type = DataType.STRING,
                )
            ),
            type = DataType.DICT,
        ), variable)
    }

    @Test
    fun parseReturn__withArrayVariable() {
        val variable = "Return: ['1', ['a | e', 'b']]".parseReturn()
        assertEquals(Variable(
            name = "",
            value = listOf(
                Variable(
                    name = "[0]",
                    value = "1",
                    type = DataType.STRING,
                ),
                Variable(
                    name = "[1]",
                    value = listOf(
                        Variable(
                            name = "[0]",
                            value = "a | e",
                            type = DataType.STRING,
                        ),
                        Variable(
                            name = "[1]",
                            value = "b",
                            type = DataType.STRING,
                        ),
                    ),
                    type = DataType.ARRAY,
                )
            ),
            type = DataType.ARRAY,
        ), variable)
    }

    @Test
    fun parseReturn__withArrayVariable2() {
        val variable = "Return: ('123', {'a2': '2', 'a3': '3'})".parseReturn()
        assertEquals(Variable(
            name = "",
            value = listOf(
                Variable(
                    name = "[0]",
                    value = "123",
                    type = DataType.STRING,
                ),
                Variable(
                    name = "[1]",
                    value = listOf(
                        Variable(
                            name = "a2",
                            value = "2",
                            type = DataType.STRING,
                        ),
                        Variable(
                            name = "a3",
                            value = "3",
                            type = DataType.STRING,
                        ),
                    ),
                    type = DataType.DICT,
                )
            ),
            type = DataType.ARRAY,
        ), variable)
    }
}