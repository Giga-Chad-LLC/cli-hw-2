package org.hse.cli

import java.io.File

class LsCommand(args: List<Value>) : BuiltInCommand(args) {
    override val name = "ls"

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val targetDir: File = when {
            args.isEmpty() -> state.currentDir
            else -> {
                val pathArg = args[0].evaluateToString(state.environment)
                val file = File(pathArg)
                if (file.isAbsolute) file else File(state.currentDir, pathArg)
            }
        }

        if (!targetDir.exists()) {
            streamConfig.stderr.writeln("ls: ${targetDir.path}: No such file or directory")
            return ReturnCode.FAILURE
        }

        if (!targetDir.isDirectory) {
            streamConfig.stderr.writeln("ls: ${targetDir.path}: Not a directory")
            return ReturnCode.FAILURE
        }

        val files = targetDir.list()
        files?.forEach { streamConfig.stdout.writeln(it) }
        return ReturnCode.SUCCESS
    }
}
