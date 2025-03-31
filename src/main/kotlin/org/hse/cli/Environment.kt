package org.hse.cli

import java.io.File

data class ProgramState(
    val environment: Environment,
    var currentDir: File,
    var isRunning: Boolean
)


/**
 * Class representing the environment with key-value mappings.
 */
class Environment(private val env: MutableMap<String, String> = mutableMapOf()) {
    /**
     * Get the value for the given key.
     */
    fun get(key: String): String = env[key] ?: ""

    /**
     * Set the value for the given key.
     */
    fun set(key: String, value: String) {
        env[key] = value
    }

    /**
     * Get a copy of the environment as a map.
     */
    fun asMap(): Map<String, String> = env.toMap()
}