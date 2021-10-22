// Ported from
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/PersianCalendar.cs
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/CalendricalCalculationsHelper.cs#L8
// Which is released under The MIT License (MIT)
package io.github.persiancalendar.calendar.persian;

import io.github.persiancalendar.calendar.CivilDate;

public class AlgorithmicConverter {

    private static final long projectJdnOffset = 1_721_426; // Offset from Jdn to jdn used in this converter
    private static final long persianEpoch = 226895; // new DateTime(622, 3, 22).Ticks / TicksPerDay
    private static final double meanTropicalYearInDays = 365.242189;
    private static final double fullCircleOfArc = 360.0; // 360.0;
    private static final double meanSpeedOfSun = meanTropicalYearInDays / fullCircleOfArc;
    private static final int halfCircleOfArc = 180;
    private static final double twoDegreesAfterSpring = 2.0;
    private static final int[] daysToMonth = {0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366};
    private static final double noon2000Jan01 = 730120.5;
    private static final int daysInUniformLengthCentury = 36525;
    private static final long startOf1810 = new CivilDate(1810, 1, 1).toJdn() - projectJdnOffset;
    private static final long startOf1900Century = new CivilDate(1900, 1, 1).toJdn() - projectJdnOffset;
    private static final double twelveHours = 0.5; // half a day
    private static final int secondsPerDay = 24 * 60 * 60; // 24 hours * 60 minutes * 60 seconds
    private static final int secondsPerMinute = 60;
    private static final int minutesPerDegree = 60;
    private static final double[] coefficients1900to1987 = new double[]{-0.00002, 0.000297, 0.025184, -0.181133, 0.553040, -0.861938, 0.677066, -0.212591};
    private static final double[] coefficients1800to1899 = new double[]{-0.000009, 0.003844, 0.083563, 0.865736, 4.867575, 15.845535, 31.332267, 38.291999, 28.316289, 11.636204, 2.043794};
    private static final double[] coefficients1700to1799 = new double[]{8.118780842, -0.005092142, 0.003336121, -0.0000266484};
    private static final double[] coefficients1620to1699 = new double[]{196.58333, -4.0675, 0.0219167};
    private static final double[] lambdaCoefficients = new double[]{280.46645, 36000.76983, 0.0003032};
    private static final double[] anomalyCoefficients = new double[]{357.52910, 35999.05030, -0.0001559, -0.00000048};
    private static final double[] eccentricityCoefficients = new double[]{0.016708617, -0.000042037, -0.0000001236};
    private static final double[] coefficients = new double[]{angle(23, 26, 21.448), angle(0, 0, -46.8150), angle(0, 0, -0.00059), angle(0, 0, 0.001813)};
    private static final double[] coefficientsA = new double[]{124.90, -1934.134, 0.002063};
    private static final double[] coefficientsB = new double[]{201.11, 72001.5377, 0.00057};
    private static final EphemerisCorrectionAlgorithmMap[] ephemerisCorrectionTable = new EphemerisCorrectionAlgorithmMap[]{
            // lowest year that starts algorithm, algorithm to use
            new EphemerisCorrectionAlgorithmMap(2020, CorrectionAlgorithm.Default),
            new EphemerisCorrectionAlgorithmMap(1988, CorrectionAlgorithm.Year1988to2019),
            new EphemerisCorrectionAlgorithmMap(1900, CorrectionAlgorithm.Year1900to1987),
            new EphemerisCorrectionAlgorithmMap(1800, CorrectionAlgorithm.Year1800to1899),
            new EphemerisCorrectionAlgorithmMap(1700, CorrectionAlgorithm.Year1700to1799),
            new EphemerisCorrectionAlgorithmMap(1620, CorrectionAlgorithm.Year1620to1699),
            new EphemerisCorrectionAlgorithmMap(Integer.MIN_VALUE, CorrectionAlgorithm.Default) // default must be last
    };
    private static final double longitudeSpring = 0.0;

