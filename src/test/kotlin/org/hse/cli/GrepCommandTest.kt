package org.hse.cli

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

private val newLine = System.lineSeparator()

class GrepCommandTest {

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
    fun `GrepCommand outputs matching line without options`(@TempDir tempDir: File) {
        // Создаём временный файл с содержимым
        val fileContent = """
            Первая строка
            Минимальный синтаксис grep
            Последняя строка
        """.trimIndent()
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(fileContent)
        programState.currentDir = tempDir

        // Передаём аргументы: шаблон и имя файла
        val command = GrepCommand(listOf(NoQuotesValue("Минимальный"), NoQuotesValue("test.txt")))
        val result = command.execute(streamConfig, programState)

        assertEquals(ReturnCode.SUCCESS, result)
        val expectedOutput = "Минимальный синтаксис grep$newLine"
        assertEquals(expectedOutput, stdout.toString())
    }

    @Test
    fun `GrepCommand outputs matching lines with after context merging`(@TempDir tempDir: File) {
        // Тестируем объединение областей при использовании опции -A.
        val fileContent = """
            Строка 1
            Строка 2
            Строка 3 совпадает
            Строка 4
            Строка 5 совпадает
            Строка 6
            Строка 7
            Строка 8
        """.trimIndent()
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(fileContent)
        programState.currentDir = tempDir

        // Опция -A 2: для каждой совпадающей строки выводятся ещё 2 строки после неё.
        // При совпадении в строках 3 и 5 интервалы сливаются, и ожидается вывод строк с 3 по 7.
        val command = GrepCommand(
            listOf(
                NoQuotesValue("-A"),
                NoQuotesValue("2"),
                NoQuotesValue("совпадает"),
                NoQuotesValue("test.txt")
            )
        )
        val result = command.execute(streamConfig, programState)
        assertEquals(ReturnCode.SUCCESS, result)

        val expectedLines = listOf(
            "Строка 3 совпадает",
            "Строка 4",
            "Строка 5 совпадает",
            "Строка 6",
            "Строка 7"
        )
        val actualLines = stdout.toString().lines().filter { it.isNotBlank() }
        assertEquals(expectedLines, actualLines)
    }

    @Test
    fun `GrepCommand outputs matching lines with -i and -w options`(@TempDir tempDir: File) {
        // Тестируем комбинированное использование опций: -i (регистронезависимый) и -w (поиск целого слова)
        val fileContent = """
            Минимальный синтаксис grep
            минимальный синтаксис grep
            Минимальныйный синтаксис grep
        """.trimIndent()
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(fileContent)
        programState.currentDir = tempDir

        val command = GrepCommand(
            listOf(
                NoQuotesValue("-i"),
                NoQuotesValue("-w"),
                NoQuotesValue("Минимальный"),
                NoQuotesValue("test.txt")
            )
        )
        val result = command.execute(streamConfig, programState)
        assertEquals(ReturnCode.SUCCESS, result)

        // При использовании -w ожидается вывод только тех строк, где слово совпадает целиком.
        val expectedLines = listOf(
            "Минимальный синтаксис grep",
            "минимальный синтаксис grep"
        )
        val actualLines = stdout.toString().lines().filter { it.isNotBlank() }
        assertEquals(expectedLines, actualLines)
    }

    @Test
    fun `GrepCommand returns failure for non-existent file`() {
        // Если файл не существует, команда должна вернуть FAILURE и вывести сообщение об ошибке.
        val command = GrepCommand(listOf(NoQuotesValue("pattern"), NoQuotesValue("non_existent_file.txt")))
        val result = command.execute(streamConfig, programState)
        assertEquals(ReturnCode.FAILURE, result)
        val expectedError = "grep: non_existent_file.txt: такой файл не существует$newLine"
        assertEquals(expectedError, stderr.toString())
    }

    @Test
    fun `GrepCommand outputs nothing if no line matches`(@TempDir tempDir: File) {
        // Если ни одна строка не соответствует шаблону, вывод должен быть пустым.
        val fileContent = """
            Строка 1
            Строка 2
            Строка 3
        """.trimIndent()
        val testFile = File(tempDir, "test.txt")
        testFile.writeText(fileContent)
        programState.currentDir = tempDir

        val command = GrepCommand(listOf(NoQuotesValue("несовпадает"), NoQuotesValue("test.txt")))
        val result = command.execute(streamConfig, programState)
        assertEquals(ReturnCode.SUCCESS, result)
        assertEquals("", stdout.toString())
    }
}
