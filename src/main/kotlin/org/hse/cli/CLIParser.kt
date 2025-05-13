package org.hse.cli


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
        val envVarRegex = Regex("^(\\w*)=(.*)$")
        val envVarMatch = envVarRegex.find(input.trim())
        if (envVarMatch != null) {
            val (key, valueStr) = envVarMatch.destructured
            if (key.isEmpty()) throw ParsingError("Environment variable name is required")
            if (valueStr.isEmpty()) throw ParsingError("Environment variable value is required")
            if (valueStr[0].isWhitespace()) throw ParsingError("Environment variable value must immediately follow '=' sign")
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
    internal fun parseCommand(commandStr: String): Command {
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
            GREP_COMMAND -> GrepCommand(args)
            CD_COMMAND -> CdCommand(args)
            LS_COMMAND -> LsCommand(args)
            else -> ExternalCommand(commandName, args)
        }
    }

    /**
     * Split a command string into parts, respecting quotes.
     * @param commandStr The command string to split.
     * @return The parts of the command string.
     */
    internal fun splitCommandString(commandStr: String): List<String> {
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
