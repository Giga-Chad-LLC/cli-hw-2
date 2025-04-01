package org.hse.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class CLIInterpreterTest {
    
    private lateinit var environment: Environment
    private lateinit var stdin: ByteArrayInputStream
    private lateinit var stdout: ByteArrayOutputStream
    private lateinit var stderr: ByteArrayOutputStream
    private lateinit var streamConfig: StreamConfig
    
    @BeforeEach
    fun setUp() {
        environment = Environment()
        stdin = ByteArrayInputStream(ByteArray(0))
        stdout = ByteArrayOutputStream()
        stderr = ByteArrayOutputStream()
        streamConfig = StreamConfig(stdin, stdout, stderr)
    }
    
    @Test
    fun `run executes commands until exit`() {
        // Instead, we'll test the behavior by providing input that includes an exit command
        val input = """
            echo Hello World
            exit
        """.trimIndent()
        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)
        
        val interpreter = CLIInterpreter(streamConfig, environment)
        
        interpreter.run()
        
        val output = stdout.toString()
        assertEquals(
            """
                > Hello World
                > 
            """.trimIndent(),
            output
        )
    }
    
    @Test
    fun `run handles exceptions`() {
        // Test that the interpreter handles exceptions by providing input that will cause an exception
        val input = """
            cat non_existent_file.txt
            exit
        """.trimIndent()
        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)
        
        val interpreter = CLIInterpreter(streamConfig, environment)
        
        // This will run until the exit command is executed
        interpreter.run()
        
        val errorOutput = stderr.toString()
        assertEquals("cat: non_existent_file.txt: No such file or directory$newLine", errorOutput)
    }
    
    @Test
    fun `run handles empty input`() {
        val input = """
            exit
        """.trimIndent()
        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)
        
        val interpreter = CLIInterpreter(streamConfig, environment)
        
        // This will run until the exit command is executed
        interpreter.run()
        
        val output = stdout.toString()
        assertEquals("> ", output)
    }
    
    @Test
    fun `run handles end of input`() {
        val input = ""
        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)
        
        val interpreter = CLIInterpreter(streamConfig, environment)
        
        // This will run until EOF is reached
        interpreter.run()
        
        val output = stdout.toString()
        assertEquals("> ", output)
    }


    @Test
    fun `run pipes from built-in to external command`() {
        val input = """
            cat test.txt | grep H
        """.trimIndent()

        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)

        val interpreter = CLIInterpreter(streamConfig, environment, TEST_RESOURCES_DIR.absolutePath)
        interpreter.run()

        assertEquals("""
            > Hello World
            Hi there, how are you?
            > 
        """.trimIndent(), stdout.toString())
        assertTrue(stderr.toString().isEmpty())
    }

    @Test
    fun `run pipes from external to external command`() {
        val input = """
            ls | grep test
        """.trimIndent()

        stdin = ByteArrayInputStream(input.toByteArray())
        streamConfig = StreamConfig(stdin, stdout, stderr)

        val interpreter = CLIInterpreter(streamConfig, environment, TEST_RESOURCES_DIR.absolutePath)
        interpreter.run()

        assertEquals("""
            > test.txt
            > 
        """.trimIndent(), stdout.toString())
        assertTrue(stderr.toString().isEmpty())
    }
}