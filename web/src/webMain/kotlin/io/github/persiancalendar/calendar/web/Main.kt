@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.persiancalendar.calendar.web

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import kotlin.js.ExperimentalWasmJsInterop

external fun calendarReady(
    jdnOf: (calendar: String, year: Int, month: Int, day: Int) -> Long,
    dateOf: (calendar: String, jdn: Long) -> Array<Int>,
    daysInMonth: (calendar: String, year: Int, month: Int) -> Int,
)

fun jdnOf(calendar: String, year: Int, month: Int, day: Int): Long {
    return when (calendar) {
        "civil" -> CivilDate(year, month, day)
        "persian" -> PersianDate(year, month, day)
        "islamic" -> IslamicDate(year, month, day)
        else -> error("Unknown calendar: $calendar")
    }.toJdn()
}

fun main() {
    calendarReady(
        jdnOf = ::jdnOf,
        dateOf = { calendar, jdn ->
            val date = when (calendar) {
                "civil" -> CivilDate(jdn.toLong())
                "persian" -> PersianDate(jdn.toLong())
                "islamic" -> IslamicDate(jdn.toLong())
                else -> error("Unknown calendar: $calendar")
            }
            arrayOf(date.year, date.month, date.dayOfMonth)
        },
        daysInMonth = { calendar: String, year: Int, month: Int ->
            val current = jdnOf(calendar, year, month, 1)
            val nextYear = if (month == 12) year + 1 else year
            val nextMonth = if (month == 12) 1 else month + 1
            (jdnOf(calendar, nextYear, nextMonth, 1) - current).toInt()
        },
    )
}
