package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class ValueTest {

    private lateinit var environment: Environment

    @BeforeEach
    fun setUp() {
        environment = Environment()
    }

    @Test
    fun `DoubleQuotesValue preserves literal value`() {
        val value = ValueFactory.constructFromString("\"Hello World\"")
        val result = value.evaluateToString(environment)

        assertTrue(value is DoubleQuotesValue)
        assertEquals("Hello World", result)
    }

    @Test
    fun `DoubleQuotesValue escapes special characters`() {
        val value = ValueFactory.constructFromString("\"Line1\nLine2\tTabbed\rReturn\"")
        val result = value.evaluateToString(environment)

        assertTrue(value is DoubleQuotesValue)
        assertEquals("Line1\\nLine2\\tTabbed\\rReturn", result)
    }

    @Test
    fun `DoubleQuotesValue does not replace environment variables`() {
        val value = ValueFactory.constructFromString("\"Hello \$USER\"")
        environment.set("USER", "John")
        val result = value.evaluateToString(environment)

        assertTrue(value is DoubleQuotesValue)
        assertEquals("Hello \$USER", result)
    }

    @Test
    fun `SingleQuotesValue preserves literal value`() {
        val value = ValueFactory.constructFromString("'Hello World'")
        val result = value.evaluateToString(environment)

        assertTrue(value is SingleQuotesValue)
        assertEquals("Hello World", result)
    }

    @Test
    fun `SingleQuotesValue preserves special characters`() {
        val value = ValueFactory.constructFromString("'Line1\nLine2\tTabbed\rReturn'")
        val result = value.evaluateToString(environment)

        assertTrue(value is SingleQuotesValue)
        assertEquals("Line1\nLine2\tTabbed\rReturn", result)
    }

    @Test
    fun `SingleQuotesValue replaces environment variables`() {
        val value = ValueFactory.constructFromString("'Hello \$USER'")
        environment.set("USER", "John")
        val result = value.evaluateToString(environment)

        assertTrue(value is SingleQuotesValue)
        assertEquals("Hello John", result)
    }

    @Test
    fun `NoQuotesValue preserves literal value`() {
        val value = ValueFactory.constructFromString("Hello World")
        val result = value.evaluateToString(environment)

        assertTrue(value is NoQuotesValue)
        assertEquals("Hello World", result)
    }

    @Test
    fun `NoQuotesValue preserves special characters`() {
        val value = ValueFactory.constructFromString("Line1\nLine2\tTabbed\rReturn")
        val result = value.evaluateToString(environment)

        assertTrue(value is NoQuotesValue)
        assertEquals("Line1\nLine2\tTabbed\rReturn", result)
    }

    @Test
    fun `NoQuotesValue replaces environment variables`() {
        val value = ValueFactory.constructFromString("Hello \$USER")
        environment.set("USER", "John")
        val result = value.evaluateToString(environment)

        assertTrue(value is NoQuotesValue)
        assertEquals("Hello John", result)
    }

    @Test
    fun `ValueFactory constructs DoubleQuotesValue for double-quoted string`() {
        val value = ValueFactory.constructFromString("\"Hello World\"")

        assertTrue(value is DoubleQuotesValue)
    }

    @Test
    fun `ValueFactory constructs SingleQuotesValue for single-quoted string`() {
        val value = ValueFactory.constructFromString("'Hello World'")

        assertTrue(value is SingleQuotesValue)
    }

    @Test
    fun `ValueFactory constructs NoQuotesValue for unquoted string`() {
        val value = ValueFactory.constructFromString("Hello World")

        assertTrue(value is NoQuotesValue)
    }

    @Test
    fun `ValueFactory handles empty strings`() {
        val value = ValueFactory.constructFromString("")

        assertTrue(value is NoQuotesValue)
        assertEquals("", value.evaluateToString(Environment()))
    }

    @Test
    fun `ValueFactory throws when given strings with only one quote`() {
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("\"Hello")
        }
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("Hello\"")
        }
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("'Hello")
        }
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("Hello'")
        }
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("He'llo")
        }
        assertThrows(ValueConstructionError::class.java) {
            ValueFactory.constructFromString("He\"llo'")
        }
    }
}