    public static long toJdn(int year, int month, int day) {
        final int approximateHalfYear = 180;
        int ordinalDay = daysInPreviousMonths(month) + day - 1; // day is one based, make 0 based since this will be the number of days we add to beginning of year below
        int approximateDaysFromEpochForYearStart = (int) (meanTropicalYearInDays * (year - 1));
        long yearStart = persianNewYearOnOrBefore(persianEpoch + approximateDaysFromEpochForYearStart + approximateHalfYear);
        yearStart += ordinalDay;
        return yearStart + projectJdnOffset;
    }

    public static int[] fromJdn(long jdn) {
        jdn++; // TODO: Investigate why this is needed
        long yearStart = persianNewYearOnOrBefore(jdn - projectJdnOffset);
        int y = (int) (Math.floor(((yearStart - persianEpoch) / meanTropicalYearInDays) + 0.5)) + 1;

        int ordinalDay = (int) (jdn - toJdn(y, 1, 1));
        int m = monthFromOrdinalDay(ordinalDay);
        int d = ordinalDay - daysInPreviousMonths(m);
        return new int[]{y, m, d};
    }

    private static int daysInPreviousMonths(int month) {
        return daysToMonth[month - 1];
    }

    private static double asSeason(double longitude) {
        return longitude < 0 ? longitude + fullCircleOfArc : longitude;
    }

    private static double initLongitude(double longitude) {
        return normalizeLongitude(longitude + halfCircleOfArc) - halfCircleOfArc;
    }

    private static double reminder(double divisor, double dividend) {
        double whole = Math.floor(divisor / dividend);
        return divisor - dividend * whole;
    }

    private static double normalizeLongitude(double longitude) {
        longitude = reminder(longitude, fullCircleOfArc);
        if (longitude < 0) longitude += fullCircleOfArc;
        return longitude;
    }

    private static double estimatePrior(double longitude, double time) {
        double timeSunLastAtLongitude = time - meanSpeedOfSun * asSeason(initLongitude(compute(time) - longitude));
        double longitudeErrorDelta = initLongitude(compute(timeSunLastAtLongitude) - longitude);
        return Math.min(time, timeSunLastAtLongitude - meanSpeedOfSun * longitudeErrorDelta);
    }

    private static double compute(double time) {
        double julianCenturies = julianCenturies(time);
        double lambda = 282.7771834
                + 36000.76953744 * julianCenturies
                + 0.000005729577951308232 * sumLongSequenceOfPeriodicTerms(julianCenturies);

        double longitude = lambda + aberration(julianCenturies) + nutation(julianCenturies);
        return initLongitude(longitude);
    }

    private static double polynomialSum(double[] coefficients, double indeterminate) {
        double sum = coefficients[0];
        double indeterminateRaised = 1;
        for (int i = 1; i < coefficients.length; i++) {
            indeterminateRaised *= indeterminate;
            sum += (coefficients[i] * indeterminateRaised);
        }

        return sum;
    }

    private static double nutation(double julianCenturies) {
        double a = polynomialSum(coefficientsA, julianCenturies);
        double b = polynomialSum(coefficientsB, julianCenturies);
        return -0.004778 * sinOfDegree(a) - 0.0003667 * sinOfDegree(b);
    }

    private static double aberration(double julianCenturies) {
        return 0.0000974 * cosOfDegree(177.63 + (35999.01848 * julianCenturies)) - 0.005575;
    }

    private static double radiansFromDegrees(double degree) {
        return degree * Math.PI / 180;
    }

    private static double sinOfDegree(double degree) {
        return Math.sin(radiansFromDegrees(degree));
    }

    private static double cosOfDegree(double degree) {
        return Math.cos(radiansFromDegrees(degree));
    }

    private static double tanOfDegree(double degree) {
        return Math.tan(radiansFromDegrees(degree));
    }

    private static double periodicTerm(double julianCenturies, int x, double y, double z) {
        return x * sinOfDegree(y + z * julianCenturies);
    }

