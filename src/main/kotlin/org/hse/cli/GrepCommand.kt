package org.hse.cli

import org.apache.commons.cli.*
import java.io.File
import java.nio.file.Paths

/**
 * Команда grep для поиска по регулярному выражению в файле.
 *
 * Поддерживаемые опции:
 *  - -w : искать только целое слово; шаблон оборачивается в \b (с включением Unicode‑режима)
 *  - -i : регистронезависимый поиск
 *  - -A <N> : для каждой строки с совпадением дополнительно выводить N строк ниже
 *
 * При пересечении интервалов вывода (контекст после совпадения) они объединяются.
 *
 * Синтаксис: grep [OPTIONS] <pattern> <filename>
 */
class GrepCommand(args: List<Value>) : BuiltInCommand(args) {

    override val name = "grep"

    override fun execute(streamConfig: StreamConfig, state: ProgramState): ReturnCode {
        // Преобразуем аргументы команды в список строк
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
            // Разбор аргументов; остаются позиционные: pattern и filename
            cmd = parser.parse(options, rawArgs.toTypedArray())
        } catch (e: ParseException) {
            streamConfig.stderr.writeln("grep: ошибка разбора аргументов: ${e.message}")
            return ReturnCode.FAILURE
        }

        // Оставшиеся аргументы должны быть ровно 2: шаблон и имя файла
        val remainingArgs = cmd.args
        if (remainingArgs.size != 2) {
            streamConfig.stderr.writeln("grep: некорректное количество параметров")
            streamConfig.stderr.writeln("Использование: grep [OPTIONS] <pattern> <filename>")
            return ReturnCode.FAILURE
        }

        val patternArg = remainingArgs[0]
        val filename = remainingArgs[1]

        // Если задана опция -w, оборачиваем шаблон с включением Unicode‑режима,
        // чтобы \b корректно обрабатывал кириллические буквы
        var patternToUse = patternArg
        if (cmd.hasOption("w")) {
            patternToUse = "(?U)\\b$patternArg\\b"
        }

        // Флаг для игнорирования регистра
        val regexOptions = if (cmd.hasOption("i")) {
            setOf(RegexOption.IGNORE_CASE)
        } else {
            emptySet()
        }

        val regex = try {
            Regex(patternToUse, regexOptions)
        } catch (e: Exception) {
            streamConfig.stderr.writeln("grep: некорректное регулярное выражение: ${e.message}")
            return ReturnCode.FAILURE
        }

        // Определяем количество строк для контекста (-A)
        val afterCount: Int = if (cmd.hasOption("A")) {
            val countStr = cmd.getOptionValue("A")
            countStr.toIntOrNull()?.takeIf { it >= 0 } ?: run {
                streamConfig.stderr.writeln("grep: неверное значение для -A: $countStr")
                return ReturnCode.FAILURE
            }
        } else {
            0
        }

        // Определяем файл относительно текущей директории
        val file = File(Paths.get(state.currentDir.absolutePath, filename).toString())
        if (!file.exists()) {
            streamConfig.stderr.writeln("grep: $filename: такой файл не существует")
            return ReturnCode.FAILURE
        }

        val lines = file.readText().lines()

        // Находим интервалы для вывода: для каждой строки с совпадением выводим строку + afterCount следующих строк
        val intervals = mutableListOf<Pair<Int, Int>>()
        for (i in lines.indices) {
            if (regex.containsMatchIn(lines[i])) {
                val end = minOf(i + afterCount, lines.lastIndex)
                intervals.add(Pair(i, end))
            }
        }

        // Если совпадений нет – завершаем выполнение
        if (intervals.isEmpty()) {
            return ReturnCode.SUCCESS
        }

        // Объединяем пересекающиеся интервалы для исключения дублирования строк
        val mergedIntervals = mutableListOf<Pair<Int, Int>>()
        for (interval in intervals.sortedBy { it.first }) {
            if (mergedIntervals.isEmpty()) {
                mergedIntervals.add(interval)
            } else {
                val last = mergedIntervals.last()
                if (interval.first <= last.second + 1) {
                    mergedIntervals[mergedIntervals.lastIndex] =
                        Pair(last.first, maxOf(last.second, interval.second))
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
