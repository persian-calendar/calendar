package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.YearMonthDate.CreateDate
import io.github.persiancalendar.calendar.YearMonthDate.TwelveMonthsYear.monthStartOfMonthsDistance
import io.github.persiancalendar.calendar.YearMonthDate.TwelveMonthsYear.monthsDistanceTo
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

    override fun fromJdn(jdn: Long): IntArray {
        val result = LookupTableConverter.fromJdn(jdn)
        return result ?: AlgorithmicConverter.fromJdn(jdn)
    }

    override fun monthStartOfMonthsDistance(monthsDistance: Int): PersianDate {
        val createDate: CreateDate<PersianDate> = object : CreateDate<PersianDate> {
            override fun createDate(year: Int, month: Int, dayOfMonth: Int): PersianDate {
                return PersianDate(year, month, dayOfMonth)
            }
        }

        return monthStartOfMonthsDistance(this, monthsDistance, createDate)
    }

    override fun monthsDistanceTo(date: PersianDate): Int {
        return monthsDistanceTo(this, date)
    }
}
