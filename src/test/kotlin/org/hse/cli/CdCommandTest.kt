package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

private val newLine = System.lineSeparator()

class CdCommandTest {
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
    fun `CdCommand changes directory to home`() {
        val command = CdCommand(emptyList())
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        assertTrue(programState.currentDir.absolutePath.contains(System.getProperty("user.home")))
    }

    @Test
    fun `CdCommand changes directory to specified path`(@TempDir tempDir: File) {
        val newDir = File(tempDir, "newDir")
        newDir.mkdir()

        val command = CdCommand(listOf(NoQuotesValue(newDir.absolutePath)))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals(newDir.absolutePath, programState.currentDir.absolutePath)
    }

    @Test
    fun `CdCommand returns failure for non-existent directory`() {
        val nonExistentDir = "non_existent_dir"
        val command = CdCommand(listOf(NoQuotesValue(nonExistentDir)))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.FAILURE, result)
        val expectedError = "cd: ./$nonExistentDir: No such file or directory$newLine"
        assertEquals(expectedError, stderr.toString())
    }
}