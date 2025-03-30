package org.hse.cli

import org.hse.org.hse.cli.meta.ReturnCode
import org.hse.org.hse.cli.Environment
import org.hse.org.hse.cli.ProgramState
import org.hse.org.hse.cli.utils.StreamConfig
import java.io.*

/**
 * Interface for tasks that can be executed by the CLI interpreter.
 */
interface Task {
    /**
     * Execute the task with the given stream configuration and environment.
     * @param streamConfig The configuration of streams for the task.
     * @param environment The environment for the task.
     * @return The return code of the task.
     */
    fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode
}

/**
 * Task for modifying the environment.
 */
class ModifyEnvironmentTask(private val key: String, private val value: Value) : Task {

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        state.environment.set(key, value.evaluateToString(state.environment))
        return ReturnCode.SUCCESS
    }
}

/**
 * Task for executing a pipeline of commands.
 */
class PipelineTask(private val commands: List<Command>) : Task {

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        if (commands.isEmpty()) {
            return ReturnCode.SUCCESS
        }

        // Execute each command in the pipeline
        val results = Array<ReturnCode>(commands.size) { ReturnCode.SUCCESS }
        var out = ByteArrayOutputStream()

        for (i in commands.indices) {
            val stdin = if (i == 0) streamConfig.stdin else {
                val prevBytes = out.toByteArray()
                out = ByteArrayOutputStream()
                ByteArrayInputStream(prevBytes)
            }
            val stdout = if (i == commands.size - 1) streamConfig.stdout else out
            val config = StreamConfig(stdin, stdout, streamConfig.stderr)
            results[i] = commands[i].execute(config, state)
        }

        // Return the result of the last command
        return results.last()
    }
}
