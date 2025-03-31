package org.hse.cli

import java.io.*

/**
 * Class for interpreting CLI input.
 */
class CLIInterpreter(
    private val streamConfig: StreamConfig,
    environment: Environment,
    currentDirPath: String = System.getProperty("user.dir")
) {
    private val parser = CLIParser()
    private val state = ProgramState(
        environment,
        File(currentDirPath),
        true
    )

    init {
        println("Current directory: ${state.currentDir}")
    }

    /**
     * Run the interpreter.
     */
    fun run() {
        val reader = BufferedReader(InputStreamReader(streamConfig.stdin))
        val writer = OutputStreamWriter(streamConfig.stdout)
        val errorWriter = OutputStreamWriter(streamConfig.stderr)

        while (state.isRunning) {
            try {
                // Print prompt
                writer.write("> ")
                writer.flush()

                // Read input
                val input = reader.readLine() ?: break

                // Parse input
                val task = parser.parse(input)

                // Execute task
                val result = task.execute(streamConfig, state)
                if (result != ReturnCode.SUCCESS) {
                    writer.write("Command failed: exit code ${result.exitCode}$newLine")
                }
                writer.flush()
            }
            catch (e: Exception) {
                e.message?.let {
                    errorWriter.write(it)
                    errorWriter.flush()
                }
                e.printStackTrace(System.err)
                System.err.flush()
            }
        }
    }
}
