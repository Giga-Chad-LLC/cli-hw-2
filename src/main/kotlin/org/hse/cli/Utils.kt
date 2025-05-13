package org.hse.cli

import java.io.OutputStream

const val EXIT_COMMAND = "exit"
const val PWD_COMMAND = "pwd"
const val ECHO_COMMAND = "echo"
const val CAT_COMMAND = "cat"
const val WC_COMMAND = "wc"
const val GREP_COMMAND = "grep"
const val CD_COMMAND = "cd"
const val LS_COMMAND = "ls"

val newLine = System.lineSeparator()

fun OutputStream.write(str: String) = write(str.toByteArray())
fun OutputStream.writeln(str: String) = write("$str${newLine}".toByteArray())
