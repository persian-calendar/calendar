package io.github.persiancalendar.calendar;

import io.github.persiancalendar.calendar.persian.AlgorithmicConverter;
import io.github.persiancalendar.calendar.persian.LookupTableConverter;

public class PersianDate extends AbstractDate implements YearMonthDate<PersianDate> {

    public PersianDate(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }

    public PersianDate(long jdn) {
        super(jdn);
    }

    public PersianDate(AbstractDate date) {
        super(date);
    }

    // Converters
    @Override
    public long toJdn() {
        long result = LookupTableConverter.toJdn(getYear(), getMonth(), getDayOfMonth());
        return result == -1 ? AlgorithmicConverter.toJdn(getYear(), getMonth(), getDayOfMonth()) : result;
    }

    @Override
    protected int[] fromJdn(long jdn) {
        int[] result = LookupTableConverter.fromJdn(jdn);
        return result == null ? AlgorithmicConverter.fromJdn(jdn) : result;
    }

    @Override
    public PersianDate monthStartOfMonthsDistance(int monthsDistance) {
        return TwelveMonthsYear.monthStartOfMonthsDistance(this, monthsDistance, PersianDate::new);
    }

    @Override
    public int monthsDistanceTo(PersianDate date) {
        return TwelveMonthsYear.monthsDistanceTo(this, date);
    }
}
