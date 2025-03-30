package org.hse.org.hse.cli.utils

import java.io.OutputStream

internal const val EXIT_COMMAND = "exit"
internal const val PWD_COMMAND = "pwd"
internal const val ECHO_COMMAND = "echo"
internal const val CAT_COMMAND = "cat"
internal const val WC_COMMAND = "wc"

internal val newLine = System.lineSeparator()

internal fun OutputStream.write(str: String) = write(str.toByteArray())
internal fun OutputStream.writeln(str: String) = write("$str$newLine".toByteArray())