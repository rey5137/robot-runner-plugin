package com.github.rey5137.robotrunnerplugin.editors.xml

const val PATTERN_ASSIGNMENT = "[$@]\\{(.*)\\}"

fun List<String>.parseAssignments(variable: Variable<*>?): List<Assignment<*>> {
    if(this.isEmpty())
        return emptyList()
    if(variable == null)
        return this.map {
            Assignment(
                name = it.getAssignmentName(),
                value = null,
                dataType = DataType.NONE,
                assignmentType = it.getAssignmentType(),
                hasValue = false
            )
        }
    if(size == 1)
        return listOf(first().toAssignment(variable))
    if(variable.type != DataType.ARRAY)
        throw IllegalArgumentException()

    val names = this.toMutableList()
    val variables = (variable.value as List<Variable<*>>).toMutableList()
    val result = arrayListOf<Assignment<*>>()

    while(names.isNotEmpty()) {
        val name = names.first()
        if(name.getAssignmentType() == AssignmentType.ARRAY)
            break
        result.add(name.toAssignment(variables.first()))
        names.removeAt(0)
        variables.removeAt(0)
    }

    if(names.isNotEmpty()){
        val rightResult = arrayListOf<Assignment<*>>()
        while(names.isNotEmpty()) {
            val name = names.last()
            if(name.getAssignmentType() == AssignmentType.ARRAY)
                break
            rightResult.add(name.toAssignment(variables.last()))
            names.removeAt(names.size - 1)
            variables.removeAt(variables.size - 1)
        }

        if(names.size != 1)
            throw IllegalArgumentException()

        val name = names.first()
        result.add(Assignment(
            name = name.getAssignmentName(),
            value = variables.mapIndexed { index, v -> v.copy(name = "[$index]") },
            dataType = DataType.ARRAY,
            assignmentType = AssignmentType.ARRAY,
        ))
        result.addAll(rightResult.asReversed())
    }

    return result
}

private fun String.toAssignment(variable: Variable<*>): Assignment<*> {
    return Assignment(
        name = getAssignmentName(),
        value = variable.value,
        dataType = variable.type,
        assignmentType = getAssignmentType()
    )
}

private fun String.getAssignmentName(): String {
    val result = PATTERN_ASSIGNMENT.toRegex().matchEntire(this)
    return result?.groupValues?.get(1) ?: ""
}

private fun String.getAssignmentType(): AssignmentType  = when(this[0]) {
    ARG_SINGLE -> AssignmentType.SINGLE
    ARG_ARRAY -> AssignmentType.ARRAY
    else -> throw IllegalArgumentException("Unexpected character: ${this[0]}")
}