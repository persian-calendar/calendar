// Ported from
// https://github.com/roozbehp/persiancalendar/blob/daf8fb2b46466a324cee98833c19c36aa5d97f39/persiancalendar.py
// Which is released under Apache 2.0 license
package io.github.persiancalendar.calendar.persian

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.tan

internal object AlgorithmicConverter {
    /** The value of x shifted into the range [a..b). Returns x if a=b. */
    private fun mod3(x: Double, a: Int, b: Int): Double =
        if (a == b) x else a + (x - a) % (b - a)

    /** Sum powers of x with coefficients (from order 0 up) in list a. */
    private fun poly(x: Double, a: List<Double>): Double =
        if (a.isEmpty()) 0.0 else a[0] + x * poly(x, a.drop(1))

    /**
     * Identity function for fixed dates/moments. If internal
     * timekeeping is shifted, change epoch to be RD date of
     * origin of internal count. epoch should be an integer.
     */
    private fun rd(tee: Int): Int {
        val epoch = 0
        return tee - epoch
    }

    // Fixed date of start of the (proleptic) Gregorian calendar.
    private val GREGORIAN_EPOCH = rd(1)

    /**
     * True if g_year is a leap year on the Gregorian calendar.
     */
    private fun gregorianLeapYear(gYear: Int): Boolean =
        gYear % 4 == 0 && (gYear % 400) !in listOf(100, 200, 300)

    /**
     * Fixed date equivalent to the Gregorian date g_date.
     */
    private fun fixedFromGregorian(gDate: Triple<Int, Int, Int>): Int {
        val (year, month, day) = gDate
        return (GREGORIAN_EPOCH - 1  // Days before start of calendar
                + 365 * (year - 1)  // Ordinary days since epoch
                + (year - 1) / 4   // Julian leap days since epoch...
                - (year - 1) / 100  // ...minus century years since epoch...
                + (year - 1) / 400  // plus years since epoch divisible by 400.
                // Days in prior months this year assuming 30-day Feb
                + (367 * month - 362) / 12
                // Correct for 28- or 29-day Feb
                + (if (month <= 2) 0 else if (gregorianLeapYear(year)) -1 else -2)
                + day)  // Days so far this month.
    }

    /**
     * Gregorian year corresponding to the fixed date.
     */
    private fun gregorianYearFromFixed(date: Int): Int {
        val d0 = date - GREGORIAN_EPOCH  // Prior days.
        val n400 = d0 / 146097  // Completed 400-year cycles.
        val d1 = d0 % 146097  // Prior days not in n400.
        val n100 = d1 / 36524  // 100-year cycles not in n400.
        val d2 = d1 % 36524  // Prior days not in n400 or n100.
        val n4 = d2 / 1461  // 4-year cycles not in n400 or n100.
        val d3 = d2 % 1461  // Prior days not in n400, n100, or n4.
        val n1 = d3 / 365  // Years not in n400, n100, or n4.
        val year = 400 * n400 + 100 * n100 + 4 * n4 + n1
        return if (n100 == 4 || n1 == 4) {
            year  // Date is day 366 in a leap year.
        } else {
            year + 1  // Date is ordinal day (d % 365 + 1) in (year + 1).
        }
    }

    /**
     * Fixed date of January 1 in g_year.
     */
    private fun gregorianNewYear(gYear: Int): Int =
        fixedFromGregorian(Triple(gYear, 1, 1))

    /**
     * Gregorian (year, month, day) corresponding to fixed date.
     */
    private fun gregorianFromFixed(date: Int): Triple<Int, Int, Int> {
        val year = gregorianYearFromFixed(date)
        val priorDays = date - gregorianNewYear(year)  // This year
        // To simulate a 30-day Feb
        val correction = when {
            date < fixedFromGregorian(Triple(year, 3, 1)) -> 0
            gregorianLeapYear(year) -> 1
            else -> 2
        }
        val month = (12 * (priorDays + correction) + 373) / 367  // Assuming a 30-day Feb
        // Calculate the day by subtraction.
        val day = date - fixedFromGregorian(Triple(year, month, 1)) + 1
        return Triple(year, month, day)
    }

    /**
     * Number of days from Gregorian date g_date1 until g_date2.
     */
    private fun gregorianDateDifference(
        gDate1: Triple<Int, Int, Int>,
        gDate2: Triple<Int, Int, Int>
    ): Int = fixedFromGregorian(gDate2) - fixedFromGregorian(gDate1)

