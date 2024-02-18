package io.github.persiancalendar.calendar

internal interface YearMonthDate<T : AbstractDate> {
    // Ideally getYear()/getMonth()/getDay() also should be moved to this interface
    fun monthStartOfMonthsDistance(monthsDistance: Int): T

    fun monthsDistanceTo(date: T): Int

    interface CreateDate<T : AbstractDate> {
        fun createDate(year: Int, month: Int, dayOfMonth: Int): T
    }

    object TwelveMonthsYear {
        fun <T : AbstractDate> monthStartOfMonthsDistance(
            baseDate: T, monthsDistance: Int, createDate: CreateDate<T>
        ): T {
            var month =
                monthsDistance + baseDate.month - 1 // make it zero based for easier calculations
            var year = baseDate.year + (month / 12)
            month %= 12
            if (month < 0) {
                year -= 1
                month += 12
            }
            return createDate.createDate(year, month + 1, 1)
        }

        fun <T : AbstractDate> monthsDistanceTo(baseDate: T, toDate: T): Int =
            ((toDate.year - baseDate.year) * 12) + toDate.month - baseDate.month
    }
}
