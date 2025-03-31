package org.hse.cli


// TODO: in fabric add proper errors handling

/**
 * Abstract class representing a value in the CLI.
 */
abstract class Value {
    /**
     * Evaluate the value to a string using the given environment.
     * @param environment The environment to use for evaluation.
     * @return The evaluated string.
     */
    abstract fun evaluateToString(environment: Environment): String

    /**
     * Value as a pure string, represents a parsed result.
     */
    abstract val str: String
}

/**
 * Value with double quotes.
 * Double quotes preserve the literal value of all characters except for backslash and dollar sign.
 */
class DoubleQuotesValue(override val str: String) : Value() {

    private val processedStr: String = str
        .replace("\\", "\\\\")   // Escape backslashes
        .replace("\n", "\\n")    // Escape newline
        .replace("\t", "\\t")    // Escape tab
        .replace("\r", "\\r")    // Escape carriage return

    override fun evaluateToString(environment: Environment): String = processedStr
}

/**
 * Abstract class for values that need evaluation.
 */
abstract class EvalValue(override val str: String) : Value() {

    override fun evaluateToString(environment: Environment): String {
        // Replace environment variables
        return str.replace(Regex("\\$(\\w+)")) { matchResult ->
            val varName = matchResult.groupValues[1]
            environment.get(varName)
        }
    }
}

/**
 * Value with single quotes.
 * Single quotes preserve the literal value of all characters.
 */
class SingleQuotesValue(str: String) : EvalValue(str)

/**
 * Value without quotes.
 * No quotes allow for variable substitution.
 */
class NoQuotesValue(str: String) : EvalValue(str)

/**
 * Utility class for constructing values from strings.
 */
object ValueFactory {
    /**
     * Construct a value from a string.
     * @param str The string to construct the value from.
     * @return The constructed value.
     */
    fun constructFromString(str: String): Value {
        return when {
            str.startsWith("\"") && str.endsWith("\"") -> {
                DoubleQuotesValue(str.substring(1, str.length - 1))
            }
            str.startsWith("'") && str.endsWith("'") -> {
                SingleQuotesValue(str.substring(1, str.length - 1))
            }
            else -> {
                if (str.contains("\"") || str.contains("'")) {
                    throw ValueConstructionError("Value construction error: '$str' must either be surrounded with '/\" or contain no such symbols")
                }
                NoQuotesValue(str)
            }
        }
    }
}
