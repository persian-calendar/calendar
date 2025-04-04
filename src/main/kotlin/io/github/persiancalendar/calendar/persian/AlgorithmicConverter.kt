// Ported from
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/PersianCalendar.cs
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/CalendricalCalculationsHelper.cs#L8
// Which is released under The MIT License (MIT)
package io.github.persiancalendar.calendar.persian

import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate.Companion.daysInPreviousMonths
import io.github.persiancalendar.calendar.PersianDate.Companion.monthFromDaysCount
import io.github.persiancalendar.calendar.util.cosOfDegree
import io.github.persiancalendar.calendar.util.sinOfDegree
import io.github.persiancalendar.calendar.util.tanOfDegree
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.withSign

internal object AlgorithmicConverter {
    private const val projectJdnOffset: Long =
        1721426 // Offset from Jdn to jdn used in this converter
    private const val persianEpoch: Long = 226895 // new DateTime(622, 3, 22).Ticks / TicksPerDay
    private const val meanTropicalYearInDays = 365.242189
    private const val fullCircleOfArc = 360.0
    private const val meanSpeedOfSun = meanTropicalYearInDays / fullCircleOfArc
    private const val halfCircleOfArc = 180
    private const val noon2000Jan01 = 730120.5
    private const val daysInUniformLengthCentury = 36525
    private val startOf1810: Long = CivilDate(1810, 1, 1).toJdn() - projectJdnOffset
    private val startOf1900Century: Long = CivilDate(1900, 1, 1).toJdn() - projectJdnOffset
    private const val twelveHours = .5 // half a day
    private const val secondsPerDay = 24 * 60 * 60 // 24 hours * 60 minutes * 60 seconds
    private const val secondsPerMinute = 60
    private const val minutesPerDegree = 60
    private val coefficients1900to1987 = doubleArrayOf(
        -0.00002,
        0.000297,
        0.025184,
        -0.181133,
        0.553040,
        -0.861938,
        0.677066,
        -0.212591,
    )
    private val coefficients1800to1899 = doubleArrayOf(
        -0.000009,
        0.003844,
        0.083563,
        0.865736,
        4.867575,
        15.845535,
        31.332267,
        38.291999,
        28.316289,
        11.636204,
        2.043794,
    )
    private val coefficients1700to1799 = doubleArrayOf(
        8.118780842,
        -0.005092142,
        0.003336121,
        -0.0000266484,
    )
    private val coefficients1620to1699 = doubleArrayOf(
        196.58333,
        -4.0675,
        0.0219167,
    )
    private val lambdaCoefficients = doubleArrayOf(
        280.46645,
        36000.76983,
        0.0003032,
    )
    private val anomalyCoefficients = doubleArrayOf(
        357.52910,
        35999.05030,
        -0.0001559,
        -0.00000048,
    )
    private val eccentricityCoefficients = doubleArrayOf(
        0.016708617,
        -0.000042037,
        -0.0000001236,
    )
    private val coefficients = doubleArrayOf(
        angle(23, 26, 21.448),
        angle(0, 0, -46.8150),
        angle(0, 0, -0.00059),
        angle(0, 0, 0.001813),
    )
    private val coefficientsA = doubleArrayOf(
        124.90,
        -1934.134,
        0.002063,
    )
    private val coefficientsB = doubleArrayOf(
        201.11,
        72001.5377,
        0.00057,
    )

    fun toJdn(year: Int, month: Int, day: Int, oldEra: Boolean): Long {
        val approximateHalfYear = 180
        val approximateDaysFromEpochForYearStart = (meanTropicalYearInDays * (year - 1)).toInt()
        return if (oldEra) {
            persianNewYearOnOrBefore(
                numberOfDays = persianEpoch + approximateDaysFromEpochForYearStart + (month - 1) * 30,
                month = month,
                oldEra = true,
            )
        } else {
            persianNewYearOnOrBefore(
                numberOfDays = persianEpoch + approximateDaysFromEpochForYearStart + approximateHalfYear,
                month = 1,
                oldEra = false,
            ) + daysInPreviousMonths(month)
        } + day - 1 + // day is one based, make 0 based since this will be the number of days we add to beginning of year below
                projectJdnOffset
    }

