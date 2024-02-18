package io.github.persiancalendar.calendar;

interface YearMonthDate<T extends AbstractDate> {
    // Ideally getYear()/getMonth()/getDay() also should be moved to this interface

    T monthStartOfMonthsDistance(int monthsDistance);

    int monthsDistanceTo(T date);

    interface CreateDate<T extends AbstractDate> {
        T createDate(int year, int month, int dayOfMonth);
    }

    class TwelveMonthsYear {
        static <T extends AbstractDate> T monthStartOfMonthsDistance(
                T baseDate, int monthsDistance, CreateDate<T> createDate
        ) {
            int month = monthsDistance + baseDate.getMonth() - 1; // make it zero based for easier calculations
            int year = baseDate.getYear() + month / 12;
            month %= 12;
            if (month < 0) {
                year -= 1;
                month += 12;
            }
            return createDate.createDate(year, month + 1, 1);
        }

        static <T extends AbstractDate> int monthsDistanceTo(T baseDate, T toDate) {
            return (toDate.getYear() - baseDate.getYear()) * 12 + toDate.getMonth() - baseDate.getMonth();
        }
    }
}
