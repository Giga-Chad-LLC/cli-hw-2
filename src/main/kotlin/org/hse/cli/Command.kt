package org.hse.cli

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Interface for all commands in the CLI.
 */
interface Command {
    /**
     * Stores the string representation name of the command.
     */
    val name: String

    /**
     * Execute the command with the given stream configuration and environment.
     * @param streamConfig The configuration of streams for the command.
     * @param environment The environment for the command.
     * @return The return code of the command.
     */
    fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode
}

/**
 * Base class for built-in commands.
 */
abstract class BuiltInCommand(protected val args: List<Value>) : Command

/**
 * Command for exiting the CLI.
 */
class ExitCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = EXIT_COMMAND

    init {
        checkArgsCount(name, args, 0)
    }

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        state.isRunning = false
        return ReturnCode.SUCCESS
    }

}

/**
 * Command for printing the current working directory.
 */
class PwdCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = PWD_COMMAND

    init {
        checkArgsCount(name, args, 0)
    }

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val currentDir = state.currentDir.path
        streamConfig.stdout.writeln(currentDir)
        return ReturnCode.SUCCESS
    }
}

/**
 * Command for echoing text to the output.
 */
class EchoCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = ECHO_COMMAND

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val output = args.joinToString(" ") { it.evaluateToString(state.environment) }
        streamConfig.stdout.writeln(output)
        return ReturnCode.SUCCESS
    }
}

/**
 * Command for displaying the contents of a file.
 */
class CatCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name: String = CAT_COMMAND

    init {
        checkArgsCount(name, args, 1)
    }

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val filename = args[0].evaluateToString(state.environment)

        try {
            val file = File(filename)
            if (!file.exists()) {
                streamConfig.stderr.writeln("$name: $filename: No such file or directory")
                return ReturnCode.FAILURE
            }
            streamConfig.stdout.writeln(file.readText())
        } catch (e: Exception) {
            streamConfig.stderr.writeln("$name: $filename: ${e.message}")
            return ReturnCode.FAILURE
        }

        return ReturnCode.SUCCESS
    }
}

/**
 * Command for counting lines, words, and characters in a file.
 */
class WcCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = WC_COMMAND

    init {
        checkArgsCount(name, args, 1)
    }

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val filename = args[0].evaluateToString(state.environment)

        try {
            val file = File(filename)
            if (!file.exists()) {
                streamConfig.stderr.writeln("$name: $filename: No such file or directory")
                return ReturnCode.FAILURE
            }
            val content = file.readText()
            val (lines, words, chars) = countLinesWordsChars(content)
            streamConfig.stdout.writeln("$lines $words $chars $filename")
        } catch (e: Exception) {
            streamConfig.stderr.writeln("$name: $filename: ${e.message}")
            return ReturnCode.FAILURE
        }

        return ReturnCode.SUCCESS
    }

    private fun countLinesWordsChars(text: String): Triple<Int, Int, Int> {
        val lines = text.lines().size
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        val chars = text.length
        return Triple(lines, words, chars)
    }
}

/**
 * Command for executing an external process.
 */
class ExternalCommand(private val command: String, private val args: List<Value>) : Command {

    override val name = command

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        try {
            // NOTE: all IO operations here are blocking
            val processBuilder = ProcessBuilder(command, *args.map { it.evaluateToString(state.environment) }.toTypedArray())
            processBuilder.environment().putAll(state.environment.asMap())
            val process = processBuilder
                .directory(state.currentDir)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val bytes = streamConfig.stdin.readNBytes(streamConfig.stdin.available())
            process.outputStream.writer().use {
                it.write(bytes.toString(Charsets.UTF_8))
                it.flush()
            }

            // TODO: make timeout a parameter
            // Wait for the process to complete
            val exitCode = if (process.waitFor(10, TimeUnit.SECONDS)) {
                // getting output
                process.inputStream.bufferedReader().readText().let { output ->
                    streamConfig.stdout.write(output)
                }
                process.errorStream.bufferedReader().readText().let { errors ->
                    streamConfig.stderr.write(errors)
                }
                ReturnCode.fromCode(process.exitValue())
            } else {
                streamConfig.stderr.writeln("$name: subcommand timed out")
                ReturnCode.FAILURE
            }

            return exitCode
        } catch (e: Exception) {
            streamConfig.stderr.writeln("$name: ${e.message}")
            return ReturnCode.FAILURE
        }
    }
}