package io.github.persiancalendar.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
            assertTrue(reference == IslamicDate(reference.toJdn()))
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
            assertTrue(islamicDate == IslamicDate(jdn))
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
            assertTrue(islamicDate == IslamicDate(jdn))
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
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..if (date.month in 1..6) 31 else 30)
            date.dayOfMonth
        }.ensureContinuity()
    }

    @Test
    fun `Practice Islamic converting back and forth`() {
        (startJdn..endJdn).map {
            val date = IslamicDate(it)
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
            date.dayOfMonth
        }.ensureContinuity()
    }

    @Test
    fun `Practice UmmAlqara converting back and forth`() {
        IslamicDate.useUmmAlQura = true
        (startJdn..endJdn).map {
            val date = IslamicDate(it)
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..30)
            date.dayOfMonth
        }.ensureContinuity()
        IslamicDate.useUmmAlQura = false
    }

    @Test
    fun `Practice Gregorian converting back and forth`() {
        (startJdn..endJdn).mapNotNull {
            val date = CivilDate(it)
            assertEquals(it, date.toJdn())
            assertTrue(date.month in 1..12)
            assertTrue(date.dayOfMonth in 1..31)
            if (date.year == 1582 && date.month == 10) null else date.dayOfMonth
        }.ensureContinuity()
    }

    private fun List<Int>.ensureContinuity() = this.reduce { previousDayOfMonth, dayOfMonth ->
        assertTrue(dayOfMonth == 1 || dayOfMonth == previousDayOfMonth + 1)
        dayOfMonth
    }.let {}

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
}