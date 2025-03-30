package org.hse.org.hse.cli.meta

/**
 * Sealed class representing return codes for commands.
 */
sealed class ReturnCode(val exitCode: Int) {
    /**
     * Represents a successful operation with code 0.
     */
    data object SUCCESS : ReturnCode(0)

    /**
     * Represents a failed operation with code 1.
     */
    data object FAILURE : ReturnCode(1)

    /**
     * Represents any other return code.
     */
    class OTHER(exitCode: Int) : ReturnCode(exitCode)

    companion object {
        fun fromCode(exitCode: Int): ReturnCode = when (exitCode) {
            0 -> SUCCESS
            1 -> FAILURE
            else -> OTHER(exitCode)
        }
    }
}