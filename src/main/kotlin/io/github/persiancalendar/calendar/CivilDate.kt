package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.persian.AlgorithmicConverter
import io.github.persiancalendar.calendar.util.TwelveMonthsYear

class CivilDate : AbstractDate, YearMonthDate<CivilDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)
    constructor(date: AbstractDate) : super(date)
    constructor(jdn: Long) : super(jdn)

    // Converters
    override fun toJdn(): Long = AlgorithmicConverter.civilToJdn(year, month, dayOfMonth)

    override fun fromJdn(jdn: Long): IntArray = AlgorithmicConverter.civilFromJdn(jdn)

    override fun monthStartOfMonthsDistance(monthsDistance: Int): CivilDate =
        TwelveMonthsYear.monthStartOfMonthsDistance(this, monthsDistance, ::CivilDate)

    override fun monthsDistanceTo(date: CivilDate): Int =
        TwelveMonthsYear.monthsDistanceTo(this, date)
}
