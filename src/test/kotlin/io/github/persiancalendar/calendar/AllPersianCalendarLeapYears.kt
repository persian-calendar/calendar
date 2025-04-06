package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.persian.AlgorithmicConverter
import kotlin.test.Test
import kotlin.test.assertEquals

class AllPersianCalendarLeapYears {

    @Test
    fun `Conforms with calendrical leap years`() {
        val leapYears = AllPersianCalendarLeapYears::class.java
            .getResourceAsStream("/leaps.txt")
            ?.readBytes()!!
            .decodeToString()
            .split("\n")
            .filter { !it.startsWith("#") }
            .map { it.toInt() }
            .toSet()
        (1..10_000).forEach {
            assertEquals(
                it in leapYears,
                AlgorithmicConverter.persianLeapYear(it, 52.5),
                it.toString()
            )
        }
    }
}
