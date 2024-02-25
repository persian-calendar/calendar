package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.util.TwelveMonthsYear

/**
 * @author Amir
 */
class CivilDate : AbstractDate, YearMonthDate<CivilDate> {
    constructor(year: Int, month: Int, dayOfMonth: Int) : super(year, month, dayOfMonth)

    constructor(jdn: Long) : super(jdn)

    constructor(date: AbstractDate) : super(date)

    // Converters
    override fun toJdn(): Long {
        val lYear = year.toLong()
        val lMonth = month.toLong()
        val lDay = dayOfMonth.toLong()

        return if (
            (lYear > 1582)
            || ((lYear == 1582L) && (lMonth > 10))
            || ((lYear == 1582L) && (lMonth == 10L) && (lDay > 14))
        ) {
            (((1461 * (lYear + 4800 + ((lMonth - 14) / 12))) / 4)
                    + ((367 * (lMonth - 2 - 12 * (((lMonth - 14) / 12)))) / 12)
                    - ((3 * (((lYear + 4900 + ((lMonth - 14) / 12)) / 100))) / 4)
                    + lDay - 32075)
        } else julianToJdn(lYear, lMonth, lDay)
    }

    override fun fromJdn(jdn: Long): IntArray {
        return if (jdn > 2299160) {
            var l = jdn + 68569
            val n = ((4 * l) / 146097)
            l -= ((146097 * n + 3) / 4)
            val i = ((4000 * (l + 1)) / 1461001)
            l = l - ((1461 * i) / 4) + 31
            val j = ((80 * l) / 2447)
            val day = (l - ((2447 * j) / 80)).toInt()
            l = (j / 11)
            val month = (j + 2 - 12 * l).toInt()
            val year = (100 * (n - 49) + i + l).toInt()
            intArrayOf(year, month, day)
        } else julianFromJdn(jdn)
    }

    override fun monthStartOfMonthsDistance(monthsDistance: Int): CivilDate =
        TwelveMonthsYear.monthStartOfMonthsDistance(this, monthsDistance, ::CivilDate)

    override fun monthsDistanceTo(date: CivilDate): Int =
        TwelveMonthsYear.monthsDistanceTo(this, date)

    companion object {
        // TODO Is it correct to return a CivilDate as a JulianDate?
        private fun julianFromJdn(jdn: Long): IntArray {
            var j = jdn + 1402
            val k = ((j - 1) / 1461)
            val l = j - 1461 * k
            val n = ((l - 1) / 365) - (l / 1461)
            var i = l - 365 * n + 30
            j = ((80 * i) / 2447)
            val day = (i - ((2447 * j) / 80)).toInt()
            i = (j / 11)
            val month = (j + 2 - 12 * i).toInt()
            val year = (4 * k + n + i - 4716).toInt()

            return intArrayOf(year, month, day)
        }

        private fun julianToJdn(lYear: Long, lMonth: Long, lDay: Long): Long {
            return (367 * lYear) -
                    ((7 * (lYear + 5001 + ((lMonth - 9) / 7))) / 4) +
                    ((275 * lMonth) / 9) +
                    lDay + 1729777
        }
    }
}
