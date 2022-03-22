package io.github.persiancalendar.calendar

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import kotlin.test.assertEquals

class CalendarCenterConversions {
    // https://calendar.ut.ac.ir/Fa/Software/CalConv.asp
    @ParameterizedTest
    @CsvFileSource(resources = ["/CalendarCenterData.csv"], numLinesToSkip = 1)
    fun `Matches with Calendar Center converter Hijri months`(
        persianDay: Int, persianMonth: Int, persianYear: Int,
        islamicDay: Int, islamicMonth: Int, islamicYear: Int,
        civilDay: Int, civilMonth: Int, civilYear: Int
    ) {
        assertEquals(
            1, setOf(
                PersianDate(persianYear, persianMonth, persianDay).toJdn(),
                IslamicDate(islamicYear, islamicMonth, islamicDay).toJdn(),
                CivilDate(civilYear, civilMonth, civilDay).toJdn()
            ).size
        )
    }
}