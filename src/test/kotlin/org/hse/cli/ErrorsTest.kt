package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ErrorsTest {
    
    @Test
    fun `ParsingError has correct message`() {
        val errorMessage = "Invalid syntax"
        val error = ParsingError(errorMessage)
        
        assertEquals(errorMessage, error.message)
    }
    
    @Test
    fun `IllegalCommandArgumentsCount has correct message with no args`() {
        val commandName = "test"
        val args = emptyList<Value>()
        val expectedCount = 1
        val error = IllegalCommandArgumentsCount(commandName, args, expectedCount)
        
        val expectedMessage = "Illegal arguments for command 'test': expected count 1, but got 0$newLine"
        assertEquals(expectedMessage, error.message)
    }
    
    @Test
    fun `IllegalCommandArgumentsCount has correct message with args`() {
        val commandName = "test"
        val args = listOf<Value>(NoQuotesValue("arg1"), NoQuotesValue("arg2"))
        val expectedCount = 1
        val error = IllegalCommandArgumentsCount(commandName, args, expectedCount)
        
        val expectedMessage = "Illegal arguments for command 'test': expected count 1, but got 2: arg1, arg2$newLine"
        assertEquals(expectedMessage, error.message)
    }
    
    @Test
    fun `checkArgsCount does not throw when count matches`() {
        val commandName = "test"
        val args = listOf<Value>(NoQuotesValue("arg1"))
        val expectedCount = 1
        
        // This should not throw an exception
        checkArgsCount(commandName, args, expectedCount)
    }
    
    @Test
    fun `checkArgsCount throws when count does not match`() {
        val commandName = "test"
        val args = listOf<Value>(NoQuotesValue("arg1"), NoQuotesValue("arg2"))
        val expectedCount = 1
        
        val exception = assertThrows(IllegalCommandArgumentsCount::class.java) {
            checkArgsCount(commandName, args, expectedCount)
        }
        
        val expectedMessage = "Illegal arguments for command 'test': expected count 1, but got 2: arg1, arg2$newLine"
        assertEquals(expectedMessage, exception.message)
    }
    
    @Test
    fun `checkArgsCount throws when args is empty but expected count is not`() {
        val commandName = "test"
        val args = emptyList<Value>()
        val expectedCount = 1
        
        val exception = assertThrows(IllegalCommandArgumentsCount::class.java) {
            checkArgsCount(commandName, args, expectedCount)
        }
        
        val expectedMessage = "Illegal arguments for command 'test': expected count 1, but got 0$newLine"
        assertEquals(expectedMessage, exception.message)
    }
}