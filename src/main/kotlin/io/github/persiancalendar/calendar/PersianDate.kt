package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.persian.AlgorithmicConverter
import io.github.persiancalendar.calendar.persian.LookupTableConverter
import io.github.persiancalendar.calendar.persian.OldEraConverter
import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthStartOfMonthsDistance
import io.github.persiancalendar.calendar.util.TwelveMonthsYear.monthsDistanceTo

class PersianDate : AbstractDate, YearMonthDate<PersianDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)
    constructor(date: AbstractDate) : super(date)
    constructor(jdn: Long) : super(jdn)

    // Converters
    override fun toJdn(): Long {
        var result = OldEraConverter.toJdn(year, month, dayOfMonth)
        if (result == -1L) result = LookupTableConverter.toJdn(year, month, dayOfMonth)
        if (result == -1L) result = AlgorithmicConverter.toJdn(year, month, dayOfMonth)
        return result
    }

    override fun fromJdn(jdn: Long): IntArray =
        OldEraConverter.fromJdn(jdn) ?: LookupTableConverter.fromJdn(jdn)
        ?: AlgorithmicConverter.fromJdn(jdn)

    override fun monthStartOfMonthsDistance(monthsDistance: Int): PersianDate =
        monthStartOfMonthsDistance(this, monthsDistance, ::PersianDate)

    override fun monthsDistanceTo(date: PersianDate): Int = monthsDistanceTo(this, date)

    companion object {
        // First six months have length of 31, next 5 months are 30 and the last month is 29 and in leap years are 30
        private val daysToMonth =
            intArrayOf(0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366)

        internal fun monthFromDaysCount(days: Int): Int = daysToMonth.indexOfFirst { it >= days }
        internal fun daysInPreviousMonths(month: Int): Int = daysToMonth[month - 1]
    }
}
