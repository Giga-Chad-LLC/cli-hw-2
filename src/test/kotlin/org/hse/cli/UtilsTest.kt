package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayOutputStream

class UtilsTest {

    @Test
    fun `newLine matches system line separator`() {
        assertEquals(System.lineSeparator(), newLine)
    }
    
    @Test
    fun `OutputStream write extension writes bytes to stream`() {
        val outputStream = ByteArrayOutputStream()
        val testString = "Hello, World!"
        
        outputStream.write(testString)
        
        val result = outputStream.toString()
        assertEquals(testString, result)
    }
    
    @Test
    fun `OutputStream writeln extension writes bytes with newline to stream`() {
        val outputStream = ByteArrayOutputStream()
        val testString = "Hello, World!"
        val expectedString = testString + newLine
        
        outputStream.writeln(testString)
        
        val result = outputStream.toString()
        assertEquals(expectedString, result)
    }
    
    @Test
    fun `OutputStream write extension handles empty string`() {
        val outputStream = ByteArrayOutputStream()
        val testString = ""
        
        outputStream.write(testString)
        
        val result = outputStream.toString()
        assertEquals(testString, result)
    }
    
    @Test
    fun `OutputStream writeln extension handles empty string`() {
        val outputStream = ByteArrayOutputStream()
        val testString = ""
        val expectedString = testString + newLine
        
        outputStream.writeln(testString)
        
        val result = outputStream.toString()
        assertEquals(expectedString, result)
    }
}