    private static double sumLongSequenceOfPeriodicTerms(double julianCenturies) {
        double sum = 0.0;
        sum += periodicTerm(julianCenturies, 403406, 270.54861, 0.9287892);
        sum += periodicTerm(julianCenturies, 195207, 340.19128, 35999.1376958);
        sum += periodicTerm(julianCenturies, 119433, 63.91854, 35999.4089666);
        sum += periodicTerm(julianCenturies, 112392, 331.2622, 35998.7287385);
        sum += periodicTerm(julianCenturies, 3891, 317.843, 71998.20261);
        sum += periodicTerm(julianCenturies, 2819, 86.631, 71998.4403);
        sum += periodicTerm(julianCenturies, 1721, 240.052, 36000.35726);
        sum += periodicTerm(julianCenturies, 660, 310.26, 71997.4812);
        sum += periodicTerm(julianCenturies, 350, 247.23, 32964.4678);
        sum += periodicTerm(julianCenturies, 334, 260.87, -19.441);
        sum += periodicTerm(julianCenturies, 314, 297.82, 445267.1117);
        sum += periodicTerm(julianCenturies, 268, 343.14, 45036.884);
        sum += periodicTerm(julianCenturies, 242, 166.79, 3.1008);
        sum += periodicTerm(julianCenturies, 234, 81.53, 22518.4434);
        sum += periodicTerm(julianCenturies, 158, 3.5, -19.9739);
        sum += periodicTerm(julianCenturies, 132, 132.75, 65928.9345);
        sum += periodicTerm(julianCenturies, 129, 182.95, 9038.0293);
        sum += periodicTerm(julianCenturies, 114, 162.03, 3034.7684);
        sum += periodicTerm(julianCenturies, 99, 29.8, 33718.148);
        sum += periodicTerm(julianCenturies, 93, 266.4, 3034.448);
        sum += periodicTerm(julianCenturies, 86, 249.2, -2280.773);
        sum += periodicTerm(julianCenturies, 78, 157.6, 29929.992);
        sum += periodicTerm(julianCenturies, 72, 257.8, 31556.493);
        sum += periodicTerm(julianCenturies, 68, 185.1, 149.588);
        sum += periodicTerm(julianCenturies, 64, 69.9, 9037.75);
        sum += periodicTerm(julianCenturies, 46, 8.0, 107997.405);
        sum += periodicTerm(julianCenturies, 38, 197.1, -4444.176);
        sum += periodicTerm(julianCenturies, 37, 250.4, 151.771);
        sum += periodicTerm(julianCenturies, 32, 65.3, 67555.316);
        sum += periodicTerm(julianCenturies, 29, 162.7, 31556.08);
        sum += periodicTerm(julianCenturies, 28, 341.5, -4561.54);
        sum += periodicTerm(julianCenturies, 27, 291.6, 107996.706);
        sum += periodicTerm(julianCenturies, 27, 98.5, 1221.655);
        sum += periodicTerm(julianCenturies, 25, 146.7, 62894.167);
        sum += periodicTerm(julianCenturies, 24, 110.0, 31437.369);
        sum += periodicTerm(julianCenturies, 21, 5.2, 14578.298);
        sum += periodicTerm(julianCenturies, 21, 342.6, -31931.757);
        sum += periodicTerm(julianCenturies, 20, 230.9, 34777.243);
        sum += periodicTerm(julianCenturies, 18, 256.1, 1221.999);
        sum += periodicTerm(julianCenturies, 17, 45.3, 62894.511);
        sum += periodicTerm(julianCenturies, 14, 242.9, -4442.039);
        sum += periodicTerm(julianCenturies, 13, 115.2, 107997.909);
        sum += periodicTerm(julianCenturies, 13, 151.8, 119.066);
        sum += periodicTerm(julianCenturies, 13, 285.3, 16859.071);
        sum += periodicTerm(julianCenturies, 12, 53.3, -4.578);
        sum += periodicTerm(julianCenturies, 10, 126.6, 26895.292);
        sum += periodicTerm(julianCenturies, 10, 205.7, -39.127);
        sum += periodicTerm(julianCenturies, 10, 85.9, 12297.536);
        sum += periodicTerm(julianCenturies, 10, 146.1, 90073.778);
        return sum;
    }