    // Fixed date of start of the Julian calendar.
    private val JULIAN_EPOCH = fixedFromGregorian(Triple(0, 12, 30))

    /** True if j_year is a leap year on the Julian calendar. */
    private fun julianLeapYear(jYear: Int): Boolean = (jYear % 4) == (if (jYear > 0) 0 else 3)

    /** Fixed date equivalent to the Julian date. */
    private fun fixedFromJulian(year: Int, month: Int, day: Int): Int {
        val y = if (year < 0) year + 1 else year  // No year zero
        return (JULIAN_EPOCH - 1  // Days before start of calendar
                + 365 * (y - 1)  // Ordinary days since epoch.
                + (y - 1) / 4   // Leap days since epoch...
                // Days in prior months this year...
                + ((367 * month - 362) / 12)  // ...assuming 30-day Feb
                // Correct for 28- or 29-day Feb
                + (if (month <= 2) 0 else if (julianLeapYear(year)) -1 else -2)
                + day)           // Days so far this month.
    }

    /** x hours. */
    private fun hr(x: Int): Double = x / 24.0

    /** d degrees, m arcminutes, s arcseconds. */
    private fun angle(d: Int, m: Int, s: Double): Double = d + (m + s / 60) / 60.0

    /** Convert angle theta from degrees to radians. */
    private fun radiansFromDegrees(theta: Double): Double = (theta % 360) * PI / 180

    /** Sine of theta (given in degrees). */
    private fun sinDegrees(theta: Double): Double = sin(radiansFromDegrees(theta))

    /** Cosine of theta (given in degrees). */
    private fun cosDegrees(theta: Double): Double = cos(radiansFromDegrees(theta))

    /** Tangent of theta (given in degrees). */
    private fun tanDegrees(theta: Double): Double = tan(radiansFromDegrees(theta))

    private fun longitude(location: List<Double>): Double = location[1]

    /**
     * Difference between UT and local mean time at longitude
     * phi as a fraction of a day.
     */
    private fun zoneFromLongitude(phi: Double): Double = phi / 360

    /** Universal time from local tee_ell at location */
    private fun universalFromLocal(teeEll: Double, location: List<Double>): Double =
        teeEll - zoneFromLongitude(longitude(location))

    /** Local time from sundial time tee at location. */
    private fun localFromApparent(tee: Double, location: List<Double>): Double =
        tee - equationOfTime(universalFromLocal(tee, location))

    /** Universal time from sundial time tee at location */
    private fun universalFromApparent(tee: Double, location: List<Double>): Double =
        universalFromLocal(localFromApparent(tee, location), location)

    /** Universal time on fixed date of midday at location */
    private fun midday(date: Int, location: List<Double>): Double =
        universalFromApparent(date + hr(12), location)

    /** Julian centuries since 2000 at moment tee. */
    private fun julianCenturies(tee: Double): Double =
        (dynamicalFromUniversal(tee) - J2000) / 36525

    /** Obliquity of ecliptic at moment tee. */
    private fun obliquity(tee: Double): Double {
        val c = julianCenturies(tee)
        return angle(23, 26, 21.448) + poly(
            c, listOf(
                0.0,
                angle(0, 0, -46.8150),
                angle(0, 0, -0.00059),
                angle(0, 0, 0.001813)
            )
        )
    }

    /** Dynamical time at Universal moment tee_rom_u. */
    private fun dynamicalFromUniversal(teeRomU: Double): Double =
        teeRomU + ephemerisCorrection(teeRomU)

    // Noon at start of Gregorian year 2000.
    private val J2000 = hr(12) + gregorianNewYear(2000)

    private const val MEAN_TROPICAL_YEAR = 365.242189

