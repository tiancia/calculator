package com.local.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CalculatorTest {
    private fun value(source: String, expected: String) {
        assertEquals(Calculator.Result.Value(expected), Calculator.evaluate(source))
    }

    @Test fun precedenceAndDecimalArithmetic() {
        value("12+3×4", "24")
        value("20÷5×2", "8")
        value("0.1+0.2", "0.3")
        value("-2×-3", "6")
        value("10--2", "12")
        value("12.", "12")
    }

    @Test fun percentagesAndRounding() {
        value("50%", "0.5")
        value("200×10%", "20")
        value("200+10%", "200.1")
        value("1÷3", "0.3333333333333333")
    }

    @Test fun incompleteAndInvalidInputsNeverReturnPreviousAnswer() {
        for (source in listOf("", "-", "12+", "2×-")) {
            assertEquals(Calculator.Result.Incomplete, Calculator.evaluate(source))
        }
        assertEquals(Calculator.Result.DivisionByZero, Calculator.evaluate("1÷0"))
        for (source in listOf("1..2", "2%%", "2a3")) {
            assertEquals(Calculator.Result.Invalid, Calculator.evaluate(source))
        }
    }

    @Test fun keyboardEditsAndEquals() {
        val input = CalculatorInput()
        for (key in listOf("1", "2", "+", "3", "×", "4")) input.press(key)
        value(input.expression, "24")
        input.press("⌫")
        assertEquals(Calculator.Result.Incomplete, Calculator.evaluate(input.expression))
        input.press("2")
        input.press("=")
        assertEquals("18", input.expression)
        input.press("+")
        input.press("2")
        input.press("=")
        assertEquals("20", input.expression)
        input.press("7")
        assertEquals("7", input.expression)
        input.press("AC")
        assertEquals("", input.expression)
    }

    @Test fun signsOperatorReplacementAndDecimalGuard() {
        val input = CalculatorInput("12+3")
        input.press("±")
        value(input.expression, "9")
        input.press("±")
        value(input.expression, "15")
        input.press("×")
        input.press("÷")
        input.press("-")
        input.press("2")
        value(input.expression, "10.5")
        input.press("AC")
        input.press(".")
        input.press(".")
        input.press("5")
        assertEquals("0.5", input.expression)
    }

    @Test fun restoringConfirmedStateAndLengthLimit() {
        val restored = CalculatorInput("24", true)
        restored.press("2")
        assertEquals("2", restored.expression)
        val longInput = CalculatorInput("1".repeat(Calculator.MAX_LENGTH))
        assertFalse(longInput.press("2"))
        assertEquals(Calculator.MAX_LENGTH, longInput.expression.length)
    }
}