    fun fromJdn(jdn: Long, oldEra: Boolean): IntArray {
        var jdn = jdn
        jdn++ // TODO: Investigate why this is needed
        val yearStart = persianNewYearOnOrBefore(
            numberOfDays = jdn - projectJdnOffset,
            month = 1,
            oldEra = oldEra,
        )
        val y: Int = floor((yearStart - persianEpoch) / meanTropicalYearInDays + 0.5).toInt() + 1
        val ordinalDay = (jdn - toJdn(y, 1, 1, oldEra)).toInt()
        val m = monthFromDaysCount(ordinalDay)
        val d = ordinalDay - daysInPreviousMonths(m)
        return intArrayOf(y, m, d)
    }

    private fun asSeason(longitude: Double): Double =
        if (longitude < 0) longitude + fullCircleOfArc else longitude

    private fun initLongitude(longitude: Double): Double =
        normalizeLongitude(longitude + halfCircleOfArc) - halfCircleOfArc

    private fun normalizeLongitude(longitude: Double): Double {
        var longitude = longitude
        longitude %= fullCircleOfArc
        if (longitude < 0) longitude += fullCircleOfArc
        return longitude
    }

    private fun estimatePrior(longitude: Double, time: Double): Double {
        val timeSunLastAtLongitude =
            time - meanSpeedOfSun * asSeason(initLongitude(compute(time) - longitude))
        val longitudeErrorDelta = initLongitude(compute(timeSunLastAtLongitude) - longitude)
        return min(time, timeSunLastAtLongitude - meanSpeedOfSun * longitudeErrorDelta)
    }

    private fun compute(time: Double): Double {
        val julianCenturies = julianCenturies(time)
        val lambda = 282.7771834 +
                (36000.76953744 * julianCenturies) +
                (0.000005729577951308232 * sumLongSequenceOfPeriodicTerms(julianCenturies))
        val longitude = lambda + aberration(julianCenturies) + nutation(julianCenturies)
        return initLongitude(longitude)
    }

    private fun polynomialSum(coefficients: DoubleArray, indeterminate: Double): Double {
        var sum = coefficients[0]
        var indeterminateRaised = 1.0
        for (i in 1 until coefficients.size) {
            indeterminateRaised *= indeterminate
            sum += coefficients[i] * indeterminateRaised
        }
        return sum
    }

    private fun nutation(julianCenturies: Double): Double {
        val a = polynomialSum(coefficientsA, julianCenturies)
        val b = polynomialSum(coefficientsB, julianCenturies)
        return -0.004778 * sinOfDegree(a) - 0.0003667 * sinOfDegree(b)
    }

    private fun aberration(julianCenturies: Double): Double =
        (0.0000974 * cosOfDegree(177.63 + 35999.01848 * julianCenturies)) - 0.005575

