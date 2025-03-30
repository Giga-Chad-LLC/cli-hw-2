package org.hse.org.hse.cli.errors

import org.hse.cli.Value
import org.hse.org.hse.cli.utils.newLine


internal class ParsingError(msg: String) : Exception(msg)
internal class IllegalCommandArgumentsCount(commandName: String, args: List<Value>, expectedCount: Int) :
    Exception("Illegal arguments for command '$commandName': expected count $expectedCount, but got ${args.size}${if (args.isNotEmpty()) ": ${args.joinToString(", ")}" else ""}$newLine")

internal fun checkArgsCount(commandName: String, args: List<Value>, expectedCount: Int) {
    if (args.size == expectedCount) return
    throw IllegalCommandArgumentsCount(commandName, args, expectedCount)
}