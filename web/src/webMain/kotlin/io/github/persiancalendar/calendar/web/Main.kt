@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.persiancalendar.calendar.web

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import kotlin.js.ExperimentalWasmJsInterop

// A day count crosses as a Double rather than a Long: a Wasm build hands a Long to
// JavaScript as a BigInt and a JS build hands over a number, and the page should not have to
// know which bundle it is running against. A Julian day number is around 2 500 000, far
// inside what a Double holds exactly.
external fun calendarReady(
    jdnOf: (calendar: String, year: Int, month: Int, day: Int) -> Double,
    // An array cannot cross into a Wasm build either, so the date travels as "year|month|day".
    dateOf: (calendar: String, jdn: Double) -> String,
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
        jdnOf = { calendar, year, month, day -> jdnOf(calendar, year, month, day).toDouble() },
        dateOf = { calendar, jdn ->
            val date = when (calendar) {
                "civil" -> CivilDate(jdn.toLong())
                "persian" -> PersianDate(jdn.toLong())
                "islamic" -> IslamicDate(jdn.toLong())
                else -> error("Unknown calendar: $calendar")
            }
            "${date.year}|${date.month}|${date.dayOfMonth}"
        },
        daysInMonth = { calendar: String, year: Int, month: Int ->
            val current = jdnOf(calendar, year, month, 1)
            val nextYear = if (month == 12) year + 1 else year
            val nextMonth = if (month == 12) 1 else month + 1
            (jdnOf(calendar, nextYear, nextMonth, 1) - current).toInt()
        },
    )
}
