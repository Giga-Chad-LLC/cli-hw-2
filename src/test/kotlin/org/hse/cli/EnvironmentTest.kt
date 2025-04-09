package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class EnvironmentTest {
    
    private lateinit var environment: Environment
    
    @BeforeEach
    fun setUp() {
        environment = Environment()
    }
    
    @Test
    fun `get returns empty string for non-existent key`() {
        val result = environment.get("NON_EXISTENT_KEY")
        assertEquals("", result)
    }
    
    @Test
    fun `get returns value for existing key`() {
        val key = "TEST_KEY"
        val value = "test_value"
        environment.set(key, value)
        
        val result = environment.get(key)
        assertEquals(value, result)
    }
    
    @Test
    fun `set updates value for existing key`() {
        val key = "TEST_KEY"
        val initialValue = "initial_value"
        val updatedValue = "updated_value"
        
        environment.set(key, initialValue)
        assertEquals(initialValue, environment.get(key))
        
        environment.set(key, updatedValue)
        assertEquals(updatedValue, environment.get(key))
    }
    
    @Test
    fun `asMap returns copy of environment`() {
        val key1 = "KEY1"
        val value1 = "value1"
        val key2 = "KEY2"
        val value2 = "value2"
        
        environment.set(key1, value1)
        environment.set(key2, value2)
        
        val map = environment.asMap()
        
        assertEquals(2, map.size)
        assertEquals(value1, map[key1])
        assertEquals(value2, map[key2])
        
        // Verify it's a copy by modifying the original environment
        environment.set(key1, "modified")
        assertEquals(value1, map[key1]) // Map should still have the old value
    }
    
    @Test
    fun `constructor with map initializes environment`() {
        val initialMap = mutableMapOf(
            "KEY1" to "value1",
            "KEY2" to "value2"
        )
        
        val env = Environment(initialMap)
        
        assertEquals("value1", env.get("KEY1"))
        assertEquals("value2", env.get("KEY2"))
    }
}