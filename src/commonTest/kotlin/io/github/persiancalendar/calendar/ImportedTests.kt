package io.github.persiancalendar.calendar

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals
import kotlin.test.fail

class ImportedTests : FunSpec({

    test("Conforms with officially published leap years") {
        // Doesn't match with https://calendar.ut.ac.ir/Fa/News/Data/Doc/KabiseShamsi1206-1498-new.pdf
        val leapYears = listOf(
            1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243, 1247, 1251, 1255, 1259, 1263,
            1267, 1271, 1276, 1280, 1284, 1288, 1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321,
            1325, 1329, 1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
            1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424, 1428, 1432, 1436,
            1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469, 1474, 1478, 1482, 1486, 1490, 1494,
            1498
        )

        (1206..1498).forEach {
            val yearLength = PersianDate(it + 1, 1, 1).toJdn() - PersianDate(it, 1, 1).toJdn()
            assertEquals(if (it in leapYears) 366 else 365, yearLength, it.toString())
        }
    }

    test("Partially conforming with calendariale tests") {
        val J0000 = 1721425L // Ours is different apparently
        listOf(
//        listOf(-214193, -1208, 5, 1),
//        listOf(-61387, -790, 9, 14),
//        listOf(25469, -552, 7, 2),
//        listOf(49217, -487, 7, 9),
//        listOf(171307, -153, 10, 18),
//        listOf(210155, -46, 2, 30),
            listOf(253428, 73, 8, 19),
            listOf(369740, 392, 2, 5),
//            listOf(400085, 475, 3, 3),
            listOf(434355, 569, 1, 3),
            listOf(452604, 618, 12, 20),
            listOf(470160, 667, 1, 14),
            listOf(473837, 677, 2, 8),
            listOf(507850, 770, 3, 22),
            listOf(524155, 814, 11, 13),
            listOf(544676, 871, 1, 21),
            listOf(567119, 932, 6, 28),
            listOf(569476, 938, 12, 14),
            listOf(601716, 1027, 3, 21),
            listOf(613424, 1059, 4, 10),
            listOf(626596, 1095, 5, 2),
            listOf(645554, 1147, 3, 30),
            listOf(664224, 1198, 5, 10),
            listOf(671401, 1218, 1, 7),
            listOf(694799, 1282, 1, 29),
            listOf(704424, 1308, 6, 3),
            listOf(708842, 1320, 7, 7),
            listOf(709409, 1322, 1, 29),
            listOf(709580, 1322, 7, 14),
            listOf(727274, 1370, 12, 27),
            listOf(728714, 1374, 12, 6),
            listOf(744313, 1417, 8, 19),
            listOf(764652, 1473, 4, 28)
        ).forEach {
            assertEquals(it[0] + J0000, PersianDate(it[1], it[2], it[3]).toJdn(), it.toString())
            val from = PersianDate(it[0] + J0000)
            assertEquals(it[1], from.year)
            assertEquals(it[2], from.month)
            assertEquals(it[3], from.dayOfMonth)
        }

        listOf(
//        listOf(1507231, -586, 7, 24),
//        listOf(1660037, -168, 12, 5),
//        listOf(1746893, 70, 9, 24),
//        listOf(1770641, 135, 10, 2),
//        listOf(1892731, 470, 1, 8),
//        listOf(1931579, 576, 5, 20),
//        listOf(1974851, 694, 11, 10),
//        listOf(2091164, 1013, 4, 25),
//        listOf(2121509, 1096, 5, 24),
//        listOf(2155779, 1190, 3, 23),
//        listOf(2174029, 1240, 3, 10),
//        listOf(2191584, 1288, 4, 2),
//        listOf(2195261, 1298, 4, 27),
//        listOf(2229274, 1391, 6, 12),
//        listOf(2245580, 1436, 2, 3),
//        listOf(2266100, 1492, 4, 9),
//        listOf(2288542, 1553, 9, 19),
//        listOf(2290901, 1560, 3, 5),
//        listOf(2323140, 1648, 6, 10),
            listOf(2334848, 1680, 6, 30),
            listOf(2348020, 1716, 7, 24),
            listOf(2366978, 1768, 6, 19),
            listOf(2385648, 1819, 8, 2),
            listOf(2392825, 1839, 3, 27),
            listOf(2416223, 1903, 4, 19),
            listOf(2425848, 1929, 8, 25),
            listOf(2430266, 1941, 9, 29),
            listOf(2430833, 1943, 4, 19),
            listOf(2431004, 1943, 10, 7),
            listOf(2448698, 1992, 3, 17),
            listOf(2450138, 1996, 2, 25),
            listOf(2465737, 2038, 11, 10),
            listOf(2486076, 2094, 7, 18)
        ).forEach {
            assertEquals(it[0] + 1L, CivilDate(it[1], it[2], it[3]).toJdn())
            val from = CivilDate(it[0] + 1L)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }

        listOf(
//        listOf(-214193, -1245, 12, 11),
//        listOf(-61387, -813, 2, 25),
//        listOf(25469, -568, 4, 2),
//        listOf(49217, -501, 4, 7),
//        listOf(171307, -157, 10, 18),
//        listOf(210155, -47, 6, 3),
//        listOf(253427, 75, 7, 13),
//        listOf(369740, 403, 10, 5),
//        listOf(400085, 489, 5, 22),
//        listOf(434355, 586, 2, 7),
            listOf(452605, 637, 8, 7),
//        listOf(470160, 687, 2, 21),
//        listOf(473837, 697, 7, 7),
//        listOf(507850, 793, 6, 30),
//        listOf(524156, 839, 7, 6),
//        listOf(544676, 897, 6, 2),
//        listOf(567118, 960, 9, 30),
//        listOf(569477, 967, 5, 27),
//        listOf(601716, 1058, 5, 18),
//        listOf(613424, 1091, 6, 3),
//        listOf(626596, 1128, 8, 4),
            listOf(645554, 1182, 2, 4),
//        listOf(664224, 1234, 10, 10),
//        listOf(671401, 1255, 1, 11),
//        listOf(694799, 1321, 1, 20),
            listOf(704424, 1348, 3, 19),
//        listOf(708842, 1360, 9, 7),
//        listOf(709409, 1362, 4, 14),
//        listOf(709580, 1362, 10, 7),
//        listOf(727274, 1412, 9, 12),
//        listOf(728714, 1416, 10, 5),
//        listOf(744313, 1460, 10, 12),
            listOf(764652, 1518, 3, 5)
        ).forEach {
            assertEquals(it[0] + J0000, IslamicDate(it[1], it[2], it[3]).toJdn(), "${it[0]}")
            val from = IslamicDate(it[0] + J0000)
            assertEquals(from.year, it[1])
            assertEquals(from.month, it[2])
            assertEquals(from.dayOfMonth, it[3])
        }
    }

    val persianDates = listOf(
        // Persian year, Persian Month, Persian Day, Gregorian Year, Gregorian Month, Gregorian Day
        // listOf(1, 1, 1, 622, 3, 22), TODO: Make these work, probably the Gregorian calendar side is broken
        // listOf(101, 2, 2, 722, 4, 23),
        // listOf(201, 3, 3, 822, 5, 24),
        // listOf(301, 4, 4, 922, 6, 25),
        // listOf(401, 5, 5, 1022, 7, 27),
        // listOf(501, 6, 6, 1122, 8, 29),
        // listOf(601, 7, 7, 1222, 9, 29),
        // listOf(701, 8, 8, 1322, 10, 30),
        // listOf(801, 9, 9, 1422, 11, 30),
        // listOf(901, 10, 10, 1523, 1, 1),
        listOf(1001, 11, 11, 1623, 1, 30),
        listOf(1101, 12, 12, 1723, 3, 2),
        listOf(1202, 1, 13, 1823, 4, 3),
//        listOf(1302, 2, 14, 1923, 5, 5),
        listOf(1402, 3, 15, 2023, 6, 5),
        listOf(1502, 4, 16, 2123, 7, 7),
        listOf(1602, 5, 17, 2223, 8, 8),
        listOf(1702, 6, 18, 2323, 9, 10),
        listOf(1802, 7, 19, 2423, 10, 11),
        listOf(1902, 8, 20, 2523, 11, 11),
        listOf(2002, 9, 21, 2623, 12, 12),
        listOf(2102, 10, 22, 2724, 1, 13),
        listOf(2202, 11, 23, 2824, 2, 12),
        listOf(2302, 12, 24, 2924, 3, 14),
        listOf(2403, 1, 25, 3024, 4, 14),
        listOf(2503, 2, 26, 3124, 5, 16),
        listOf(2603, 3, 27, 3224, 6, 16),
        listOf(2703, 4, 28, 3324, 7, 18),
        listOf(2803, 5, 29, 3424, 8, 20),
        listOf(2903, 6, 30, 3524, 9, 21),
        listOf(3003, 8, 1, 3624, 10, 22),
        listOf(3103, 9, 2, 3724, 11, 22),
        listOf(3203, 10, 3, 3824, 12, 24),
        listOf(3303, 11, 4, 3925, 1, 24),
        listOf(3403, 12, 5, 4025, 2, 23),
        listOf(3504, 1, 6, 4125, 3, 25),
        listOf(3604, 2, 7, 4225, 4, 27),
        listOf(3704, 3, 8, 4325, 5, 29),
        listOf(3804, 4, 9, 4425, 6, 29),
        listOf(3904, 5, 10, 4525, 7, 31),
        listOf(4004, 6, 11, 4625, 9, 2),
        listOf(4104, 7, 12, 4725, 10, 4),
        listOf(4204, 8, 13, 4825, 11, 3),
        listOf(4304, 9, 14, 4925, 12, 4),
        listOf(4404, 10, 15, 5026, 1, 5),
        listOf(4504, 11, 16, 5126, 2, 5),
        listOf(4604, 12, 17, 5226, 3, 7),
        listOf(4705, 1, 18, 5326, 4, 6),
        listOf(4805, 2, 19, 5426, 5, 9),
        listOf(4905, 3, 20, 5526, 6, 10),
        listOf(5005, 4, 21, 5626, 7, 11),
        listOf(5105, 5, 22, 5726, 8, 12),
        listOf(5205, 6, 23, 5826, 9, 14),
        listOf(5305, 7, 24, 5926, 10, 16),
        listOf(5405, 8, 25, 6026, 11, 15),
        listOf(5505, 9, 26, 6126, 12, 16),
        listOf(5605, 10, 27, 6227, 1, 16),
        listOf(5705, 11, 28, 6327, 2, 17),
        listOf(5805, 12, 29, 6427, 3, 19),
        listOf(5906, 1, 30, 6527, 4, 18),
        listOf(6006, 2, 31, 6627, 5, 21),
        listOf(6106, 4, 1, 6727, 6, 22),
        listOf(6206, 5, 2, 6827, 7, 23),
        listOf(6306, 6, 3, 6927, 8, 24),
        listOf(6406, 7, 4, 7027, 9, 25),
        listOf(6506, 8, 5, 7127, 10, 27),
        listOf(6606, 9, 6, 7227, 11, 26),
        listOf(6706, 10, 7, 7327, 12, 27),
        listOf(6806, 11, 8, 7428, 1, 27),
        listOf(6906, 12, 9, 7528, 2, 27),
        listOf(7007, 1, 10, 7628, 3, 28),
        listOf(7107, 2, 11, 7728, 4, 29),
        listOf(7207, 3, 12, 7828, 5, 31),
        listOf(7307, 4, 13, 7928, 7, 2),
        listOf(7407, 5, 14, 8028, 8, 3),
        listOf(7507, 6, 15, 8128, 9, 4),
        listOf(7607, 7, 16, 8228, 10, 6),
        listOf(7707, 8, 17, 8328, 11, 6),
        listOf(7807, 9, 18, 8428, 12, 6),
        listOf(7907, 10, 19, 8529, 1, 6),
        listOf(8007, 11, 20, 8629, 2, 7),
        listOf(8107, 12, 21, 8729, 3, 10),
        listOf(8208, 1, 22, 8829, 4, 8),
        listOf(8308, 2, 23, 8929, 5, 10),
        listOf(8408, 3, 24, 9029, 6, 11),
        listOf(8508, 4, 25, 9129, 7, 14),
        listOf(8608, 5, 26, 9229, 8, 14),
        listOf(8708, 6, 27, 9329, 9, 15),
        listOf(8808, 7, 28, 9429, 10, 17),
        listOf(8908, 8, 29, 9529, 11, 17),
        listOf(9008, 9, 30, 9629, 12, 17),
        listOf(9108, 11, 1, 9730, 1, 18),
        listOf(9208, 12, 2, 9830, 2, 18),
        listOf(9309, 1, 3, 9930, 3, 20),
        listOf(9378, 10, 13, 9999, 12, 31)
    )

    test("Matches with dotnet tests for Persian calendar") {
        persianDates.forEach { line ->
            assertEquals(
                PersianDate(line[0], line[1], line[2]).toJdn(),
                CivilDate(line[3], line[4], line[5]).toJdn(),
                line.toString()
            )
        }
    }

    test("Test Nepali calendar implementation") {
        // https://github.com/techgaun/ad-bs-converter/blob/master/test/unit/converter.js
        """2072/4/3 to 2015/7/19
        2072/4/16 to 2015/8/1
        2070/9/17 to 2014/1/1
        2072/4/1 to 2015/7/17
        1978/1/1 to 1921/4/13
        2047/4/26 to 1990/8/10
        2076/09/16 to 2020/01/1
        2073/12/31 to 2017/4/13
        2074/12/30 to 2018/4/13""".split("\n").forEach { line ->
            val v = line.trimStart().replace(" to ", "/").split("/")
                .map { it.trimStart('0').toInt() }
            val nepaliDate = NepaliDate(v[0], v[1], v[2])
            val jdn = CivilDate(v[3], v[4], v[5]).toJdn()
            assertEquals(nepaliDate.toJdn(), jdn, line)
            val date = NepaliDate(jdn)
            assertEquals(v[0], date.year, line)
            assertEquals(v[1], date.month, line)
            assertEquals(v[2], date.dayOfMonth, line)
        }
    }

    test("old era persian calendar passes") {
        readResource("OldEraPersianCalendar.txt")
            .split("\n")
            .map { it.split("#")[0] }
            .filter { it.isNotBlank() }
            .forEach { line ->
                val (persianYear, persianMonth, year, month, day) = (
                        line
                            .trimStart('*').trim()
                            .replace(Regex("[ /-]"), ",")
                            .split(',')
                            .map { it.toIntOrNull() ?: fail(line) })
                assertEquals(
                    expected = listOf(year, month, day),
                    actual = CivilDate(
                        PersianDate(
                            persianYear,
                            persianMonth,
                            1
                        )
                    ).let { date ->
                        listOf(date.year, date.month, date.dayOfMonth)
                    },
                    message = line.split(" ")[0]
                )
            }
    }
})
