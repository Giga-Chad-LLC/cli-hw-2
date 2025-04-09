package org.hse.cli

class ParsingError(msg: String) : Exception(msg)
class ValueConstructionError(msg: String) : Exception(msg)
class IllegalCommandArgumentsCountError(commandName: String, args: List<Value>, expectedCount: Int) :
    Exception("Illegal arguments for command '$commandName': expected count $expectedCount, but got ${args.size}${if (args.isNotEmpty()) ": ${args.joinToString(", ") { it.str }}" else ""}$newLine")

fun checkArgsCount(commandName: String, args: List<Value>, expectedCount: Int) {
    if (args.size == expectedCount) return
    throw IllegalCommandArgumentsCountError(commandName, args, expectedCount)
}
