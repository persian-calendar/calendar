package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.BooksTests.Companion.weekDay
import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import io.github.persiancalendar.calendar.util.fixedFromGregorian
import io.github.persiancalendar.calendar.util.fixedFromJulian
import io.github.persiancalendar.calendar.util.gregorianFromFixed
import io.github.persiancalendar.calendar.util.julianFromFixed
import io.github.persiancalendar.calendar.util.julianFromJdn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTests {

    @Test
    fun `Islamic converter test`() {
        listOf(
            listOf(2453767, 1427, 1, 1), listOf(2455658, 1432, 5, 2)
//            listOf(2458579, 1440, 7, 29), listOf(2458580, 1440, 8, 1)
        ).forEach {
            val reference = IslamicDate(it[1], it[2], it[3])
            assertEquals(it[0].toLong(), reference.toJdn())
            val converted = IslamicDate(it[0].toLong())

            assertEquals(it[1], converted.year)
            assertEquals(it[2], converted.month)
            assertEquals(it[3], converted.dayOfMonth)

            assertEquals(it[0].toLong(), converted.toJdn())
            assertEquals(reference, IslamicDate(reference.toJdn()))
        }

        listOf(
            listOf(2016, 10, 3, 1438, 1, 1),
            listOf(2016, 11, 1, 1438, 2, 1),
            listOf(2016, 12, 1, 1438, 3, 1),
            listOf(2016, 12, 31, 1438, 4, 1),
            listOf(2016, 10, 3, 1438, 1, 1),
            listOf(2016, 11, 1, 1438, 2, 1),
            listOf(2016, 12, 1, 1438, 3, 1),
            listOf(2016, 12, 31, 1438, 4, 1),
            listOf(2017, 1, 30, 1438, 5, 1),
            listOf(2017, 2, 28, 1438, 6, 1),
            listOf(2017, 3, 30, 1438, 7, 1),
            listOf(2017, 4, 28, 1438, 8, 1),
            listOf(2017, 5, 27, 1438, 9, 1),
            listOf(2017, 6, 26, 1438, 10, 1),
            listOf(2017, 7, 25, 1438, 11, 1),
            listOf(2017, 8, 23, 1438, 12, 1),
            listOf(2017, 9, 22, 1439, 1, 1),
            listOf(2017, 10, 21, 1439, 2, 1),
            listOf(2017, 11, 20, 1439, 3, 1),
            listOf(2017, 12, 20, 1439, 4, 1),
            listOf(2018, 1, 19, 1439, 5, 1),
            listOf(2018, 2, 18, 1439, 6, 1),
            listOf(2018, 3, 19, 1439, 7, 1),
            listOf(2018, 4, 18, 1439, 8, 1),
            listOf(2018, 5, 17, 1439, 9, 1),
            listOf(2018, 6, 15, 1439, 10, 1),
            listOf(2018, 7, 15, 1439, 11, 1),
            listOf(2018, 8, 13, 1439, 12, 1),
            listOf(2018, 9, 11, 1440, 1, 1),
            listOf(2018, 10, 11, 1440, 2, 1),
            listOf(2018, 11, 9, 1440, 3, 1),
            listOf(2018, 12, 9, 1440, 4, 1),
            listOf(2019, 1, 8, 1440, 5, 1),
            listOf(2019, 2, 7, 1440, 6, 1)
//            listOf(2040, 5, 12, 1462, 5, 1),
//            listOf(2040, 6, 11, 1462, 6, 1),
//            listOf(2040, 7, 10, 1462, 7, 1),
//            listOf(2040, 8, 9, 1462, 8, 1),
//            listOf(2040, 9, 7, 1462, 9, 1),
//            listOf(2040, 10, 7, 1462, 10, 1),
//            listOf(2040, 11, 6, 1462, 11, 1),
//            listOf(2040, 12, 5, 1462, 12, 1),
//            listOf(2041, 1, 4, 1463, 1, 1),
//            listOf(2041, 2, 2, 1463, 2, 1),
//            listOf(2041, 3, 4, 1463, 3, 1),
//            listOf(2041, 4, 2, 1463, 4, 1),
//            listOf(2041, 5, 1, 1463, 5, 1),
//            listOf(2041, 5, 31, 1463, 6, 1),
//            listOf(2041, 6, 29, 1463, 7, 1),
//            listOf(2041, 7, 29, 1463, 8, 1),
//            listOf(2041, 8, 28, 1463, 9, 1),
//            listOf(2041, 9, 26, 1463, 10, 1),
//            listOf(2041, 10, 26, 1463, 11, 1),
//            listOf(2041, 11, 25, 1463, 12, 1),
//            listOf(2041, 12, 24, 1464, 1, 1)
        ).forEach {
            val jdn = CivilDate(it[0], it[1], it[2]).toJdn()
            val islamicDate = IslamicDate(it[3], it[4], it[5])

            assertEquals(jdn, islamicDate.toJdn())
            assertEquals(islamicDate, IslamicDate(jdn))
        }

        IslamicDate.useUmmAlQura = true
        listOf(
            listOf(listOf(2015, 3, 14), listOf(1436, 5, 23)),
            listOf(listOf(1999, 4, 1), listOf(1419, 12, 15)),
            listOf(listOf(1989, 2, 25), listOf(1409, 7, 19))
        ).forEach {
            val jdn = CivilDate(it[0][0], it[0][1], it[0][2]).toJdn()
            val islamicDate = IslamicDate(it[1][0], it[1][1], it[1][2])

            assertEquals(jdn, islamicDate.toJdn())
            assertEquals(islamicDate, IslamicDate(jdn))
        }
        IslamicDate.useUmmAlQura = false

        //        int i = -1;
        //        long last = 0;
        //        for (int[][] test : tests2) {
        //            if (i % 12 == 0) {
        //                System.out.print(test[1][0]);
        //                System.out.print(", ");
        //            }
        //            long jdn = DateConverter.toJdn(test[0][0], test[0][1], test[0][2]);
        //            System.out.print(jdn - last);
        //            last = jdn;
        //            System.out.print(", ");
        //            if (i % 12 == 11)
        //                System.out.print("\n");
        //            ++i;
        //        }

        // https://imgur.com/a/AbIvGVT
        assertEquals(
            PersianDate(1367, 1, 25).toJdn(),
            IslamicDate(1408, 8, 26).toJdn()
        )
    }

    private val startJdn = CivilDate(1250, 1, 1).toJdn()
    private val endJdn = CivilDate(2350, 1, 1).toJdn()

    @Test
    fun `Practice Persian converting back and forth`() {
        assertEquals(PersianDate(1398, 1, 1).toJdn(), 2458564)
        (startJdn..endJdn).map {
            val date = PersianDate(it)
            assertEquals(
                expected = it,
                actual = date.toJdn(),
                message = "$it: ${date.year}/${date.month}/${date.dayOfMonth}"
            )
            assertTrue(date.month in 1..12)
            if (date.year > 1303) assertTrue(
                date.dayOfMonth in 1..if (date.month in 1..6) 31 else 30,
                date.year.toString()
            )
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(29..32)
    }

    @Test
    fun `Practice Islamic converting back and forth`() {
        val startJdn = CivilDate(1600, 1, 1).toJdn()
        val endJdn = CivilDate(2200, 1, 1).toJdn()
        (startJdn..endJdn).map {
            val date = IslamicDate(it)
            assertEquals(
                it,
                date.toJdn(),
                "$it: ${date.year}/${date.month}/${date.dayOfMonth}"
            )
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30, date.toString())
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(29..30)
    }

    @Test
    fun `Practice UmmAlqara converting back and forth`() {
        val startJdn = CivilDate(1950, 1, 1).toJdn()
        val endJdn = CivilDate(2150, 1, 1).toJdn()
        IslamicDate.useUmmAlQura = true
        (startJdn..endJdn).map {
            val date = IslamicDate(it)
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(29..30)
        IslamicDate.useUmmAlQura = false
    }

    @Test
    fun `Practice CivilDate converting back and forth`() {
        (startJdn..endJdn).mapNotNull {
            val date = CivilDate(it)
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..31)
            if (date.year == 1582 && date.month == 10) null else date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(28..31)
    }

    @Test
    fun `Practice Nepali converting back and forth`() {
        (startJdn..endJdn).mapNotNull {
            val date = NepaliDate(it)
            assertEquals(it, date.toJdn(), CivilDate(it).run { "$year/$month/$dayOfMonth" })
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..32)
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(29..32)
    }

    @Test
    fun `Practice Gregorian converting back and forth`() {
        (startJdn..endJdn).map {
            val date = gregorianFromFixed(it.toInt())
            val convertedBack = fixedFromGregorian(date.year, date.month, date.dayOfMonth)
            assertEquals(it.toInt(), convertedBack)
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(28..32)
    }

    @Test
    fun `Practice Julian converting back and forth`() {
        (startJdn..endJdn).map {
            val date = julianFromFixed(it.toInt())
            val convertedBack = fixedFromJulian(date.year, date.month, date.dayOfMonth)
            assertEquals(it.toInt(), convertedBack)
            date.dayOfMonth
        }.ensureContinuity().ensureValidMonthLengths(28..32)
    }

    // This gets a list of day of months and ensures they are either in increasing order or are 1
    private fun List<Int>.ensureContinuity(): List<Int> {
        this.reduce { previousDayOfMonth: Int, dayOfMonth: Int ->
            assertTrue(dayOfMonth == 1 || dayOfMonth == previousDayOfMonth + 1)
            dayOfMonth
        }
        return this
    }

    // This gets a list of day of months and ensures they are either in increasing order or are 1
    private fun List<Int>.ensureValidMonthLengths(validMonthLengthRange: IntRange): List<Int> {
        this.reduce { previousDayOfMonth: Int, dayOfMonth: Int ->
            if (dayOfMonth == 1) assertTrue(
                previousDayOfMonth in validMonthLengthRange,
                "$previousDayOfMonth"
            )
            dayOfMonth
        }
        return this
    }

    @Test
    fun `Test month distance methods`() {
        run {
            val date = CivilDate(2014, 7, 7)
            assertEquals(0, date.monthsDistanceTo(date))
            assertEquals(-10, date.monthsDistanceTo(CivilDate(2013, 9, 5)))
            assertEquals(30, date.monthsDistanceTo(CivilDate(2017, 1, 7)))
            assertEquals(CivilDate(2013, 12, 1), date.monthStartOfMonthsDistance(-7))
            assertEquals(CivilDate(2014, 6, 1), date.monthStartOfMonthsDistance(-1))
            assertEquals(CivilDate(2014, 7, 1), date.monthStartOfMonthsDistance(0))
            assertEquals(CivilDate(2014, 8, 1), date.monthStartOfMonthsDistance(1))
            assertEquals(CivilDate(2015, 1, 1), date.monthStartOfMonthsDistance(6))
        }

        (-40..40).forEach {
            val date = PersianDate(1318, 2, 5)
            val dateWithDistance = date.monthStartOfMonthsDistance(it)
            assertEquals(it, date.monthsDistanceTo(dateWithDistance))
        }
        (-40..40).forEach {
            val date = CivilDate(2016, 7, 26)
            val dateWithDistance = date.monthStartOfMonthsDistance(it)
            assertEquals(it, date.monthsDistanceTo(dateWithDistance))
        }
        (-40..40).forEach {
            val date = IslamicDate(1440, 7, 26)
            val dateWithDistance = date.monthStartOfMonthsDistance(it)
            assertEquals(it, date.monthsDistanceTo(dateWithDistance))
        }
    }

    @Test
    fun `Islamic Calendar Conversions of 1401`() {
        val dates = listOf(
            listOf(1401, 1, 14, 1443, 9, 1),
            listOf(1401, 2, 13, 1443, 10, 1),
            listOf(1401, 3, 11, 1443, 11, 1),
            listOf(1401, 4, 10, 1443, 12, 1),
            listOf(1401, 5, 8, 1444, 1, 1),
            listOf(1401, 6, 7, 1444, 2, 1),
            listOf(1401, 7, 6, 1444, 3, 1),
            listOf(1401, 8, 5, 1444, 4, 1),
            listOf(1401, 9, 5, 1444, 5, 1),
            listOf(1401, 10, 4, 1444, 6, 1),
            listOf(1401, 11, 3, 1444, 7, 1),
            listOf(1401, 12, 3, 1444, 8, 1),
        )
        assertEquals(dates.count { it[0] == 1401 }, 12)
        dates.forEach {
            assertEquals(
                PersianDate(it[0], it[1], it[2]).toJdn(), IslamicDate(it[3], it[4], it[5]).toJdn()
            )
        }
    }

    @Test
    fun `Islamic Calendar Conversions of 1404`() {
        val dates = listOf(
            listOf(1404, 1, 11, 1446, 10, 1),
            listOf(1404, 2, 9, 1446, 11, 1),
            listOf(1404, 3, 7, 1446, 12, 1),
            listOf(1404, 4, 6, 1447, 1, 1),
            listOf(1404, 5, 4, 1447, 2, 1),
            listOf(1404, 6, 3, 1447, 3, 1),
            listOf(1404, 7, 2, 1447, 4, 1),
            listOf(1404, 8, 1, 1447, 5, 1),
            listOf(1404, 9, 1, 1447, 6, 1),
            listOf(1404, 10, 1, 1447, 7, 1),
            listOf(1404, 11, 1, 1447, 8, 1),
            listOf(1404, 11, 30, 1447, 9, 1),
            listOf(1405, 1, 1, 1447, 10, 1),
        )
        assertEquals(dates.count { it[0] == 1404 }, 12)
        dates.forEach {
            assertEquals(
                PersianDate(it[0], it[1], it[2]).toJdn(), IslamicDate(it[3], it[4], it[5]).toJdn()
            )
        }
    }

    // https://www.pcci.ir/ershad_content/media/image/2025/10/%D8%AA%D9%82%D9%88%DB%8C%D9%85%201405_1633718.pdf
    @Test
    fun `Supported Year Islamic Calendar Conversions`() {
        val dates = listOf(
            listOf(1405, 1, 1, 1447, 10, 1),
            listOf(1405, 1, 30, 1447, 11, 1),
            listOf(1405, 2, 28, 1447, 12, 1),
            listOf(1405, 3, 26, 1448, 1, 1),
            listOf(1405, 4, 25, 1448, 2, 1),
            listOf(1405, 5, 23, 1448, 3, 1),
            listOf(1405, 6, 22, 1448, 4, 1),
            listOf(1405, 7, 20, 1448, 5, 1),
            listOf(1405, 8, 20, 1448, 6, 1),
            listOf(1405, 9, 20, 1448, 7, 1),
            listOf(1405, 10, 20, 1448, 8, 1),
            listOf(1405, 11, 19, 1448, 9, 1),
            listOf(1405, 12, 19, 1448, 10, 1),
        )
        assertEquals(
            expected = dates.count { it[0] == IranianIslamicDateConverter.latestSupportedYearOfIran },
            actual = 13,
        )
        dates.map {
            {
                val persianDate = PersianDate(it[0], it[1], it[2])
                val islamicDate = IslamicDate(it[3], it[4], it[5])
                assertEquals(
                    persianDate.toJdn(),
                    islamicDate.toJdn(),
                    "$persianDate-$islamicDate",
                )
            }
        }.let(::assertAll)
    }

    @Test
    fun `Use constructor for date conversion`() {
        assertEquals(
            PersianDate(NepaliDate(CivilDate(IslamicDate(PersianDate(1400, 1, 1))))),
            PersianDate(1400, 1, 1)
        )
    }

//    @Test
//    fun `Test Chinese calendar`() {
//        val inputJdn = CivilDate(2022, 3, 3).toJdn()
//        assertEquals(2459642, inputJdn)
//        val chineseDate = ChineseDate.fromJdn(inputJdn)
//        assertEquals(2022, chineseDate.year)
//        assertEquals(2, chineseDate.month)
//        assertEquals(false, chineseDate.leapMonth)
//        assertEquals(1, chineseDate.day)
//        assertEquals(2459642, chineseDate.toJdn())
//
//        // It crashes! Oh well
//        // (2459000L..24600000L).forEach {
//        //     assertEquals(it, ChineseDate.fromJdn(it).toJdn())
//        // }
//    }

    @Test
    fun `Misc tests`() {
        // https://t.me/khoshnevisi_Qodama/29080 or https://archive.is/4CDPj
        // Provided by Mahmoud Arasteh what is actually written is
        // "شب شنبه پنجم محرم یکهزار و سیصد" but "شب شنبه" means Friday
        // https://t.me/khoshnevisi_Qodama/29080 archived at https://archive.is/4CDPj
        assertEquals("جمعه", IslamicDate(1300, 1, 5).weekDay)
        // https://t.me/Calligraphy_Archive/1364 archived at https://archive.is/fCfv9
        assertEquals("جمعه", IslamicDate(1294, 1, 25).weekDay)
        // Doesn't match right now :/
        //// https://t.me/Calligraphy_Archive/345 archived at https://archive.is/8Az2y
//        assertEquals("پنجشنبه", IslamicDate(1289, 3, 7).weekDay)
        // https://t.me/Calligraphy_Archive/1384 archived at https://archive.is/S3ncG
        assertEquals("یکشنبه", IslamicDate(1336, 12, 15).weekDay)
    }

    @Test
    fun `Julian date`() {
        assertEquals(
            // There is "۸ آذر" in the first page of سالنمای فارسی مصباح‌زاده"
            DateTriplet(2025, 3, 8),
            julianFromJdn(PersianDate(1404, 1, 1).toJdn())
        )
    }
}
