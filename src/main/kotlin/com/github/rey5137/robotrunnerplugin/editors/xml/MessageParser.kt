package com.github.rey5137.robotrunnerplugin.editors.xml

import java.lang.IllegalArgumentException
import kotlin.math.min
import kotlin.text.StringBuilder

const val ARG_SINGLE = '$'
const val ARG_DICT = '&'
const val ARG_ARRAY = '@'
const val ARG_NAME_START = '{'
const val ARG_NAME_END = '}'

const val ARG_ASSIGN = '='
const val VAR_ASSIGN = ':'

const val ARG_SEPARATOR = '|'
const val VAR_SEPARATOR = ','

const val STR_START_1 = '\''
const val STR_START_2 = '"'

const val ARRAY_START_1 = '['
const val ARRAY_END_1 = ']'

const val ARRAY_START_2 = '('
const val ARRAY_END_2 = ')'

const val DICT_START = '{'
const val DICT_END = '}'

const val NONE_START = 'N'

const val BOOL_START_1 = 'T'
const val BOOL_START_2 = 'F'

const val ESCAPE_CHAR = '\\'

const val VAL_NONE = "None"
const val VAL_TRUE = "True"
const val VAL_FALSE = "False"

const val PATTERN_ARGUMENT_MESSAGE = "Arguments:\\s*\\[\\s*(.*)\\s*\\]"
const val PATTERN_PYTHON_ARGUMENT = "^[\\w\\d]*$"
const val PATTERN_RETURN_MESSAGE = "Return:\\s*(.*)"

const val VAL_ORDERED_DICT = "OrderedDict"

fun String.isArgumentMessage() = PATTERN_ARGUMENT_MESSAGE.toRegex(option = RegexOption.DOT_MATCHES_ALL).matches(this)
fun String.isReturnMessage() = PATTERN_RETURN_MESSAGE.toRegex(option = RegexOption.DOT_MATCHES_ALL).matches(this)

/**
 * Some examples:
 * Arguments: [ ${a1}='qwe' | ${a2}='abc' | &amp;{dic}={'a3': 4, 'a4': 5.1, 'a5': True} ]
 * Arguments: [ ${a1}='a\'b | c \\"q' | ${a2}=None | &amp;{dic}={} ]
 * Arguments: [ ${a1}="a'b" | ${a2}='' | &amp;{dic}={'a3': {'k1': 'abc', 'k2': 1}} ]
 * Arguments: [ ${a1}='123' | @{array}=['', 7, None] ]
 */
fun String.parseArguments(): List<Argument<*>> {
    val matchResults =
        PATTERN_ARGUMENT_MESSAGE.toRegex(option = RegexOption.DOT_MATCHES_ALL).matchEntire(this)
            ?: return emptyList()
    val text = matchResults.groupValues[1]
    val pointer = Pointer(value = text)
    val args = ArrayList<Argument<*>>()

    while (pointer.hasNext()) {
        val argument = pointer.parseArgument()
        args.add(argument)
        pointer.skipChar(ARG_SEPARATOR)
    }

    return args
}

/**
 * Some examples:
 * Return: ('123', ('a2=2', '3'))
 * Return: ('123', {'a2': '2', 'a3': '3'})
 * Return: ['1', ['a | e', 'b', 'c']]
 * Return: 'qwe'
 */
fun String.parseReturn(): Variable<*> {
    val matchResults = PATTERN_RETURN_MESSAGE.toRegex(option = RegexOption.DOT_MATCHES_ALL).matchEntire(this)
        ?: return VARIABLE_EMPTY
    val text = matchResults.groupValues[1].trim()
    val pointer = Pointer(value = text)
    return pointer.parseVariable("")
}

