package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class TaskTest {
    
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
    fun `ModifyEnvironmentTask sets environment variable`() {
        val key = "TEST_VAR"
        val value = NoQuotesValue("test_value")
        val task = ModifyEnvironmentTask(key, value)
        
        val result = task.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("test_value", environment.get(key))
    }
    
    @Test
    fun `ModifyEnvironmentTask evaluates value before setting`() {
        val key = "TEST_VAR"
        environment.set("OTHER_VAR", "other_value")
        val value = NoQuotesValue("\$OTHER_VAR")
        val task = ModifyEnvironmentTask(key, value)
        
        val result = task.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("other_value", environment.get(key))
    }
    
    @Test
    fun `PipelineTask with no commands returns success`() {
        val task = PipelineTask(emptyList())
        
        val result = task.execute(streamConfig, programState)
        
        assertEquals(ReturnCode.SUCCESS, result)
    }
}