package org.hse.cli

import org.apache.commons.cli.*
import java.io.File
import java.nio.file.Paths

/**
 * Команда grep для поиска по регулярному выражению.
 *
 * Поддерживаемые опции:
 *  - -w : поиск целого слова (шаблон оборачивается в границы слова с включением Unicode‑режима)
 *  - -i : регистронезависимый поиск
 *  - -A <N> : вывод N строк контекста после совпавшей строки
 *
 * Если указан второй параметр (filename), поиск производится в файле.
 * Если filename не указан, grep читает входные данные из streamConfig.stdin.
 *
 * Синтаксис:
 *   grep [OPTIONS] <pattern> [filename]
 */
class GrepCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = "grep"

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        // Преобразуем аргументы в список строк с учетом переменных окружения
        val rawArgs = args.map { it.evaluateToString(state.environment) }

        // Определяем опции с помощью Apache Commons CLI
        val options = Options().apply {
            addOption("w", false, "Поиск только целого слова")
            addOption("i", false, "Регистронезависимый поиск")
            addOption(
                Option.builder("A")
                    .hasArg()
                    .desc("Вывод N строк контекста после строки с совпадением")
                    .build()
            )
        }

        val parser = DefaultParser()
        val cmd: CommandLine
        try {
            cmd = parser.parse(options, rawArgs.toTypedArray())
        } catch (e: ParseException) {
            streamConfig.stderr.writeln("grep: ошибка разбора аргументов: ${e.message}")
            return ReturnCode.FAILURE
        }

        // Количество оставшихся позиционных аргументов может быть 1 (pattern) или 2 (pattern и filename)
        val remainingArgs = cmd.args
        if (remainingArgs.size !in 1..2) {
            streamConfig.stderr.writeln("grep: некорректное количество параметров")
            streamConfig.stderr.writeln("Использование: grep [OPTIONS] <pattern> [filename]")
            return ReturnCode.FAILURE
        }

        val patternArg = remainingArgs[0]

        // Если filename передан, читаем данные из файла; иначе — из потока ввода (stdin)
        val inputText: String = if (remainingArgs.size == 2) {
            val filename = remainingArgs[1]
            val file = File(Paths.get(state.currentDir.absolutePath, filename).toString())
            if (!file.exists()) {
                streamConfig.stderr.writeln("grep: $filename: такой файл не существует")
                return ReturnCode.FAILURE
            }
            file.readText()
        } else {
            streamConfig.stdin.bufferedReader().readText()
        }

        // Если опция -w указана, оборачиваем шаблон в границы слова, включая Unicode‑режим
        var patternToUse = patternArg
        if (cmd.hasOption("w")) {
            patternToUse = "(?U)\\b$patternArg\\b"
        }

        val regexOptions = if (cmd.hasOption("i")) setOf(RegexOption.IGNORE_CASE) else emptySet()
        val regex = try {
            Regex(patternToUse, regexOptions)
        } catch (e: Exception) {
            streamConfig.stderr.writeln("grep: некорректное регулярное выражение: ${e.message}")
            return ReturnCode.FAILURE
        }

        // Определяем количество дополнительных строк (-A)
        val afterCount: Int = if (cmd.hasOption("A")) {
            val countStr = cmd.getOptionValue("A")
            countStr.toIntOrNull()?.takeIf { it >= 0 } ?: run {
                streamConfig.stderr.writeln("grep: неверное значение для -A: $countStr")
                return ReturnCode.FAILURE
            }
        } else {
            0
        }

        val lines = inputText.lines()

        // Находим интервалы: для каждой строки с совпадением плюс afterCount следующих строк
        val intervals = mutableListOf<Pair<Int, Int>>()
        for (i in lines.indices) {
            if (regex.containsMatchIn(lines[i])) {
                val end = minOf(i + afterCount, lines.lastIndex)
                intervals.add(Pair(i, end))
            }
        }

        // Если совпадений нет, ничего не выводим и возвращаем успех
        if (intervals.isEmpty()) {
            return ReturnCode.SUCCESS
        }

        // Объединяем пересекающиеся интервалы, чтобы исключить дублирование строк
        val mergedIntervals = mutableListOf<Pair<Int, Int>>()
        for (interval in intervals.sortedBy { it.first }) {
            if (mergedIntervals.isEmpty()) {
                mergedIntervals.add(interval)
            } else {
                val last = mergedIntervals.last()
                if (interval.first <= last.second + 1) {
                    mergedIntervals[mergedIntervals.lastIndex] = Pair(last.first, maxOf(last.second, interval.second))
                } else {
                    mergedIntervals.add(interval)
                }
            }
        }

        // Выводим найденные строки в stdout
        for ((start, end) in mergedIntervals) {
            for (i in start..end) {
                streamConfig.stdout.writeln(lines[i])
            }
        }

        return ReturnCode.SUCCESS
    }
}
