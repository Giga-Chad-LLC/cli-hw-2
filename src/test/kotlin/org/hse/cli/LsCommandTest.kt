package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

private val newLine = System.lineSeparator()

class LsCommandTest {
    private lateinit var environment: Environment
    private lateinit var programState: ProgramState
    private lateinit var stdout: ByteArrayOutputStream
    private lateinit var stderr: ByteArrayOutputStream
    private lateinit var streamConfig: StreamConfig

    @BeforeEach
    fun setUp() {
        environment = Environment()
        programState = ProgramState(environment, File("."), true)
        stdout = ByteArrayOutputStream()
        stderr = ByteArrayOutputStream()
        streamConfig = StreamConfig(ByteArrayInputStream(ByteArray(0)), stdout, stderr)
    }

    @Test
    fun `LsCommand lists files in current directory`(@TempDir tempDir: File) {
        val file1 = File(tempDir, "file1.txt")
        file1.createNewFile()
        val file2 = File(tempDir, "file2.txt")
        file2.createNewFile()

        programState.currentDir = tempDir
        val command = LsCommand(emptyList())
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        val expectedOutput1 = "${file2.name}$newLine${file1.name}$newLine"
        val expectedOutput2 = "${file1.name}$newLine${file2.name}$newLine"

        assertTrue(expectedOutput1 == stdout.toString() || expectedOutput2 == stdout.toString())
    }

    @Test
    fun `LsCommand lists files in specified directory`(@TempDir tempDir: File) {
        val subDir = File(tempDir, "subDir")
        subDir.mkdir()
        val file = File(subDir, "file.txt")
        file.createNewFile()

        val command = LsCommand(listOf(NoQuotesValue(subDir.absolutePath)))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        val expectedOutput = "${file.name}$newLine"
        assertEquals(expectedOutput, stdout.toString())
    }

    @Test
    fun `LsCommand returns failure for non-existent directory`() {
        val nonExistentDir = "non_existent_dir"
        val command = LsCommand(listOf(NoQuotesValue(nonExistentDir)))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.FAILURE, result)
        val expectedError = "ls: ./$nonExistentDir: No such file or directory$newLine"
        assertEquals(expectedError, stderr.toString())
    }
}