    private static double julianCenturies(double moment) {
        double dynamicalMoment = moment + ephemerisCorrection(moment);
        return (dynamicalMoment - noon2000Jan01) / daysInUniformLengthCentury;
    }

    // the following formulas defines a polynomial function which gives us the amount that the earth is slowing down for specific year ranges
    private static double defaultEphemerisCorrection(int gregorianYear) {
        // Contract.Assert(gregorianYear < 1620 || 2020 <= gregorianYear);
        long january1stOfYear = new CivilDate(gregorianYear, 1, 1).toJdn() - projectJdnOffset;
        double daysSinceStartOf1810 = january1stOfYear - startOf1810;
        double x = twelveHours + daysSinceStartOf1810;
        return (Math.pow(x, 2) / 41048480 - 15) / secondsPerDay;
    }

    private static double ephemerisCorrection1988to2019(int gregorianYear) {
        // Contract.Assert(1988 <= gregorianYear && gregorianYear <= 2019);
        return (double) (gregorianYear - 1933) / secondsPerDay;
    }

    private static double centuriesFrom1900(int gregorianYear) {
        long july1stOfYear = new CivilDate(gregorianYear, 7, 1).toJdn() - projectJdnOffset;
        return (double) (july1stOfYear - startOf1900Century) / daysInUniformLengthCentury;
    }

    private static double angle(int degrees, int minutes, double seconds) {
        return (seconds / secondsPerMinute + minutes) / minutesPerDegree + degrees;
    }

    private static double ephemerisCorrection1900to1987(int gregorianYear) {
        // Contract.Assert(1900 <= gregorianYear && gregorianYear <= 1987);
        double centuriesFrom1900 = centuriesFrom1900(gregorianYear);
        return polynomialSum(coefficients1900to1987, centuriesFrom1900);
    }

    private static double ephemerisCorrection1800to1899(int gregorianYear) {
        // Contract.Assert(1800 <= gregorianYear && gregorianYear <= 1899);
        double centuriesFrom1900 = centuriesFrom1900(gregorianYear);
        return polynomialSum(coefficients1800to1899, centuriesFrom1900);
    }

    private static double ephemerisCorrection1700to1799(int gregorianYear) {
        // Contract.Assert(1700 <= gregorianYear && gregorianYear <= 1799);
        double yearsSince1700 = gregorianYear - 1700;
        return polynomialSum(coefficients1700to1799, yearsSince1700) / secondsPerDay;
    }

    private static double ephemerisCorrection1620to1699(int gregorianYear) {
        // Contract.Assert(1620 <= gregorianYear && gregorianYear <= 1699);
        double yearsSince1600 = gregorianYear - 1600;
        return polynomialSum(coefficients1620to1699, yearsSince1600) / secondsPerDay;
    }

    private static int getGregorianYear(double numberOfDays) {
        return new CivilDate((long) (Math.floor(numberOfDays)) + projectJdnOffset).getYear();
    }

    // ephemeris-correction: correction to account for the slowing down of the rotation of the earth
    private static double ephemerisCorrection(double time) {
        int year = getGregorianYear(time);
        for (EphemerisCorrectionAlgorithmMap map : ephemerisCorrectionTable) {
            if (map.lowestYear <= year) {
                switch (map.algorithm) {
                    case Default:
                        return defaultEphemerisCorrection(year);
                    case Year1988to2019:
                        return ephemerisCorrection1988to2019(year);
                    case Year1900to1987:
                        return ephemerisCorrection1900to1987(year);
                    case Year1800to1899:
                        return ephemerisCorrection1800to1899(year);
                    case Year1700to1799:
                        return ephemerisCorrection1700to1799(year);
                    case Year1620to1699:
                        return ephemerisCorrection1620to1699(year);
                }

                break; // break the loop and assert eventually
            }
        }

        // Contract.Assert(false, "Not expected to come here");
        return defaultEphemerisCorrection(year);
    }

    private static double asDayFraction(double longitude) {
        return longitude / fullCircleOfArc;
    }

    private static double obliquity(double julianCenturies) {
        return polynomialSum(coefficients, julianCenturies);
    }

