package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class StreamConfigTest {
    
    @Test
    fun `StreamConfig stores streams correctly`() {
        val stdin = ByteArrayInputStream(ByteArray(0))
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        
        val streamConfig = StreamConfig(stdin, stdout, stderr)
        
        assertSame(stdin, streamConfig.stdin)
        assertSame(stdout, streamConfig.stdout)
        assertSame(stderr, streamConfig.stderr)
    }
    
    @Test
    fun `StreamConfig can have same stream for stdout and stderr`() {
        val stdin = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        
        val streamConfig = StreamConfig(stdin, output, output)
        
        assertSame(stdin, streamConfig.stdin)
        assertSame(output, streamConfig.stdout)
        assertSame(output, streamConfig.stderr)
        assertSame(streamConfig.stdout, streamConfig.stderr)
    }
}