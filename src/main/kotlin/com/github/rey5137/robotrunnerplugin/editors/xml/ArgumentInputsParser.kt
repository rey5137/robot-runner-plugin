package com.github.rey5137.robotrunnerplugin.editors.xml

fun List<Argument<*>>.parseArgumentInputs(inputs: List<String>): List<List<InputArgument>> {
    val hasPythonArgument = firstOrNull { it.argumentType == ArgumentType.PYTHON } != null
    return if(hasPythonArgument) parsePythonArgumentInputs(inputs) else parseRobotArgumentInputs(inputs)
}

private fun List<Argument<*>>.parseRobotArgumentInputs(inputs: List<String>): List<List<InputArgument>> {
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
        if (name != null && argumentMap.containsKey(name))
            argumentMap[name]!!.addInput(value = value, rawInput = input)
        else if (name != null && dictHolder.containsVariable(name))
            dictHolder!!.addInput(name = name, value = value, rawInput = input)
        else if(name == null && dictHolder != null && value.isDictInput())
            dictHolder!!.addInput(value = value, rawInput = input)
        else if(name == null && arrayHolder != null && value.isArrayInput())
            arrayHolder!!.addInput(value = value, rawInput = input)
        else {
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
                        else -> {}
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

private fun List<Argument<*>>.parsePythonArgumentInputs(inputs: List<String>): List<List<InputArgument>> {
    return mapIndexed { index, argument ->
        val input = inputs[index]
        if(argument.name.isEmpty())
            listOf(InputArgument(value = input, rawInput = input))
        else {
            val (name, value) = inputs[index].parseInput()
            if (name == argument.name)
                listOf(InputArgument(value = value, rawInput = input))
            else
                listOf(InputArgument(value = input, rawInput = input))
        }
    }
}

private fun String.parseInput(): Pair<String?, String> {
    val regex = "^([^\\s=]*)\\s*=(.*)?".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    val result = regex.matchEntire(this)
    return if (result == null)
        null to this
    else
        result.groupValues[1] to result.groupValues[2]
}

private fun String.isDictInput(): Boolean {
    val regex = "^&\\{.*}$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    return regex.matches(this)
}

private fun String.isArrayInput(): Boolean {
    val regex = "^@\\{.*}$".toRegex(option = RegexOption.DOT_MATCHES_ALL)
    return regex.matches(this)
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

