package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CLIParserTest {
    
    private lateinit var parser: CLIParser
    
    @BeforeEach
    fun setUp() {
        parser = CLIParser()
    }
    
    @Test
    fun `parse returns empty pipeline task for blank input`() {
        val task = parser.parse("   ")
        
        assertTrue(task is PipelineTask)
        val pipelineTask = task as PipelineTask
        assertEquals(0, pipelineTask.execute(StreamConfig(System.`in`, System.out, System.err), 
            ProgramState(Environment(), java.io.File("."), true)).exitCode)
    }
    
    @Test
    fun `parse handles environment variable assignment`() {
        val task = parser.parse("VAR=value")
        
        assertTrue(task is ModifyEnvironmentTask)
        val envTask = task as ModifyEnvironmentTask
        assertEquals("VAR", envTask.key)
        assertEquals("value", envTask.value.str)
    }
    
    @Test
    fun `parse creates pipeline task for command input`() {
        val task = parser.parse("echo Hello")
        
        assertTrue(task is PipelineTask)
        val cmds = (task as PipelineTask).commands
        assertTrue(
            cmds.size == 1 &&
            cmds[0] is EchoCommand
        )
    }
    
    @Test
    fun `parse handles pipeline of commands`() {
        val task = parser.parse("echo Hello | wc file.txt | exit | pwd | cat file.txt | unknown")
        
        assertTrue(task is PipelineTask)
        val cmds = (task as PipelineTask).commands
        assertTrue(
            cmds.size == 6 &&
            cmds[0] is EchoCommand &&
            cmds[1] is WcCommand &&
            cmds[2] is ExitCommand &&
            cmds[3] is PwdCommand &&
            cmds[4] is CatCommand &&
            cmds[5] is ExternalCommand
        )
    }

    @Test
    fun `parse throws ParsingError for invalid env input`() {
        assertThrows(ParsingError::class.java) {
            parser.parse("KEY=")
        }
        assertThrows(ParsingError::class.java) {
            parser.parse("=VALUE")
        }
        assertThrows(ParsingError::class.java) {
            parser.parse("=")
        }
        assertThrows(ParsingError::class.java) {
            parser.parse("KEY=   'hi = there'")
        }
    }
    
    @Test
    fun `parse throws ParsingError for invalid pipeline input`() {
        assertThrows(ParsingError::class.java) {
            parser.parse("|")
        }
        assertThrows(ParsingError::class.java) {
            parser.parse("echo Hello | ")
        }
        assertThrows(ParsingError::class.java) {
            parser.parse("| echo Hello")
        }
    }
    
    @Test
    fun `parseCommand creates correct command for exit`() {
        val command = parser.parseCommand("exit")
        
        assertTrue(command is ExitCommand)
    }
    
    @Test
    fun `parseCommand creates correct command for pwd`() {
        val command = parser.parseCommand("pwd")
        
        assertTrue(command is PwdCommand)
    }
    
    @Test
    fun `parseCommand creates correct command for echo`() {
        val command = parser.parseCommand("echo Hello")
        
        assertTrue(command is EchoCommand)
    }
    
    @Test
    fun `parseCommand creates correct command for cat`() {
        val command = parser.parseCommand("cat file.txt")
        
        assertTrue(command is CatCommand)
    }
    
    @Test
    fun `parseCommand creates correct command for wc`() {
        val command = parser.parseCommand("wc file.txt")
        
        assertTrue(command is WcCommand)
    }
    
    @Test
    fun `parseCommand creates external command for unknown command`() {
        val command = parser.parseCommand("unknown arg1 arg2")
        
        assertTrue(command is ExternalCommand)
    }
    
    @Test
    fun `parseCommand throws ParsingError for empty command`() {
        assertThrows(ParsingError::class.java) {
            parser.parseCommand("")
        }
    }
    
    @Test
    fun `splitCommandString splits command string correctly`() {
        val parts = parser.splitCommandString("command arg1 arg2")
        
        assertEquals(3, parts.size)
        assertEquals("command", parts[0])
        assertEquals("arg1", parts[1])
        assertEquals("arg2", parts[2])
    }
    
    @Test
    fun `splitCommandString handles quoted arguments`() {
        val parts = parser.splitCommandString("command \"arg with spaces\" 'another arg'")
        
        assertEquals(3, parts.size)
        assertEquals("command", parts[0])
        assertEquals("\"arg with spaces\"", parts[1])
        assertEquals("'another arg'", parts[2])
    }
    
    @Test
    fun `splitCommandString handles empty string`() {
        val parts = parser.splitCommandString("")
        
        assertEquals(0, parts.size)
    }
}