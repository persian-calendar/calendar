package io.github.persiancalendar.calendar

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarCenterConversions {
    // https://calendar.ut.ac.ir/Fa/Software/CalConv.asp
    @Test
    fun `Matches with Calendar Center converter Hijri months`() {
        TestResources.calendarCenterData
            .split("\n")
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val fields = line.split(",").map { it.trim().toInt() }
                val (persianDay, persianMonth, persianYear) = fields
                val (islamicDay, islamicMonth, islamicYear) = fields.drop(3)
                val (civilDay, civilMonth, civilYear) = fields.drop(6)

                assertEquals(
                    1, setOf(
                        PersianDate(persianYear, persianMonth, persianDay).toJdn(),
                        IslamicDate(islamicYear, islamicMonth, islamicDay).toJdn(),
                        CivilDate(civilYear, civilMonth, civilDay).toJdn()
                    ).size
                )
            }
    }
}
