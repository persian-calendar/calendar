// Ported from
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/PersianCalendar.cs
// https://github.com/dotnet/runtime/blob/4f4af6b5/src/libraries/System.Private.CoreLib/src/System/Globalization/CalendricalCalculationsHelper.cs#L8
// Which is released under The MIT License (MIT)
package io.github.persiancalendar.calendar.persian

import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.util.toRadians
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.withSign

internal object AlgorithmicConverter {
    private const val projectJdnOffset: Long =
        1721426 // Offset from Jdn to jdn used in this converter
    private const val persianEpoch: Long = 226895 // new DateTime(622, 3, 22).Ticks / TicksPerDay
    private const val meanTropicalYearInDays = 365.242189
    private const val fullCircleOfArc = 360.0
    private const val meanSpeedOfSun = meanTropicalYearInDays / fullCircleOfArc
    private const val halfCircleOfArc = 180
    private const val twoDegreesAfterSpring = 2.0
    private val daysToMonth = intArrayOf(0, 31, 62, 93, 124, 155, 186, 216, 246, 276, 306, 336, 366)
    private const val noon2000Jan01 = 730120.5
    private const val daysInUniformLengthCentury = 36525
    private val startOf1810: Long = CivilDate(1810, 1, 1).toJdn() - projectJdnOffset
    private val startOf1900Century: Long = CivilDate(1900, 1, 1).toJdn() - projectJdnOffset
    private const val twelveHours = .5 // half a day
    private const val secondsPerDay = 24 * 60 * 60 // 24 hours * 60 minutes * 60 seconds
    private const val secondsPerMinute = 60
    private const val minutesPerDegree = 60
    private val coefficients1900to1987 = doubleArrayOf(
        -0.00002, 0.000297, 0.025184, -0.181133, 0.553040, -0.861938, 0.677066, -0.212591
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
        2.043794
    )
    private val coefficients1700to1799 =
        doubleArrayOf(8.118780842, -0.005092142, 0.003336121, -0.0000266484)
    private val coefficients1620to1699 = doubleArrayOf(196.58333, -4.0675, 0.0219167)
    private val lambdaCoefficients = doubleArrayOf(280.46645, 36000.76983, 0.0003032)
    private val anomalyCoefficients = doubleArrayOf(357.52910, 35999.05030, -0.0001559, -0.00000048)
    private val eccentricityCoefficients = doubleArrayOf(0.016708617, -0.000042037, -0.0000001236)
    private val coefficients = doubleArrayOf(
        angle(23, 26, 21.448), angle(0, 0, -46.8150), angle(0, 0, -0.00059), angle(0, 0, 0.001813)
    )
    private val coefficientsA = doubleArrayOf(124.90, -1934.134, 0.002063)
    private val coefficientsB = doubleArrayOf(201.11, 72001.5377, 0.00057)

    // lowest year that starts algorithm, algorithm to use
    private val ephemerisCorrectionTable = listOf(
        EphemerisCorrectionAlgorithmMap(2020, CorrectionAlgorithm.Default),
        EphemerisCorrectionAlgorithmMap(1988, CorrectionAlgorithm.Year1988to2019),
        EphemerisCorrectionAlgorithmMap(1900, CorrectionAlgorithm.Year1900to1987),
        EphemerisCorrectionAlgorithmMap(1800, CorrectionAlgorithm.Year1800to1899),
        EphemerisCorrectionAlgorithmMap(1700, CorrectionAlgorithm.Year1700to1799),
        EphemerisCorrectionAlgorithmMap(1620, CorrectionAlgorithm.Year1620to1699),
        EphemerisCorrectionAlgorithmMap(
            Int.MIN_VALUE, CorrectionAlgorithm.Default
        ) // default must be last
    )

    private const val longitudeSpring = .0
    fun toJdn(year: Int, month: Int, day: Int): Long {
        val approximateHalfYear = 180
        val ordinalDay =
            daysInPreviousMonths(month) + day - 1 // day is one based, make 0 based since this will be the number of days we add to beginning of year below
        val approximateDaysFromEpochForYearStart = (meanTropicalYearInDays * (year - 1)).toInt()
        var yearStart =
            persianNewYearOnOrBefore(persianEpoch + approximateDaysFromEpochForYearStart + approximateHalfYear)
        yearStart += ordinalDay.toLong()
        return yearStart + projectJdnOffset
    }

