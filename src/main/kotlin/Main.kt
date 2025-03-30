package org.hse

import org.hse.cli.CLIInterpreter
import org.hse.cli.Environment
import org.hse.cli.StreamConfig

fun main() {
    // Create environment from system environment
    val env = Environment()
    System.getenv().forEach { (key, value) ->
        env.set(key, value)
    }

    // Create stream config with standard streams
    val streamConfig = StreamConfig(System.`in`, System.out, System.out)

    // Create and run interpreter
    val interpreter = CLIInterpreter(streamConfig, env)
    interpreter.run()
}