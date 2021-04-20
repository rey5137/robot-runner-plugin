package com.github.rey5137.robotrunnerplugin.editors.xml

import java.lang.IllegalArgumentException
import kotlin.math.min
import kotlin.text.StringBuilder

/**
 * Some examples:
 * Arguments: [ ${a1}='qwe' | ${a2}='abc' | &amp;{dic}={'a3': 4, 'a4': 5.1, 'a5': True} ]
 * Arguments: [ ${a1}='a\'b | c \\"q' | ${a2}=None | &amp;{dic}={} ]
 * Arguments: [ ${a1}="a'b" | ${a2}='' | &amp;{dic}={'a3': {'k1': 'abc', 'k2': 1}} ]
 * Arguments: [ ${a1}='123' | @{array}=['', 7, None] ]
 */

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

const val ARRAY_START = '['
const val ARRAY_END = ']'

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

fun String.isArgumentMessage() = PATTERN_ARGUMENT_MESSAGE.toRegex().matches(this)

fun String.parseArguments(): List<Argument<*>> {
    val matchResults = PATTERN_ARGUMENT_MESSAGE.toRegex().matchEntire(this) ?: return emptyList()
    val text = matchResults.groupValues[1]
    val pointer = Pointer(value = text, end = text.length)
    val args = ArrayList<Argument<*>>()

    while (pointer.hasNext()) {
        val argument = pointer.parseArgument()
        args.add(argument)
        pointer.skipChar(ARG_SEPARATOR)
    }

    return args
}

private fun Pointer.parseArgument(): Argument<*> {
    val name: String
    val argumentType: ArgumentType
    when(peek()) {
        ARG_SINGLE -> {
            name = parseArgumentName()
            argumentType = ArgumentType.SINGLE
        }
        ARG_DICT -> {
            name = parseArgumentName()
            argumentType = ArgumentType.DICT
        }
        ARG_ARRAY -> {
            name = parseArgumentName()
            argumentType = ArgumentType.ARRAY
        }
        else -> throw IllegalArgumentException("Unexpected character: ${peek()}")
    }
    return when (peek()) {
        NONE_START -> Argument(
            name = name,
            value = parseNoneValue(),
            dataType = DataType.NONE,
            argumentType = argumentType
        )
        BOOL_START_1 -> Argument(
            name = name,
            value = parseBoolValue(true),
            dataType = DataType.BOOL,
            argumentType = argumentType
        )
        BOOL_START_2 -> Argument(
            name = name,
            value = parseBoolValue(false),
            dataType = DataType.BOOL,
            argumentType = argumentType
        )
        DICT_START -> Argument(
            name = name,
            value = parseDictValue(),
            dataType = DataType.DICT,
            argumentType = argumentType
        )
        ARRAY_START -> Argument(
            name = name,
            value = parseArrayValue(),
            dataType = DataType.ARRAY,
            argumentType = argumentType
        )
        STR_START_1 -> Argument(
            name = name,
            value = parseStringValue(STR_START_1),
            dataType = DataType.STRING,
            argumentType = argumentType
        )
        STR_START_2 -> Argument(
            name = name,
            value = parseStringValue(STR_START_2),
            dataType = DataType.STRING,
            argumentType = argumentType
        )
        else -> {
            val value = parseNumberValue()
            Argument(
                name = name,
                value = value,
                dataType = if (value is Long) DataType.INTEGER else DataType.NUMBER,
                argumentType = argumentType
            )
        }
    }
}

private fun Pointer.parseVariable(name: String): Variable<*> {
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
        ARRAY_START -> Variable(
            name = name,
            value = parseArrayValue(),
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
        else -> {
            val value = parseNumberValue()
            Variable(
                name = name,
                value = value,
                type = if (value is Long) DataType.INTEGER else DataType.NUMBER
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
    while(hasNext()) {
        val next = peek()
        if(next == DICT_END) {
            skip(1)
            break
        }
        if(next == STR_START_1 || next == STR_START_2) {
            val name = parseStringValue(next)
            skipChar(VAR_ASSIGN)
            variables.add(parseVariable(name))
            if(skipChar(VAR_SEPARATOR, DICT_END) == DICT_END)
                break
        }
        else
            throw IllegalArgumentException("Unexpected character: $next")
    }
    return variables
}

private fun Pointer.parseArrayValue(): List<Variable<*>> {
    val variables = ArrayList<Variable<*>>()
    skipChar(ARRAY_START)
    while(hasNext()) {
        val next = peek()
        if(next == ARRAY_END) {
            skip(1)
            break
        }
        else {
            variables.add(parseVariable(""))
            if (skipChar(VAR_SEPARATOR, ARRAY_END) == ARRAY_END)
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

private fun Pointer.parseArgumentName(): String {
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

private fun Pointer.skipChar(vararg values: Char): Char? {
    skipSpace()
    val next = peek() ?: return null
    if(next in values) {
        skip(1)
        skipSpace()
        return next
    }
    else
        throw IllegalArgumentException("Unexpected character: ${peek()}")
}

private fun Pointer.skipSpace() {
    while (peek() == ' ')
        skip(1)
}

data class Pointer(
    val value: String,
    var current: Int = -1,
    val end: Int
) {

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

    override fun toString() = "end=[$end] current=[$current] next=[${peek()}] value=[$value]"
}

enum class DataType {
    STRING,
    INTEGER,
    NUMBER,
    BOOL,
    NONE,
    DICT,
    ARRAY
}

enum class ArgumentType {
    SINGLE,
    DICT,
    ARRAY
}

data class Argument<T>(
    val name: String = "",
    val value: T,
    val dataType: DataType,
    val argumentType: ArgumentType
)

data class Variable<T>(
    val name: String = "",
    val value: T,
    val type: DataType
)
