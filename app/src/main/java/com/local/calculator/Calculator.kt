package com.local.calculator

import java.math.BigDecimal
import java.math.MathContext

/** Small decimal parser: addition/subtraction, multiplication/division, unary minus, percent. */
object Calculator {
    const val MAX_LENGTH = 120
    private val precision = MathContext.DECIMAL64

    sealed interface Result {
        data class Value(val text: String) : Result
        data object Incomplete : Result
        data object DivisionByZero : Result
        data object Invalid : Result
    }

    fun evaluate(expression: String): Result {
        if (expression.isEmpty()) return Result.Incomplete
        if (expression.length > MAX_LENGTH) return Result.Invalid
        return try {
            val parser = Parser(expression)
            val value = parser.sum()
            if (!parser.atEnd()) return Result.Invalid
            val normalized = value.stripTrailingZeros()
            Result.Value(if (normalized.signum() == 0) "0" else normalized.toPlainString())
        } catch (_: Unfinished) {
            Result.Incomplete
        } catch (_: ZeroDivisor) {
            Result.DivisionByZero
        } catch (_: IllegalArgumentException) {
            Result.Invalid
        } catch (_: ArithmeticException) {
            Result.Invalid
        }
    }

    private class Unfinished : RuntimeException()
    private class ZeroDivisor : RuntimeException()

    private class Parser(private val source: String) {
        private var index = 0
        fun atEnd() = index == source.length
        private fun peek(): Char? = source.getOrNull(index)

        fun sum(): BigDecimal {
            var value = product()
            while (peek() == '+' || peek() == '-') {
                val operator = source[index++]
                val next = product()
                value = if (operator == '+') value.add(next, precision) else value.subtract(next, precision)
            }
            return value
        }

        private fun product(): BigDecimal {
            var value = number()
            while (peek() == '×' || peek() == '÷') {
                val operator = source[index++]
                val next = number()
                value = if (operator == '×') {
                    value.multiply(next, precision)
                } else {
                    if (next.signum() == 0) throw ZeroDivisor()
                    value.divide(next, precision)
                }
            }
            return value
        }

        private fun number(): BigDecimal {
            if (atEnd()) throw Unfinished()
            val negative = peek() == '-'
            if (negative) index++
            if (atEnd()) throw Unfinished()
            val start = index
            var digits = 0
            var dots = 0
            while (peek()?.let { it in '0'..'9' || it == '.' } == true) {
                if (source[index++] == '.') dots++ else digits++
            }
            require(digits > 0 && dots <= 1)
            // A trailing decimal point is a valid number while typing, e.g. 12. -> 12.
            var value = BigDecimal(source.substring(start, index))
            if (negative) value = value.negate()
            if (peek() == '%') {
                index++
                value = value.movePointLeft(2)
            }
            return value
        }
    }
}

/** Keyboard state is independent of the Activity, including rotation and equals behavior. */
class CalculatorInput(expression: String = "", confirmed: Boolean = false) {
    var expression = expression
        private set
    var confirmed = confirmed
        private set

    fun press(key: String): Boolean {
        val old = expression
        when (key) {
            "AC" -> expression = ""
            "⌫" -> expression = expression.dropLast(1)
            "=" -> {
                val result = Calculator.evaluate(expression)
                if (result is Calculator.Result.Value) {
                    if (result.text.length > Calculator.MAX_LENGTH) return false
                    expression = result.text
                    confirmed = true
                }
                return true
            }
            "±" -> toggleSign()
            "%" -> if (expression.lastOrNull()?.let { it.isDigit() || it == '.' } == true) expression += "%"
            "+", "-", "×", "÷" -> appendOperator(key)
            "." -> {
                if (confirmed) expression = ""
                if (!expression.endsWith('%')) {
                    val operand = expression.takeLastWhile { it.isDigit() || it == '.' }
                    if ('.' !in operand) expression += if (operand.isEmpty()) "0." else "."
                }
            }
            else -> if (key.length == 1 && key[0] in '0'..'9') {
                if (confirmed) expression = ""
                if (!expression.endsWith('%')) expression += key
            }
        }
        confirmed = false
        if (expression.length > Calculator.MAX_LENGTH) {
            expression = old
            return false
        }
        return true
    }

    private fun appendOperator(key: String) {
        if (expression.isEmpty()) {
            if (key == "-") expression = "-"
            return
        }
        val last = expression.last()
        if (last.isDigit() || last == '.' || last == '%') {
            expression += key
        } else if (key == "-" && (last == '×' || last == '÷')) {
            expression += key
        } else {
            // Replace unfinished operator(s), but never create a leading +, × or ÷.
            val base = expression.trimEnd('+', '-', '×', '÷')
            expression = if (base.isEmpty()) "-" else base + key
        }
    }

    private fun toggleSign() {
        var start = expression.length
        if (start > 0 && expression[start - 1] == '%') start--
        while (start > 0 && (expression[start - 1].isDigit() || expression[start - 1] == '.')) start--
        val hasUnaryMinus = start > 0 && expression[start - 1] == '-' &&
            (start == 1 || expression[start - 2] in "+-×÷")
        expression = if (hasUnaryMinus) {
            expression.removeRange(start - 1, start)
        } else {
            expression.substring(0, start) + "-" + expression.substring(start)
        }
    }
}