    fun fromJdn(jdn: Long): IntArray {
        var jdn = jdn
        jdn++ // TODO: Investigate why this is needed
        val yearStart = persianNewYearOnOrBefore(jdn - projectJdnOffset)
        val y: Int = floor((yearStart - persianEpoch) / meanTropicalYearInDays + 0.5).toInt() + 1
        val ordinalDay = (jdn - toJdn(y, 1, 1)).toInt()
        val m = monthFromOrdinalDay(ordinalDay)
        val d = ordinalDay - daysInPreviousMonths(m)
        return intArrayOf(y, m, d)
    }

    private fun daysInPreviousMonths(month: Int): Int = daysToMonth[month - 1]

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
        val lambda =
            282.7771834 + 36000.76953744 * julianCenturies + 0.000005729577951308232 * sumLongSequenceOfPeriodicTerms(
                julianCenturies
            )
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
        return -0.004778 * sin(a.toRadians()) - 0.0003667 * sin(b.toRadians())
    }

    private fun aberration(julianCenturies: Double): Double =
        0.0000974 * cos((177.63 + 35999.01848 * julianCenturies).toRadians()) - 0.005575

    private fun periodicTerm(julianCenturies: Double, x: Int, y: Double, z: Double): Double =
        x * sin((y + z * julianCenturies).toRadians())

    private fun sumLongSequenceOfPeriodicTerms(julianCenturies: Double): Double {
        val terms = listOf(
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
        var sum = .0
        for ((a, b, c) in terms) {
            sum += periodicTerm(julianCenturies, a, b, c)
        }
        return sum
    }

    private fun julianCenturies(moment: Double): Double {
        val dynamicalMoment = moment + ephemerisCorrection(moment)
        return (dynamicalMoment - noon2000Jan01) / daysInUniformLengthCentury
    }

    // the following formulas defines a polynomial function which gives us the amount that the earth is slowing down for specific year ranges
    private fun defaultEphemerisCorrection(gregorianYear: Int): Double {
        // Contract.Assert(gregorianYear < 1620 || 2020 <= gregorianYear);
        val january1stOfYear: Long = CivilDate(gregorianYear, 1, 1).toJdn() - projectJdnOffset
        val daysSinceStartOf1810 = (january1stOfYear - startOf1810).toDouble()
        val x = twelveHours + daysSinceStartOf1810
        return (x.pow(2.0) / 41048480 - 15) / secondsPerDay
    }

    private fun ephemerisCorrection1988to2019(gregorianYear: Int): Double {
        // Contract.Assert(1988 <= gregorianYear && gregorianYear <= 2019);
        return (gregorianYear - 1933).toDouble() / secondsPerDay
    }

    private fun centuriesFrom1900(gregorianYear: Int): Double {
        val july1stOfYear: Long = CivilDate(gregorianYear, 7, 1).toJdn() - projectJdnOffset
        return (july1stOfYear - startOf1900Century).toDouble() / daysInUniformLengthCentury
    }

    private fun angle(degrees: Int, minutes: Int, seconds: Double): Double =
        (seconds / secondsPerMinute + minutes) / minutesPerDegree + degrees

    private fun ephemerisCorrection1900to1987(gregorianYear: Int): Double {
        // Contract.Assert(1900 <= gregorianYear && gregorianYear <= 1987);
        val centuriesFrom1900 = centuriesFrom1900(gregorianYear)
        return polynomialSum(coefficients1900to1987, centuriesFrom1900)
    }

    private fun ephemerisCorrection1800to1899(gregorianYear: Int): Double {
        // Contract.Assert(1800 <= gregorianYear && gregorianYear <= 1899);
        val centuriesFrom1900 = centuriesFrom1900(gregorianYear)
        return polynomialSum(coefficients1800to1899, centuriesFrom1900)
    }

    private fun ephemerisCorrection1700to1799(gregorianYear: Int): Double {
        // Contract.Assert(1700 <= gregorianYear && gregorianYear <= 1799);
        val yearsSince1700 = (gregorianYear - 1700).toDouble()
        return polynomialSum(coefficients1700to1799, yearsSince1700) / secondsPerDay
    }

    private fun ephemerisCorrection1620to1699(gregorianYear: Int): Double {
        // Contract.Assert(1620 <= gregorianYear && gregorianYear <= 1699);
        val yearsSince1600 = (gregorianYear - 1600).toDouble()
        return polynomialSum(coefficients1620to1699, yearsSince1600) / secondsPerDay
    }

    private fun getGregorianYear(numberOfDays: Double): Int =
        CivilDate(floor(numberOfDays).toLong() + projectJdnOffset).year

    // ephemeris-correction: correction to account for the slowing down of the rotation of the earth
    private fun ephemerisCorrection(time: Double): Double {
        val year = getGregorianYear(time)
        for (map in ephemerisCorrectionTable) {
            if (map.lowestYear <= year) {
                return when (map.algorithm) {
                    CorrectionAlgorithm.Default -> defaultEphemerisCorrection(year)
                    CorrectionAlgorithm.Year1988to2019 -> ephemerisCorrection1988to2019(year)
                    CorrectionAlgorithm.Year1900to1987 -> ephemerisCorrection1900to1987(year)
                    CorrectionAlgorithm.Year1800to1899 -> ephemerisCorrection1800to1899(year)
                    CorrectionAlgorithm.Year1700to1799 -> ephemerisCorrection1700to1799(year)
                    CorrectionAlgorithm.Year1620to1699 -> ephemerisCorrection1620to1699(year)
                }
                break // break the loop and assert eventually
            }
        }

        // Contract.Assert(false, "Not expected to come here");
        return defaultEphemerisCorrection(year)
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
        val tanHalfEpsilon: Double = tan((epsilon / 2).toRadians())
        val y = tanHalfEpsilon * tanHalfEpsilon
        val dividend: Double = y * sin((2 * lambda).toRadians()) -
                2 * eccentricity * sin(anomaly.toRadians()) +
                4 * eccentricity * y * sin(anomaly.toRadians()) * cos((2 * lambda).toRadians()) -
                .5 * y.pow(2.0) * sin((4 * lambda).toRadians()) -
                1.25 * eccentricity.pow(2.0) * sin((2 * anomaly).toRadians())
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

    // midday-in-tehran
    private fun middayAtPersianObservationSite(date: Double): Double {
        return midday(
            date,
            initLongitude(52.5)
        ) // 52.5 degrees east - longitude of UTC+3:30 which defines Iranian Standard Time
    }

    // persian-new-year-on-or-before
    //  number of days is the absolute date. The absolute date is the number of days from January 1st, 1 A.D.
    //  1/1/0001 is absolute date 1.
    private fun persianNewYearOnOrBefore(numberOfDays: Long): Long {
        val date = numberOfDays.toDouble()
        val approx = estimatePrior(longitudeSpring, middayAtPersianObservationSite(date))
        val lowerBoundNewYearDay: Long = floor(approx).toLong() - 1
        val upperBoundNewYearDay =
            lowerBoundNewYearDay + 3 // estimate is generally within a day of the actual occurrance (at the limits, the error expands, since the calculations rely on the mean tropical year which changes...)
        var day = lowerBoundNewYearDay
        while (day != upperBoundNewYearDay) {
            val midday = middayAtPersianObservationSite(day.toDouble())
            val l = compute(midday)
            if (l in longitudeSpring..twoDegreesAfterSpring) break
            ++day
        }
        // Contract.Assert(day != upperBoundNewYearDay);
        return day - 1
    }

    private fun monthFromOrdinalDay(ordinalDay: Int): Int {
        var index = 0
        while (ordinalDay > daysToMonth[index]) index++
        return index
    }

    private enum class CorrectionAlgorithm {
        Default,
        Year1988to2019,
        Year1900to1987,
        Year1800to1899,
        Year1700to1799,
        Year1620to1699
    }

    private data class EphemerisCorrectionAlgorithmMap(
        val lowestYear: Int,
        val algorithm: CorrectionAlgorithm
    )
}