private fun Pointer.parseArgument(): Argument<*> {
    val name: String
    val argumentType: ArgumentType
    when (peek()) {
        ARG_SINGLE -> {
            name = parseRobotArgumentName()
            argumentType = ArgumentType.SINGLE
        }
        ARG_DICT -> {
            name = parseRobotArgumentName()
            argumentType = ArgumentType.DICT
        }
        ARG_ARRAY -> {
            name = parseRobotArgumentName()
            argumentType = ArgumentType.ARRAY
        }
        else -> {
            name = parsePythonArgumentName()
            argumentType = ArgumentType.PYTHON
        }
    }
    val startIndex = current + 1
    return when (peek()) {
        NONE_START -> Argument(
            name = name,
            value = parseNoneValue(),
            dataType = DataType.NONE,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        BOOL_START_1 -> Argument(
            name = name,
            value = parseBoolValue(true),
            dataType = DataType.BOOL,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        BOOL_START_2 -> Argument(
            name = name,
            value = parseBoolValue(false),
            dataType = DataType.BOOL,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        DICT_START -> Argument(
            name = name,
            value = parseDictValue(),
            dataType = DataType.DICT,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        ARRAY_START_1 -> Argument(
            name = name,
            value = parseArrayValue(ARRAY_START_1, ARRAY_END_1),
            dataType = DataType.ARRAY,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        ARRAY_START_2 -> Argument(
            name = name,
            value = parseArrayValue(ARRAY_START_2, ARRAY_END_2),
            dataType = DataType.ARRAY,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        STR_START_1 -> Argument(
            name = name,
            value = parseStringValue(STR_START_1),
            dataType = DataType.STRING,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        STR_START_2 -> Argument(
            name = name,
            value = parseStringValue(STR_START_2),
            dataType = DataType.STRING,
            argumentType = argumentType,
            rawValue = value.substring(startIndex, current + 1).trim()
        )
        else -> {
            val numValue = parseNumberValue()
            Argument(
                name = name,
                value = numValue,
                dataType = if (numValue is Long) DataType.INTEGER else DataType.NUMBER,
                argumentType = argumentType,
                rawValue = value.substring(startIndex, current + 1).trim()
            )
        }
    }
}

private fun Pointer.parseVariable(name: String, parentEndChar: Char? = null): Variable<*> {
    return when (peek()) {
        NONE_START -> Variable(
            name = name,
            value = parseNoneValue(),
            type = DataType.NONE
        )
        BOOL_START_1 -> Variable(
            name = name,
            value = parseBoolValue(true),
            type = DataType.BOOL
        )
        BOOL_START_2 -> Variable(
            name = name,
            value = parseBoolValue(false),
            type = DataType.BOOL
        )
        DICT_START -> Variable(
            name = name,
            value = parseDictValue(),
            type = DataType.DICT
        )
        ARRAY_START_1 -> Variable(
            name = name,
            value = parseArrayValue(ARRAY_START_1, ARRAY_END_1),
            type = DataType.ARRAY
        )
        ARRAY_START_2 -> Variable(
            name = name,
            value = parseArrayValue(ARRAY_START_2, ARRAY_END_2),
            type = DataType.ARRAY
        )
        STR_START_1 -> Variable(
            name = name,
            value = parseStringValue(STR_START_1),
            type = DataType.STRING
        )
        STR_START_2 -> Variable(
            name = name,
            value = parseStringValue(STR_START_2),
            type = DataType.STRING
        )
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
            val value = parseNumberValue()
            Variable(
                name = name,
                value = value,
                type = if (value is Long) DataType.INTEGER else DataType.NUMBER
            )
        }
        else -> {
            val isOrderedDict = peek(VAL_ORDERED_DICT.length) == VAL_ORDERED_DICT
            Variable(
                name = name,
                value = when {
                    isOrderedDict -> parseOrderedDictValue()
                    parentEndChar == null -> parseRawStringValue(VAR_SEPARATOR)
                    else -> parseRawStringValue(VAR_SEPARATOR, parentEndChar)
                },
                type = if (isOrderedDict) DataType.DICT else DataType.STRING,
                childOrdered = isOrderedDict
            )
        }
    }
}

private fun Pointer.parseNoneValue(): Any? {
    return if (peek(4).equals(VAL_NONE, ignoreCase = true)) {
        skip(4)
        null
    } else
        throw IllegalArgumentException("Unexpected value ${peek(4)}")
}

private fun Pointer.parseBoolValue(expected: Boolean): Boolean {
    val value = if (expected) peek(4) else peek(5)
    return when {
        value.equals(VAL_TRUE, ignoreCase = true) -> {
            skip(4)
            true
        }
        value.equals(VAL_FALSE, ignoreCase = true) -> {
            skip(5)
            false
        }
        else -> throw IllegalArgumentException("Unexpected value $value")
    }
}

private fun Pointer.parseDictValue(): List<Variable<*>> {
    val variables = ArrayList<Variable<*>>()
    skipChar(DICT_START)
    while (hasNext()) {
        val next = peek()
        if (next == DICT_END) {
            skip(1)
            break
        }
        if (next == STR_START_1 || next == STR_START_2) {
            val name = parseStringValue(next)
            skipChar(VAR_ASSIGN)
            variables.add(parseVariable(name, DICT_END))
            if (skipChar(VAR_SEPARATOR, DICT_END) == DICT_END)
                break
        } else
            throw IllegalArgumentException("Unexpected character: $next")
    }
    return variables
}

private fun Pointer.parseArrayValue(arrayStart: Char, arrayEnd: Char): List<Variable<*>> {
    val variables = ArrayList<Variable<*>>()
    skipChar(arrayStart)
    while (hasNext()) {
        val next = peek()
        if (next == arrayEnd) {
            skip(1)
            break
        } else {
            variables.add(parseVariable("[${variables.size}]", arrayEnd))
            if (skipChar(VAR_SEPARATOR, arrayEnd) == arrayEnd)
                break
        }
    }
    return variables
}

private fun Pointer.parseStringValue(wrapChar: Char): String {
    val builder = StringBuilder()
    skip(1)
    while (hasNext()) {
        var next = next()
        if (next == ESCAPE_CHAR && hasNext())
            next = next()
        else if (next == wrapChar)
            break
        builder.append(next)
    }
    return builder.toString()
}

private fun Pointer.parseNumberValue(): Any {
    val builder = StringBuilder()
    var dotCount = 0
    while (hasNext()) {
        val next = peek()
        if (next in '0'..'9')
            builder.append(next())
        else if (next == '.') {
            if (dotCount == 0) {
                dotCount = 1
                builder.append(next())
            } else
                break
        } else
            break
    }
    return if (dotCount == 0) builder.toString().toLong() else builder.toString().toDouble()
}

private fun Pointer.parseRawStringValue(vararg stopChars: Char): String {
    val builder = StringBuilder()
    while (hasNext()) {
        val peek = peek()
        if (peek == null || stopChars.contains(peek))
            break
        builder.append(next())
    }
    return builder.toString()
}

private fun Pointer.parseOrderedDictValue(): List<Variable<*>> {
    skip(VAL_ORDERED_DICT.length)
    skipChar(ARRAY_START_2)
    val variables = if (peek() == ARRAY_START_1) {
        parseArrayValue(ARRAY_START_1, ARRAY_END_1)
            .map { itemVar ->
                val children = itemVar.value as List<Variable<*>>
                Variable(
                    name = children[0].value.toString(),
                    value = children[1].value,
                    type = children[1].type,
                )
            }
    }
    else
        emptyList()
    skipChar(ARRAY_END_2)
    return variables
}

private fun Pointer.parseRobotArgumentName(): String {
    skip(1)
    val builder = StringBuilder()
    var count = 0

    while (hasNext()) {
        val next = next()
        if (next == ARG_NAME_START) {
            count++
            if (count == 1)
                continue
        } else if (next == ARG_NAME_END) {
            count--
            if (count == 0) {
                skipChar(ARG_ASSIGN)
                break
            }
        }
        builder.append(next)
    }
    return builder.toString()
}

private fun Pointer.parsePythonArgumentName(): String {
    val assignIndex = nextIndex(ARG_ASSIGN)
    if (assignIndex < 0)
        return ""
    val length = assignIndex - current
    val name = peek(length - 1).trim()
    if (PATTERN_PYTHON_ARGUMENT.toRegex().matches(name)) {
        skip(length)
        return name
    }
    return ""
}

private fun Pointer.skipChar(vararg values: Char): Char? {
    skipSpace()
    val next = peek() ?: return null
    if (next in values) {
        skip(1)
        skipSpace()
        return next
    } else
        throw IllegalArgumentException("Unexpected character: ${peek()}")
}

private fun Pointer.skipSpace() {
    while (peek() == ' ' || peek() == '\n')
        skip(1)
}

private data class Pointer(
    val value: String,
    var current: Int = -1
) {

    val end = value.length

    fun hasNext() = current < end - 1

    fun next(): Char {
        current++
        return value[current]
    }

    fun peek(): Char? = if (hasNext()) value[current + 1] else null

    fun skip(n: Int) {
        current = min(current + n, end - 1)
    }

    fun peek(n: Int): String {
        return if (hasNext())
            value.substring(current + 1, min(current + 1 + n, end))
        else ""
    }

    fun nextIndex(c: Char): Int {
        ((current + 1) until end).forEach {
            if (value[it] == c)
                return it
        }
        return -1
    }

    override fun toString() =
        "end=[$end] current=[$current] next=[${peek()}] ${
            value.substring(
                current + 1,
                Math.min(value.length, current + 21)
            )
        }"
}