    /**
     * Dynamical Time minus Universal Time (in days) for moment tee.
     *
     * Adapted from "Astronomical Algorithms"
     * by Jean Meeus, Willmann-Bell (1991) for years
     * 1600-1986 and from polynomials on the NASA
     * Eclipse web site for other years.
     */
    private fun ephemerisCorrection(tee: Double): Double {
        val year = gregorianYearFromFixed(floor(tee).toInt())
        val c = gregorianDateDifference(Triple(1900, 1, 1), Triple(year, 7, 1)) / 36525.0
        val c2051 = (-20 + 32 * ((year - 1820) / 100.0).pow(2)
                + 0.5628 * (2150 - year)) / 86400
        val y2000 = year - 2000
        val c2006 = poly(y2000.toDouble(), listOf(62.92, 0.32217, 0.005589)) / 86400
        val c1987 = poly(
            y2000.toDouble(), listOf(
                63.86, 0.3345, -0.060374,
                0.0017275,
                0.000651814, 0.00002373599
            )
        ) / 86400
        val c1900 = poly(
            c, listOf(
                -0.00002, 0.000297, 0.025184,
                -0.181133, 0.553040, -0.861938,
                0.677066, -0.212591
            )
        )
        val c1800 = poly(
            c, listOf(
                -0.000009, 0.003844, 0.083563,
                0.865736,
                4.867575, 15.845535, 31.332267,
                38.291999, 28.316289, 11.636204,
                2.043794
            )
        )
        val y1700 = year - 1700
        val c1700 = poly(
            y1700.toDouble(), listOf(
                8.118780842, -0.005092142,
                0.003336121, -0.0000266484
            )
        ) / 86400
        val y1600 = year - 1600
        val c1600 = poly(
            y1600.toDouble(), listOf(
                120.0, -0.9808, -0.01532,
                0.000140272128
            )
        ) / 86400
        val y1000 = (year - 1000) / 100.0
        val c500 = poly(
            y1000, listOf(
                1574.2, -556.01, 71.23472, 0.319781,
                -0.8503463, -0.005050998,
                0.0083572073
            )
        ) / 86400
        val y0 = year / 100.0
        val c0 = poly(
            y0, listOf(
                10583.6, -1014.41, 33.78311,
                -5.952053, -0.1798452, 0.022174192,
                0.0090316521
            )
        ) / 86400
        val y1820 = (year - 1820) / 100.0
        val other = poly(y1820, listOf(-20.0, 0.0, 32.0)) / 86400
        return when {
            2051 <= year && year <= 2150 -> c2051
            2006 <= year && year <= 2050 -> c2006
            1987 <= year && year <= 2005 -> c1987
            1900 <= year && year <= 1986 -> c1900
            1800 <= year && year <= 1899 -> c1800
            1700 <= year && year <= 1799 -> c1700
            1600 <= year && year <= 1699 -> c1600
            500 <= year && year <= 1599 -> c500
            -500 < year && year < 500 -> c0
            else -> other
        }
    }

    /**
     * Equation of time (as fraction of day) for moment tee.
     *
     * Adapted from "Astronomical Algorithms" by Jean Meeus,
     * Willmann-Bell, 2nd edn., 1998, p. 185.
     */
    private fun equationOfTime(tee: Double): Double {
        val c = julianCenturies(tee)
        val lamda = poly(c, listOf(280.46645, 36000.76983, 0.0003032))
        val anomaly = poly(c, listOf(357.52910, 35999.05030, -0.0001559, -0.00000048))
        val eccentricity = poly(c, listOf(0.016708617, -0.000042037, -0.0000001236))
        val varepsilon = obliquity(tee)
        val y = tanDegrees(varepsilon / 2).pow(2)
        val equation = ((1.0 / 2 / PI) *
                (y * sinDegrees(2 * lamda)
                        - 2 * eccentricity * sinDegrees(anomaly)
                        + 4 * eccentricity * y * sinDegrees(anomaly)
                        * cosDegrees(2 * lamda)
                        - 0.5 * y * y * sinDegrees(4 * lamda)
                        - 1.25 * eccentricity * eccentricity
                        * sinDegrees(2 * anomaly)))
        val result = sign(equation) * min(abs(equation), hr(12))
        return sign(equation) * min(abs(equation), hr(12))
    }

