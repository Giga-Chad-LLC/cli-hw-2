package org.hse.cli

import org.hse.org.hse.cli.errors.ParsingError
import org.hse.org.hse.cli.utils.*
import org.hse.org.hse.cli.utils.CAT_COMMAND
import org.hse.org.hse.cli.utils.ECHO_COMMAND
import org.hse.org.hse.cli.utils.EXIT_COMMAND
import org.hse.org.hse.cli.utils.PWD_COMMAND
import org.hse.org.hse.cli.utils.WC_COMMAND

/**
 * Class for parsing CLI input into tasks.
 */
class CLIParser {
    /**
     * Parse the input string into a task.
     * @param input The input string to parse.
     * @return The parsed task.
     */
    fun parse(input: String): Task {
        if (input.isBlank()) {
            return PipelineTask(emptyList())
        }

        // Check if the input is a environment variable assignment
        val envVarRegex = Regex("^(\\w+)=(.*)$")
        val envVarMatch = envVarRegex.find(input.trim())
        if (envVarMatch != null) {
            val (key, valueStr) = envVarMatch.destructured
            val value = ValueFactory.constructFromString(valueStr)
            return ModifyEnvironmentTask(key, value)
        }

        // Parse the input as a pipeline of commands
        val commands = mutableListOf<Command>()
        val commandStrings = input.split("|")
        for (commandStr in commandStrings) {
            val command = parseCommand(commandStr.trim())
            commands.add(command)
        }
        if (commands.isNotEmpty()) {
            return PipelineTask(commands)
        }

        throw ParsingError("Cannot parse input: \"$input\"")
    }

    /**
     * Parse a command string into a Command object.
     * @param commandStr The command string to parse.
     * @return The parsed Command object, or null if the command is invalid.
     */
    private fun parseCommand(commandStr: String): Command {
        if (commandStr.isBlank()) {
            throw ParsingError("Empty command in the pipeline")
        }

        val parts = splitCommandString(commandStr)
        if (parts.isEmpty()) {
            throw ParsingError("Empty command in the pipeline")
        }

        val commandName = parts[0]
        val args = parts.subList(1, parts.size).map { ValueFactory.constructFromString(it) }

        return when (commandName) {
            EXIT_COMMAND -> ExitCommand(args)
            PWD_COMMAND -> PwdCommand(args)
            ECHO_COMMAND -> EchoCommand(args)
            CAT_COMMAND -> CatCommand(args)
            WC_COMMAND -> WcCommand(args)
            else -> ExternalCommand(commandName, args)
        }
    }

    /**
     * Split a command string into parts, respecting quotes.
     * @param commandStr The command string to split.
     * @return The parts of the command string.
     */
    private fun splitCommandString(commandStr: String): List<String> {
        val parts = mutableListOf<String>()
        var currentPart = StringBuilder()
        var inSingleQuotes = false
        var inDoubleQuotes = false
        var i = 0

        while (i < commandStr.length) {
            val c = commandStr[i]
            when {
                c == '\'' && !inDoubleQuotes -> {
                    inSingleQuotes = !inSingleQuotes
                    currentPart.append(c)
                }
                c == '"' && !inSingleQuotes -> {
                    inDoubleQuotes = !inDoubleQuotes
                    currentPart.append(c)
                }
                c.isWhitespace() && !inSingleQuotes && !inDoubleQuotes -> {
                    if (currentPart.isNotEmpty()) {
                        parts.add(currentPart.toString())
                        currentPart = StringBuilder()
                    }
                }
                else -> {
                    currentPart.append(c)
                }
            }
            i++
        }

        if (currentPart.isNotEmpty()) {
            parts.add(currentPart.toString())
        }

        return parts
    }
}