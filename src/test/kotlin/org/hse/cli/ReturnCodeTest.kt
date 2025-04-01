package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ReturnCodeTest {
    
    @Test
    fun `SUCCESS has exit code 0`() {
        assertEquals(0, ReturnCode.SUCCESS.exitCode)
    }
    
    @Test
    fun `FAILURE has exit code 1`() {
        assertEquals(1, ReturnCode.FAILURE.exitCode)
    }
    
    @Test
    fun `OTHER has correct exit code`() {
        val exitCode = 42
        val returnCode = ReturnCode.OTHER(exitCode)
        
        assertEquals(exitCode, returnCode.exitCode)
    }
    
    @Test
    fun `fromCode returns SUCCESS for code 0`() {
        val returnCode = ReturnCode.fromCode(0)
        
        assertEquals(ReturnCode.SUCCESS, returnCode)
    }
    
    @Test
    fun `fromCode returns FAILURE for code 1`() {
        val returnCode = ReturnCode.fromCode(1)
        
        assertEquals(ReturnCode.FAILURE, returnCode)
    }
    
    @Test
    fun `fromCode returns OTHER for other codes`() {
        val exitCode = 42
        val returnCode = ReturnCode.fromCode(exitCode)
        
        assertTrue(returnCode is ReturnCode.OTHER)
        assertEquals(exitCode, returnCode.exitCode)
    }
}