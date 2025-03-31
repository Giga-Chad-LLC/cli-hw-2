package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths

class CommandTest {
    
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
    fun `ExitCommand sets isRunning to false`() {
        val command = ExitCommand(emptyList())
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertFalse(programState.isRunning)
    }
    
    @Test
    fun `ExitCommand throws exception with arguments`() {
        assertThrows(IllegalCommandArgumentsCount::class.java) {
            ExitCommand(listOf(NoQuotesValue("arg")))
        }
    }
    
    @Test
    fun `PwdCommand outputs current directory`() {
        val command = PwdCommand(emptyList())
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals(programState.currentDir.path + newLine, stdout.toString())
    }
    
    @Test
    fun `PwdCommand throws exception with arguments`() {
        assertThrows(IllegalCommandArgumentsCount::class.java) {
            PwdCommand(listOf(NoQuotesValue("arg")))
        }
    }
    
    @Test
    fun `EchoCommand outputs arguments`() {
        val command = EchoCommand(listOf(NoQuotesValue("Hello"), NoQuotesValue("World")))
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("Hello World$newLine", stdout.toString())
    }
    
    @Test
    fun `EchoCommand evaluates environment variables`() {
        environment.set("USER", "John")
        val command = EchoCommand(listOf(NoQuotesValue("Hello"), NoQuotesValue("\$USER")))
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("Hello John$newLine", stdout.toString())
    }
    
    @Test
    fun `CatCommand outputs file contents`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Test content")
        
        val command = CatCommand(listOf(NoQuotesValue("test.txt")))

        programState.currentDir = tempDir
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("Test content$newLine", stdout.toString())
    }
    
    @Test
    fun `CatCommand returns failure for non-existent file`() {
        val command = CatCommand(listOf(NoQuotesValue("non_existent_file.txt")))
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.FAILURE, result)
        assertEquals("cat: non_existent_file.txt: No such file or directory$newLine", stderr.toString())
    }
    
    @Test
    fun `CatCommand throws exception without arguments`() {
        assertThrows(IllegalCommandArgumentsCount::class.java) {
            CatCommand(emptyList())
        }
    }
    
    @Test
    fun `WcCommand counts lines, words, and characters`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Line 1\nLine 2\nLine 3")
        
        val command = WcCommand(listOf(NoQuotesValue(testFile.absolutePath)))
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        // 3 lines, 6 words, 20 characters
        assertTrue(stdout.toString().startsWith("3 6 20"))
    }
    
    @Test
    fun `WcCommand returns failure for non-existent file`() {
        val command = WcCommand(listOf(NoQuotesValue("non_existent_file.txt")))
        
        val result = command.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.FAILURE, result)
        assertTrue(stderr.toString() == "wc: non_existent_file.txt: No such file or directory$newLine")
    }
    
    @Test
    fun `WcCommand throws exception without arguments`() {
        assertThrows(IllegalCommandArgumentsCount::class.java) {
            WcCommand(emptyList())
        }
    }

    @Test
    fun `ExternalCommand executes and returns`(@TempDir tempDir: File) {
        val testFile = File(tempDir, "test.txt")
        testFile.writeText("Test content${newLine}New line$newLine")

        // here we use real UNIX `wc` command
        val command = ExternalCommand("wc", listOf(NoQuotesValue(testFile.absolutePath)))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        assertTrue(stdout.toString().contains("2       4      22"))
    }

    @Test
    fun `ExternalCommand fails if does not exist`() {
        val command = ExternalCommand("unknown", listOf())
        val result = command.execute(streamConfig, programState)

        val errorOutput = stderr.toString()

        assertEquals(ReturnCode.FAILURE, result)
        assertTrue(errorOutput.contains("unknown: Cannot run program \"unknown\""))
        assertTrue(errorOutput.contains("No such file or directory"))
    }
    
    @Test
    fun `countLinesWordsChars counts correctly`() {
        val command = WcCommand(listOf(NoQuotesValue("dummy")))
        val text = "Line 1\nLine 2\nLine 3"
        
        val (lines, words, chars) = command.countLinesWordsChars(text)
        
        assertEquals(3, lines)
        assertEquals(6, words)
        assertEquals(20, chars)
    }
    
    @Test
    fun `countLinesWordsChars handles empty string`() {
        val command = WcCommand(listOf(NoQuotesValue("dummy")))
        val text = ""
        
        val (lines, words, chars) = command.countLinesWordsChars(text)
        
        assertEquals(1, lines)  // Empty string has 1 line
        assertEquals(0, words)
        assertEquals(0, chars)
    }
}