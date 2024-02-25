package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthStartOfMonthsDistance
import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthsDistanceTo
import io.github.persiancalendar.calendar.persian.AlgorithmicConverter
import io.github.persiancalendar.calendar.persian.LookupTableConverter

class PersianDate : AbstractDate, YearMonthDate<PersianDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)
    constructor(jdn: Long) : super(jdn)
    constructor(date: AbstractDate) : super(date)

    // Converters
    override fun toJdn(): Long {
        val result = LookupTableConverter.toJdn(year, month, dayOfMonth)
        return if (result == -1L) AlgorithmicConverter.toJdn(year, month, dayOfMonth) else result
    }

    override fun fromJdn(jdn: Long): IntArray =
        LookupTableConverter.fromJdn(jdn) ?: AlgorithmicConverter.fromJdn(jdn)

    override fun monthStartOfMonthsDistance(monthsDistance: Int): PersianDate =
        monthStartOfMonthsDistance(this, monthsDistance, ::PersianDate)

    override fun monthsDistanceTo(date: PersianDate): Int = monthsDistanceTo(this, date)
}
