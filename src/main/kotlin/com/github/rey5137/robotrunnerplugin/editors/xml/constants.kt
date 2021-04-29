package com.github.rey5137.robotrunnerplugin.editors.xml

const val TAG_ROBOT = "robot"
const val TAG_SUITE = "suite"
const val TAG_TEST = "test"
const val TAG_KEYWORD = "kw"
const val TAG_STATUS = "status"

const val TAG_GENERATOR = "generator"
const val TAG_GENERATED = "generated"
const val TAG_ID = "id"
const val TAG_NAME = "name"
const val TAG_SOURCE = "source"
const val TAG_LIBRARY = "library"
const val TAG_ARGUMENTS = "arguments"
const val TAG_TAGS = "tags"
const val TAG_ARGUMENT = "arg"
const val TAG_TAG = "tag"
const val TAG_DOC = "doc"
const val TAG_MESSAGE = "msg"
const val TAG_TIMESTAMP = "timestamp"
const val TAG_LEVEL = "level"
const val TAG_START_TIME = "starttime"
const val TAG_END_TIME = "endtime"
const val TAG_ASSIGN = "assign"
const val TAG_VAR = "var"
const val TAG_TYPE = "type"

interface Element

interface HasCommonField {

    val name: String

    val status: StatusElement
}

interface HasTagsField {

    val tags: List<String>

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
    val argumentType: ArgumentType,
    val rawValue: String,
)

data class Variable<T>(
    val name: String = "",
    val value: T,
    val type: DataType
)

data class InputArgument(
    val name: String? = null,
    val value: String,
    val rawInput: String,
)

val ARGUMENT_EMPTY = Argument<Any?>(value = null, dataType = DataType.NONE, argumentType = ArgumentType.SINGLE, rawValue = "")
val VARIABLE_EMPTY = Variable<Any?>(type = DataType.NONE, value = null)
val INPUT_EMPTY = InputArgument(value = "", rawInput = "")