    private val terms = listOf(
        Triple(403406, 270.54861, 0.9287892),
        Triple(195207, 340.19128, 35999.1376958),
        Triple(119433, 63.91854, 35999.4089666),
        Triple(112392, 331.2622, 35998.7287385),
        Triple(3891, 317.843, 71998.20261),
        Triple(2819, 86.631, 71998.4403),
        Triple(1721, 240.052, 36000.35726),
        Triple(660, 310.26, 71997.4812),
        Triple(350, 247.23, 32964.4678),
        Triple(334, 260.87, -19.441),
        Triple(314, 297.82, 445267.1117),
        Triple(268, 343.14, 45036.884),
        Triple(242, 166.79, 3.1008),
        Triple(234, 81.53, 22518.4434),
        Triple(158, 3.5, -19.9739),
        Triple(132, 132.75, 65928.9345),
        Triple(129, 182.95, 9038.0293),
        Triple(114, 162.03, 3034.7684),
        Triple(99, 29.8, 33718.148),
        Triple(93, 266.4, 3034.448),
        Triple(86, 249.2, -2280.773),
        Triple(78, 157.6, 29929.992),
        Triple(72, 257.8, 31556.493),
        Triple(68, 185.1, 149.588),
        Triple(64, 69.9, 9037.75),
        Triple(46, 8.0, 107997.405),
        Triple(38, 197.1, -4444.176),
        Triple(37, 250.4, 151.771),
        Triple(32, 65.3, 67555.316),
        Triple(29, 162.7, 31556.08),
        Triple(28, 341.5, -4561.54),
        Triple(27, 291.6, 107996.706),
        Triple(27, 98.5, 1221.655),
        Triple(25, 146.7, 62894.167),
        Triple(24, 110.0, 31437.369),
        Triple(21, 5.2, 14578.298),
        Triple(21, 342.6, -31931.757),
        Triple(20, 230.9, 34777.243),
        Triple(18, 256.1, 1221.999),
        Triple(17, 45.3, 62894.511),
        Triple(14, 242.9, -4442.039),
        Triple(13, 115.2, 107997.909),
        Triple(13, 151.8, 119.066),
        Triple(13, 285.3, 16859.071),
        Triple(12, 53.3, -4.578),
        Triple(10, 126.6, 26895.292),
        Triple(10, 205.7, -39.127),
        Triple(10, 85.9, 12297.536),
        Triple(10, 146.1, 90073.778)
    )

    private fun sumLongSequenceOfPeriodicTerms(julianCenturies: Double): Double =
        terms.sumOf { (x, y, z) -> x * sinOfDegree(y + z * julianCenturies) }

    private fun julianCenturies(moment: Double): Double {
        val dynamicalMoment = moment + ephemerisCorrection(moment)
        return (dynamicalMoment - noon2000Jan01) / daysInUniformLengthCentury
    }

    private fun centuriesFrom1900(gregorianYear: Int): Double {
        val july1stOfYear: Long = CivilDate(gregorianYear, 7, 1).toJdn() - projectJdnOffset
        return (july1stOfYear - startOf1900Century).toDouble() / daysInUniformLengthCentury
    }

    private fun angle(degrees: Int, minutes: Int, seconds: Double): Double =
        (seconds / secondsPerMinute + minutes) / minutesPerDegree + degrees

    private fun getGregorianYear(numberOfDays: Double): Int =
        CivilDate(floor(numberOfDays).toLong() + projectJdnOffset).year

    // ephemeris-correction: correction to account for the slowing down of the rotation of the earth
    private fun ephemerisCorrection(time: Double): Double {
        val year = getGregorianYear(time)
        return (CorrectionAlgorithm.entries.firstOrNull { it.lowestYear <= year }
            ?: CorrectionAlgorithm.Default).ephemerisCorrection(year)
    }

    private fun asDayFraction(longitude: Double): Double = longitude / fullCircleOfArc

    private fun obliquity(julianCenturies: Double): Double =
        polynomialSum(coefficients, julianCenturies)

    // equation-of-time; approximate the difference between apparent solar time and mean solar time
    // formal definition is EOT = GHA - GMHA
    // GHA is the Greenwich Hour Angle of the apparent (actual) Sun
    // GMHA is the Greenwich Mean Hour Angle of the mean (fictitious) Sun
    // http://www.esrl.noaa.gov/gmd/grad/solcalc/
    // http://en.wikipedia.org/wiki/Equation_of_time
    private fun equationOfTime(time: Double): Double {
        val julianCenturies = julianCenturies(time)
        val lambda = polynomialSum(lambdaCoefficients, julianCenturies)
        val anomaly = polynomialSum(anomalyCoefficients, julianCenturies)
        val eccentricity = polynomialSum(eccentricityCoefficients, julianCenturies)
        val epsilon = obliquity(julianCenturies)
        val tanHalfEpsilon: Double = tanOfDegree(epsilon / 2)
        val y = tanHalfEpsilon * tanHalfEpsilon
        val dividend: Double = (y * sinOfDegree(2 * lambda)) -
                (2 * eccentricity * sinOfDegree(anomaly)) +
                (4 * eccentricity * y * sinOfDegree(anomaly) * cosOfDegree(2 * lambda)) -
                (.5 * y.pow(2.0) * sinOfDegree(4 * lambda)) -
                (1.25 * eccentricity.pow(2.0) * sinOfDegree(2 * anomaly))
        val divisor: Double = 2 * PI
        val equation = dividend / divisor

        // approximation of equation of time is not valid for dates that are many millennia in the past or future
        // thus limited to a half day
        return min(abs(equation).withSign(twelveHours), equation)
    }

