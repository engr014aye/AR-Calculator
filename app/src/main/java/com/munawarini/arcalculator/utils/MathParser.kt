package com.munawarini.arcalculator.utils

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function as Exp4jFunction
import kotlin.math.*

/**
 * A robust math expression parser built on exp4j.
 *
 * Supported syntax:
 *   - Arithmetic: +  -  *  /  ^ (power)
 *   - Constants: π  e
 *   - Display symbols auto-converted: × → *  ÷ → /  √ → sqrt(
 *   - Trig (degree mode): sin(x) cos(x) tan(x)
 *   - Inverse trig (returns degrees): asin(x) acos(x) atan(x)
 *   - Logarithms: log(x) = log₁₀,  ln(x) = natural log
 *   - Square root: sqrt(x)
 *   - Absolute value: abs(x)
 *   - Factorial: fact(x)  (integer only, max 20)
 */
object MathParser {

    sealed class ParseResult {
        data class Success(val value: Double) : ParseResult()
        data class Error(val message: String) : ParseResult()
    }

    // ── Custom degree-mode trig functions ───────────────────────────────────

    private val sinDeg = object : Exp4jFunction("sin", 1) {
        override fun apply(vararg args: Double) = sin(Math.toRadians(args[0]))
    }
    private val cosDeg = object : Exp4jFunction("cos", 1) {
        override fun apply(vararg args: Double) = cos(Math.toRadians(args[0]))
    }
    private val tanDeg = object : Exp4jFunction("tan", 1) {
        override fun apply(vararg args: Double): Double {
            val deg = args[0] % 360.0
            if (deg == 90.0 || deg == 270.0) return Double.NaN
            return tan(Math.toRadians(args[0]))
        }
    }
    private val asinDeg = object : Exp4jFunction("asin", 1) {
        override fun apply(vararg args: Double) = Math.toDegrees(asin(args[0]))
    }
    private val acosDeg = object : Exp4jFunction("acos", 1) {
        override fun apply(vararg args: Double) = Math.toDegrees(acos(args[0]))
    }
    private val atanDeg = object : Exp4jFunction("atan", 1) {
        override fun apply(vararg args: Double) = Math.toDegrees(atan(args[0]))
    }

    // ── Log / ln ────────────────────────────────────────────────────────────

    private val log10Func = object : Exp4jFunction("log", 1) {
        override fun apply(vararg args: Double) = log10(args[0])
    }
    private val lnFunc = object : Exp4jFunction("ln", 1) {
        override fun apply(vararg args: Double) = ln(args[0])
    }

    // ── Misc ────────────────────────────────────────────────────────────────

    private val absFn = object : Exp4jFunction("abs", 1) {
        override fun apply(vararg args: Double) = abs(args[0])
    }
    private val factFn = object : Exp4jFunction("fact", 1) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toInt()
            require(n in 0..20) { "Factorial out of range" }
            return (1..n).fold(1L) { acc, i -> acc * i }.toDouble()
        }
    }
    private val sqrtFn = object : Exp4jFunction("sqrt", 1) {
        override fun apply(vararg args: Double) = sqrt(args[0])
    }

    // ── Custom functions list ────────────────────────────────────────────────

    private val customFunctions = arrayOf(
        sinDeg, cosDeg, tanDeg,
        asinDeg, acosDeg, atanDeg,
        log10Func, lnFunc,
        absFn, factFn, sqrtFn
    )

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Evaluates [expression] and returns [ParseResult.Success] or [ParseResult.Error].
     * Never throws — all exceptions are caught and wrapped.
     */
    fun evaluate(expression: String): ParseResult {
        if (expression.isBlank()) return ParseResult.Error("")

        return try {
            val sanitized = sanitize(expression)
            if (sanitized.isBlank()) return ParseResult.Error("Invalid")

            val expr = ExpressionBuilder(sanitized)
                .functions(*customFunctions)
                .variables("π", "e")
                .build()
                .also { built ->
                    built.setVariable("π", Math.PI)
                    built.setVariable("e", Math.E)
                }

            val result = expr.evaluate()

            when {
                result.isNaN() -> ParseResult.Error("Math Error")
                result.isInfinite() -> ParseResult.Error("Undefined")
                else -> ParseResult.Success(result)
            }
        } catch (e: ArithmeticException) {
            ParseResult.Error("Math Error")
        } catch (e: IllegalArgumentException) {
            ParseResult.Error("Invalid expression")
        } catch (e: Exception) {
            ParseResult.Error("Error")
        }
    }

    /**
     * Formats a [Double] result for display.
     * - Integers are shown without decimal point (e.g. 4.0 → "4")
     * - Decimals are shown with up to 10 significant digits
     */
    fun formatResult(value: Double): String {
        if (value == kotlin.math.floor(value) && !value.isInfinite()) {
            return value.toLong().toString()
        }
        val formatted = "%.10g".format(value)
        return formatted.trimEnd('0').trimEnd('.')
    }

    /**
     * Attempt a partial evaluation of an incomplete expression for live preview.
     * Returns null if the expression is not yet evaluable.
     */
    fun liveEvaluate(expression: String): String? {
        if (expression.length < 2) return null
        return when (val result = evaluate(expression)) {
            is ParseResult.Success -> formatResult(result.value)
            is ParseResult.Error -> null
        }
    }

    // ── Internal sanitizer ───────────────────────────────────────────────────

    private fun sanitize(raw: String): String {
        return raw
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√(", "sqrt(")
            .replace("√", "sqrt(")
            // Replace display π with variable name for exp4j
            .replace("pi", "π")
            // Handle implicit multiplication: 2π → 2*π, 2( → 2*(
            .replace(Regex("(\\d)(π)"), "$1*π")
            .replace(Regex("(\\d)(e)(?![a-zA-Z])"), "$1*e")
            .replace(Regex("(\\d)\\("), "$1*(")
            .replace(Regex("\\)(\\d)"), ")*$1")
            .replace(Regex("\\)\\("), ")*(")
            // Remove trailing operators before evaluation
            .trimEnd('+', '-', '*', '/', '^', '.')
    }
}