    /**
     * Longitude of sun at moment tee.
     *
     * Adapted from "Planetary Programs and Tables from -4000
     * to +2800" by Pierre Bretagnon and Jean-Louis Simon,
     * Willmann-Bell, 1986.
     */
    private fun solarLongitude(tee: Double): Double {
        val c = julianCenturies(tee)  // moment in Julian centuries
        val coefficients = listOf(
            403406, 195207, 119433, 112392, 3891, 2819, 1721,
            660, 350, 334, 314, 268, 242, 234, 158, 132, 129, 114,
            99, 93, 86, 78, 72, 68, 64, 46, 38, 37, 32, 29, 28, 27, 27,
            25, 24, 21, 21, 20, 18, 17, 14, 13, 13, 13, 12, 10, 10, 10,
            10
        )
        val multipliers = listOf(
            0.9287892, 35999.1376958, 35999.4089666,
            35998.7287385, 71998.20261, 71998.4403,
            36000.35726, 71997.4812, 32964.4678,
            -19.4410, 445267.1117, 45036.8840, 3.1008,
            22518.4434, -19.9739, 65928.9345,
            9038.0293, 3034.7684, 33718.148, 3034.448,
            -2280.773, 29929.992, 31556.493, 149.588,
            9037.750, 107997.405, -4444.176, 151.771,
            67555.316, 31556.080, -4561.540,
            107996.706, 1221.655, 62894.167,
            31437.369, 14578.298, -31931.757,
            34777.243, 1221.999, 62894.511,
            -4442.039, 107997.909, 119.066, 16859.071,
            -4.578, 26895.292, -39.127, 12297.536,
            90073.778
        )
        val addends = listOf(
            270.54861, 340.19128, 63.91854, 331.26220,
            317.843, 86.631, 240.052, 310.26, 247.23,
            260.87, 297.82, 343.14, 166.79, 81.53,
            3.50, 132.75, 182.95, 162.03, 29.8,
            266.4, 249.2, 157.6, 257.8, 185.1, 69.9,
            8.0, 197.1, 250.4, 65.3, 162.7, 341.5,
            291.6, 98.5, 146.7, 110.0, 5.2, 342.6,
            230.9, 256.1, 45.3, 242.9, 115.2, 151.8,
            285.3, 53.3, 126.6, 205.7, 85.9,
            146.1
        )
        val lamda = (
                282.7771834
                        + 36000.76953744 * c
                        + 0.000005729577951308232 *
                        coefficients.indices.sumOf { i ->
                            coefficients[i] * sinDegrees(addends[i] + multipliers[i] * c)
                        }
                )
        return (lamda + aberration(tee) + nutation(tee)).mod(360.0)
    }

    /**
     * Longitudinal nutation at moment tee.
     */
    private fun nutation(tee: Double): Double {
        val c = julianCenturies(tee)  // moment in Julian centuries
        val capA = poly(c, listOf(124.90, -1934.134, 0.002063))
        val capB = poly(c, listOf(201.11, 72001.5377, 0.00057))
        return -0.004778 * sinDegrees(capA) - 0.0003667 * sinDegrees(capB)
    }

    /**
     * Aberration at moment tee.
     */
    private fun aberration(tee: Double): Double {
        val c = julianCenturies(tee)  // moment in Julian centuries
        return 0.0000974 * cosDegrees(177.63 + 35999.01848 * c) - 0.005575
    }

    // Longitude of sun at vernal equinox.
    private const val SPRING = 0.0

    /**
     * Approximate moment at or before tee
     * when solar longitude just exceeded lamda degrees.
     */
    private fun estimatePriorSolarLongitude(lamda: Double, tee: Double): Double {
        val rate = MEAN_TROPICAL_YEAR / 360  // Mean change of one degree.
        // First approximation.
        var tau = tee - rate * ((solarLongitude(tee) - lamda) % 360)
        val capDelta = mod3((solarLongitude(tau) - lamda), -180, 180)
        return min(tee, tau - rate * capDelta)
    }

    // Fixed date of start of the Persian calendar.
    private val PERSIAN_EPOCH = fixedFromJulian(622, 3, 19)

    // Location of Tehran, Iran.
    private val TEHRAN = listOf(35.68, 51.42, 1100.0, +3.5)

    // Middle of Iran.
    private val IRAN = listOf(35.5, 52.5, 0.0, +3.5)

    private var persianLocale = IRAN

    /**
     * Universal time of true noon on fixed date in the locale used for computing the Persian calendar.
     */
    private fun middayInPersianLocale(date: Int): Double = midday(date, persianLocale)

    /**
     * Fixed date of Astronomical Persian New Year on or before fixed date.
     */
    private fun persianNewYearOnOrBefore(date: Int): Int {
        // Approximate time of equinox.
        val approx = estimatePriorSolarLongitude(SPRING, middayInPersianLocale(date))
        var day = floor(approx).toInt() - 1
        while (solarLongitude(middayInPersianLocale(day)) > SPRING + 2) {
            day += 1
        }
        return day
    }

    /**
     * Fixed date of Borji Persian new month on or before fixed date.
     */
    private fun persianBorjiNewMonthOnOrBefore(date: Int, month: Int): Int {
        // Approximate time of equinox.
        val targetLong = (month - 1) * 30.0
        val approx = estimatePriorSolarLongitude(
            targetLong, middayInPersianLocale(date)
        )
        var day = floor(approx).toInt() - 1
        while (!(targetLong + 2 > solarLongitude(middayInPersianLocale(day)) &&
                    solarLongitude(middayInPersianLocale(day)) >= targetLong)
        ) day += 1
        return day
    }

