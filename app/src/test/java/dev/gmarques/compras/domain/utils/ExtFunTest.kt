package dev.gmarques.compras.domain.utils

import dev.gmarques.compras.domain.utils.ExtFun.Companion.currencyToDouble
import dev.gmarques.compras.domain.utils.ExtFun.Companion.onlyIntegerNumbers
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeAccents
import dev.gmarques.compras.domain.utils.ExtFun.Companion.removeSpaces
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtFunTest {

    @Test
    fun testCurrencyToDouble() {
        val testCases = arrayOf(
            "$1,234.56" to 1234.56,
            "$ 1234.56" to 1234.56,
            "€1.234,56" to 1234.56,
            "1  2 34.5  6" to 1234.56,
            "jtz12çl34" to 1234.0,
            "" to 0.0,
            " $1,234.56 " to 1234.56,
            " $1,23456 " to 1.23,
            ".$1,23456 " to 1.23,
            ".$1,23456." to 123456.0,
            "R$1.234,56" to 1234.56,
            "R1234.56" to 1234.56
        )

        testCases.forEach { (input, expected) ->
            val result = input.currencyToDouble()
            assertEquals(expected, result, 0.01)
        }
    }

    @Test
    fun testOnlyIntegerNumbers() {
        val input = "abc123def456"
        val expected = 123456
        val result = input.onlyIntegerNumbers()
        assertEquals(expected, result)
    }

    @Test
    fun testRemoveAccents() {
        val input = "àéîõüç"
        val expected = "aeiouc"
        val result = input.removeAccents()
        assertEquals(expected, result)
    }

    @Test
    fun testRemoveSpaces() {
        val testCases = arrayOf(
            "  multiple   spaces  " to "multiple spaces",
            " leading space" to "leading space",
            "trailing space " to "trailing space",
            "  " to "",
            "singleword" to "singleword",
            "word    with    multiple    spaces" to "word with multiple spaces",
            "   " to "",
            "\t\ttabs\tand\nnewlines" to "tabs and newlines",
            "  mix of \n tabs and spaces " to "mix of tabs and spaces",
            "normal sentence with one space" to "normal sentence with one space"
        )

        testCases.forEach { (input, expected) ->
            val result = input.removeSpaces()
            assertEquals(expected, result)
        }
    }

}
