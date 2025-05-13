package org.hse.cli

import java.io.File

class CdCommand(args: List<Value>) : BuiltInCommand(args) {
    override val name = "cd"

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        val targetDir: File = when {
            args.isEmpty() -> File(System.getProperty("user.home"))
            else -> {
                val pathArg = args[0].evaluateToString(state.environment)
                val file = File(pathArg)
                if (file.isAbsolute) file else File(state.currentDir, pathArg)
            }
        }

        if (!targetDir.exists()) {
            streamConfig.stderr.writeln("cd: ${targetDir.path}: No such file or directory")
            return ReturnCode.FAILURE
        }

        if (!targetDir.isDirectory) {
            streamConfig.stderr.writeln("cd: ${targetDir.path}: Not a directory")
            return ReturnCode.FAILURE
        }

        state.currentDir = targetDir.absoluteFile
        return ReturnCode.SUCCESS
    }
}
