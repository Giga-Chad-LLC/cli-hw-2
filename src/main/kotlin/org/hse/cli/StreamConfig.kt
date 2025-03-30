package org.hse.cli

import java.io.InputStream
import java.io.OutputStream

/**
 * Class representing the configuration of streams for commands.
 */
class StreamConfig(
    val stdin: InputStream,
    val stdout: OutputStream,
    val stderr: OutputStream
)