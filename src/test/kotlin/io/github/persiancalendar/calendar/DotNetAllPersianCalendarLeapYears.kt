package io.github.persiancalendar.calendar

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AllPersianCalendarLeapYears {

//    // https://github.com/dotnet/runtime/blob/57bfe474/src/libraries/System.Globalization.Calendars/tests/System/Globalization/PersianCalendarTests.cs#L246
//    private val leapYears = AllPersianCalendarLeapYears::class.java
//        .getResourceAsStream("/DotNetPersianCalendarLeapYearReference.txt")
//        ?.readBytes()!!
//        .decodeToString()
//        .replace("\n", " ")
//        .split(", ")
//        .map { it.toInt() }
//
//    @Test
//    fun `Conforms with dotnet Persian calendar leap years`() = (1..9377).forEach {
//        val yearLength = PersianDate(it + 1, 1, 1).toJdn() - PersianDate(it, 1, 1).toJdn()
//        assertEquals(if (it in leapYears) 366 else 365, yearLength, it.toString())
//    }
}