    private fun asLocalTime(apparentMidday: Double, longitude: Double): Double {
        // slightly inaccurate since equation of time takes mean time not apparent time as its argument, but the difference is negligible
        val universalTime = apparentMidday - asDayFraction(longitude)
        return apparentMidday - equationOfTime(universalTime)
    }

    // midday
    private fun midday(date: Double, longitude: Double): Double =
        asLocalTime(date + twelveHours, longitude) - asDayFraction(longitude)

    // midday-in-tehran 52.5 degrees east, longitude of UTC+3:30 which defines Iranian Standard Time
    private fun middayAtPersianObservationSite(date: Double, oldEra: Boolean): Double =
        midday(date, initLongitude(if (oldEra) 51.423056 else 52.5))

    // https://github.com/catull/calendariale/blob/b281b7bdb37aa6cb575637033d0da72b319dbe55/src/Const.ts#L178

    // persian-new-year-on-or-before
    //  number of days is the absolute date. The absolute date is the number of days from January 1st, 1 A.D.
    //  1/1/0001 is absolute date 1.
    private fun persianNewYearOnOrBefore(numberOfDays: Long, month: Int, oldEra: Boolean): Long {
        val date = numberOfDays.toDouble()
        val monthLongitude = (month - 1) * 30.0
        val approx = estimatePrior(monthLongitude, middayAtPersianObservationSite(date, oldEra))
        val lowerBoundNewYearDay: Long = floor(approx).toLong() - 1
        val upperBoundNewYearDay =
            lowerBoundNewYearDay + 3 // estimate is generally within a day of the actual occurrance (at the limits, the error expands, since the calculations rely on the mean tropical year which changes...)
        var day = lowerBoundNewYearDay
        while (day != upperBoundNewYearDay) {
            val midday = middayAtPersianObservationSite(day.toDouble(), oldEra)
            val l = compute(midday)
            if (l in monthLongitude..monthLongitude + 2) break
            ++day
        }
        // Contract.Assert(day != upperBoundNewYearDay);
        return day - 1
    }

    private enum class CorrectionAlgorithm(
        val lowestYear: Int,
        val ephemerisCorrection: (Int) -> Double,
    ) {
        // the following formulas defines a polynomial function which gives us the amount that the
        // earth is slowing down for specific year ranges
        Default(
            lowestYear = 2020,
            ephemerisCorrection = {
                val january1stOfYear: Long = CivilDate(it, 1, 1).toJdn() - projectJdnOffset
                val daysSinceStartOf1810 = (january1stOfYear - startOf1810).toDouble()
                val x = twelveHours + daysSinceStartOf1810
                (x.pow(2.0) / 41048480 - 15) / secondsPerDay
            },
        ),
        Year1988to2019(
            lowestYear = 1988,
            ephemerisCorrection = {
                (it - 1933.0) / secondsPerDay
            },
        ),
        Year1900to1987(
            lowestYear = 1900,
            ephemerisCorrection = {
                polynomialSum(coefficients1900to1987, centuriesFrom1900(it))
            },
        ),
        Year1800to1899(
            lowestYear = 1800,
            ephemerisCorrection = {
                polynomialSum(coefficients1800to1899, centuriesFrom1900(it))
            },
        ),
        Year1700to1799(
            lowestYear = 1700,
            ephemerisCorrection = {
                polynomialSum(coefficients1700to1799, it - 1700.0) / secondsPerDay
            },
        ),
        Year1620to1699(
            lowestYear = 1620,
            ephemerisCorrection = {
                polynomialSum(coefficients1620to1699, it - 1600.0) / secondsPerDay
            },
        ),
    }
}
