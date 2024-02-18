package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.YearMonthDate.CreateDate
import io.github.persiancalendar.calendar.YearMonthDate.TwelveMonthsYear.monthStartOfMonthsDistance
import io.github.persiancalendar.calendar.YearMonthDate.TwelveMonthsYear.monthsDistanceTo
import io.github.persiancalendar.calendar.islamic.FallbackIslamicConverter
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.calendar.islamic.UmmAlQuraConverter

/**
 * @author Amir
 */
class IslamicDate : AbstractDate, YearMonthDate<IslamicDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)
    constructor(jdn: Long) : super(jdn)
    constructor(date: AbstractDate) : super(date)

    override fun toJdn(): Long {
        val year = year
        val month = month
        val day = dayOfMonth
        val tableResult: Long = if (useUmmAlQura) UmmAlQuraConverter.toJdn(year, month, day)
            .toLong() else IranianIslamicDateConverter.toJdn(year, month, day)
        return if (tableResult != -1L) tableResult - islamicOffset else FallbackIslamicConverter.toJdn(
            year,
            month,
            day
        ) - islamicOffset
    }

    override fun fromJdn(jdn: Long): IntArray {
        var jdn = jdn
        jdn += islamicOffset.toLong()
        var result = if (useUmmAlQura) {
            UmmAlQuraConverter.fromJdn(jdn)
        } else {
            IranianIslamicDateConverter.fromJdn(jdn)
        }
        if (result == null) result = FallbackIslamicConverter.fromJdn(jdn)
        return result
    }

    override fun monthStartOfMonthsDistance(monthsDistance: Int): IslamicDate {
        val createDate: CreateDate<IslamicDate> = object : CreateDate<IslamicDate> {
            override fun createDate(year: Int, month: Int, dayOfMonth: Int): IslamicDate =
                IslamicDate(year, month, dayOfMonth)
        }

        return monthStartOfMonthsDistance(this, monthsDistance, createDate)
    }

    override fun monthsDistanceTo(date: IslamicDate): Int = monthsDistanceTo(this, date)

    companion object {
        // Converters
        var useUmmAlQura = false
        var islamicOffset = 0
    }
}