    /**
     * Fixed date of Astronomical Persian date p_date.
     */
    internal fun fixedFromPersian(year: Int, month: Int, day: Int): Int {
        val newYear = persianNewYearOnOrBefore(
            PERSIAN_EPOCH + 180  // Fall after epoch.
                    + floor(
                MEAN_TROPICAL_YEAR *
                        (if (0 < year) year - 1 else year)
            ).toInt()
        )  // No year zero.
        return (newYear - 1  // Days in prior years.
                // Days in prior months this year.
                + (if (month <= 7) 31 * (month - 1) else 30 * (month - 1) + 6)
                + day)  // Days so far this month.
    }

    /**
     * Fixed date of Borji Persian date p_date.
     */
    private fun fixedFromPersianBorji(pDate: Triple<Int, Int, Int>): Int {
        val (year, month, day) = pDate
        val newMonth = persianBorjiNewMonthOnOrBefore(
            PERSIAN_EPOCH + 180
                    + floor(
                MEAN_TROPICAL_YEAR *
                        ((if (0 < year) year - 1 else year) + (month - 1) / 12.0)
            ).toInt(),
            month
        )
        return (newMonth - 1  // Days in prior months.
                + day)  // Days so far this month.
    }

    /**
     * Astronomical Persian date corresponding to fixed date.
     */
    internal fun persianFromFixed(date: Int): IntArray {
        val newYear = persianNewYearOnOrBefore(date)
        val y = round((newYear - PERSIAN_EPOCH) / MEAN_TROPICAL_YEAR).toInt() + 1
        val year = if (0 < y) y else y - 1  // No year zero
        val dayOfYear = date - fixedFromPersian(year, 1, 1) + 1
        val month =
            if (dayOfYear <= 186) ceil(dayOfYear / 31.0).toInt()
            else ceil((dayOfYear - 6) / 30.0).toInt()
        // Calculate the day by subtraction
        val day = date - fixedFromPersian(year, month, 1) + 1
        println("$date $year/$month/$day")
        return intArrayOf(year, month, day)
    }

    /**
     * Borji Persian date corresponding to fixed date.
     */
    private fun persianBorjiFromFixed(date: Int): IntArray {
        val newYear = persianNewYearOnOrBefore(date)
        val y = round((newYear - PERSIAN_EPOCH) / MEAN_TROPICAL_YEAR).toInt() + 1
        val year = if (0 < y) y else y - 1  // No year zero
        var month = 1
        while (month < 12 && date >= fixedFromPersianBorji(Triple(year, month + 1, 1))) {
            month += 1
        }
        // Calculate the day by subtraction
        val day = date - fixedFromPersianBorji(Triple(year, month, 1)) + 1
        return intArrayOf(year, month, day)
    }

    /**
     * Fixed date of Persian New Year (Nowruz) in Gregorian year g_year.
     */
    private fun nowruz(gYear: Int): Int {
        val persianYear = gYear - gregorianYearFromFixed(PERSIAN_EPOCH) + 1
        val y = if (persianYear <= 0) persianYear - 1 else persianYear  // No Persian year 0
        return fixedFromPersian(y, 1, 1)
    }

    /**
     * True if g_year is a leap year on the Persian calendar.
     */
    private fun persianLeapYear(pYear: Int): Boolean {
        val thisNowruz = fixedFromPersian(pYear, 1, 1)
        val nextNowruz = fixedFromPersian(pYear + 1, 1, 1)
        return nextNowruz - thisNowruz == 366
    }

    private const val OFFSET_JDN = 1_721_425L
    fun fromJdn(jdn: Long): IntArray = persianFromFixed((jdn - OFFSET_JDN).toInt())

    fun toJdn(year: Int, month: Int, dayOfMonth: Int): Long =
        fixedFromPersian(year, month, dayOfMonth).toLong() + OFFSET_JDN

//    fun main() {
//        val today = java.time.LocalDate.now()
//        val fixedDate = fixedFromGregorian(Triple(today.year, today.monthValue, today.dayOfMonth))
//        val persianDate = persianFromFixed(fixedDate)
//        println("${persianDate.first}/${persianDate.second}/${persianDate.third}")
//    }
}
