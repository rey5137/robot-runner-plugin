package com.github.rey5137.robotrunnerplugin.editors.xml

fun List<Argument<*>>.parseArgumentInputs(inputs: List<String>): List<List<InputArgument>> {
    val argumentMap = LinkedHashMap<String, Holder>()
    var arrayHolder: Holder? = null
    var dictHolder: Holder? = null

    forEach {
        argumentMap[it.name] = Holder(it)
        if (it.argumentType == ArgumentType.ARRAY)
            arrayHolder = argumentMap[it.name]
        else if (it.argumentType == ArgumentType.DICT)
            dictHolder = argumentMap[it.name]
    }

    inputs.forEach { input ->
        val (name, value) = input.parseInput()
        if (name != null && argumentMap.containsKey(name)) {
            argumentMap[name]!!.addInput(value = value, rawInput = input)
        } else if (name != null && dictHolder.containsVariable(name)) {
            dictHolder!!.addInput(name = name, value = value, rawInput = input)
        } else {
            val holder = argumentMap.findFirstEmptyInput()
            when {
                holder != null -> {
                    when (holder.argument.argumentType) {
                        ArgumentType.SINGLE, ArgumentType.ARRAY -> holder.addInput(value = input, rawInput = input)
                        ArgumentType.DICT -> {
                            when {
                                name != null -> holder.addInput(name = name, value = value, rawInput = input)
                                arrayHolder != null -> arrayHolder!!.addInput(value = input, rawInput = input)
                                else -> holder.addInput(value = input, rawInput = input)
                            }
                        }
                    }
                }
                name == null -> arrayHolder?.addInput(value = input, rawInput = input)
                dictHolder != null -> dictHolder!!.addInput(name = name, value = value, rawInput = input)
                else -> arrayHolder?.addInput(value = input, rawInput = input)
            }
        }
    }

    return argumentMap.values.map { it.inputs }
}

private fun String.parseInput(): Pair<String?, String> {
    val regex = "^([^\\s=]*)\\s*=(.*)?".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null to this
    else
        result.groupValues[1] to result.groupValues[2]
}

private fun LinkedHashMap<String, Holder>.findFirstEmptyInput(): Holder? = values.firstOrNull { it.inputs.isEmpty() }

private fun Holder?.containsVariable(name: String): Boolean {
    if (this == null)
        return false
    if (this.argument.argumentType != ArgumentType.DICT || this.argument.dataType != DataType.DICT)
        return false
    val variables = this.argument.value as List<Variable<*>>
    return variables.firstOrNull { it.name == name } != null
}

private data class Holder(
    val argument: Argument<*>,
    val inputs: MutableList<InputArgument> = mutableListOf()
) {

    fun addInput(name: String? = null, value: String, rawInput: String) {
        inputs.add(InputArgument(name = name, value = value, rawInput = rawInput))
    }
}