    // equation-of-time; approximate the difference between apparent solar time and mean solar time
    // formal definition is EOT = GHA - GMHA
    // GHA is the Greenwich Hour Angle of the apparent (actual) Sun
    // GMHA is the Greenwich Mean Hour Angle of the mean (fictitious) Sun
    // http://www.esrl.noaa.gov/gmd/grad/solcalc/
    // http://en.wikipedia.org/wiki/Equation_of_time
    private static double equationOfTime(double time) {
        double julianCenturies = julianCenturies(time);
        double lambda = polynomialSum(lambdaCoefficients, julianCenturies);
        double anomaly = polynomialSum(anomalyCoefficients, julianCenturies);
        double eccentricity = polynomialSum(eccentricityCoefficients, julianCenturies);

        double epsilon = obliquity(julianCenturies);
        double tanHalfEpsilon = tanOfDegree(epsilon / 2);
        double y = tanHalfEpsilon * tanHalfEpsilon;

        double dividend = y * sinOfDegree(2 * lambda)
                - 2 * eccentricity * sinOfDegree(anomaly)
                + 4 * eccentricity * y * sinOfDegree(anomaly) * cosOfDegree(2 * lambda)
                - 0.5 * Math.pow(y, 2) * sinOfDegree(4 * lambda)
                - 1.25 * Math.pow(eccentricity, 2) * sinOfDegree(2 * anomaly);
        double divisor = 2 * Math.PI;
        double equation = dividend / divisor;

        // approximation of equation of time is not valid for dates that are many millennia in the past or future
        // thus limited to a half day
        return Math.copySign(Math.min(Math.abs(equation), twelveHours), equation);
    }

    private static double asLocalTime(double apparentMidday, double longitude) {
        // slightly inaccurate since equation of time takes mean time not apparent time as its argument, but the difference is negligible
        double universalTime = apparentMidday - asDayFraction(longitude);
        return apparentMidday - equationOfTime(universalTime);
    }

    // midday
    private static double midday(double date, double longitude) {
        return asLocalTime(date + twelveHours, longitude) - asDayFraction(longitude);
    }

    // midday-in-tehran
    private static double middayAtPersianObservationSite(double date) {
        return midday(date, initLongitude(52.5)); // 52.5 degrees east - longitude of UTC+3:30 which defines Iranian Standard Time
    }

    // persian-new-year-on-or-before
    //  number of days is the absolute date. The absolute date is the number of days from January 1st, 1 A.D.
    //  1/1/0001 is absolute date 1.
    private static long persianNewYearOnOrBefore(long numberOfDays) {
        double date = (double) numberOfDays;

        double approx = estimatePrior(longitudeSpring, middayAtPersianObservationSite(date));
        long lowerBoundNewYearDay = (long) Math.floor(approx) - 1;
        long upperBoundNewYearDay = lowerBoundNewYearDay + 3; // estimate is generally within a day of the actual occurrance (at the limits, the error expands, since the calculations rely on the mean tropical year which changes...)
        long day = lowerBoundNewYearDay;
        for (; day != upperBoundNewYearDay; ++day) {
            double midday = middayAtPersianObservationSite((double) day);
            double l = compute(midday);
            if (longitudeSpring <= l && l <= twoDegreesAfterSpring) break;
        }
        // Contract.Assert(day != upperBoundNewYearDay);
        return day - 1;
    }

    private static int monthFromOrdinalDay(int ordinalDay) {
        int index = 0;
        while (ordinalDay > daysToMonth[index]) index++;
        return index;
    }

    private enum CorrectionAlgorithm {
        Default, Year1988to2019, Year1900to1987, Year1800to1899, Year1700to1799, Year1620to1699
    }

    private static class EphemerisCorrectionAlgorithmMap {
        final int lowestYear;
        final CorrectionAlgorithm algorithm;

        EphemerisCorrectionAlgorithmMap(int year, CorrectionAlgorithm algorithm) {
            lowestYear = year;
            this.algorithm = algorithm;
        }
    }
}
