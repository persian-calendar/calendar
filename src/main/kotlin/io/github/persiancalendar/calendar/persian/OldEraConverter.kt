package io.github.persiancalendar.calendar.persian

import io.github.persiancalendar.calendar.DateTriplet

internal object OldEraConverter {
    private const val jdSupportStart: Long = 2_393_551 // CivilDate(1841, 3, 21).toJdn()
    private val jdSupportEnd: Long
    private val months: IntArray
    private val supportedYears: Int
    private const val supportedYearsStart = 1220

    init {
        // This is brought from https://github.com/roozbehp/persiancalendar/blob/4fc570c/data/tsybulsky.txt
        val monthLength = listOf(
            /*1220*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1221*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1222*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1223*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1224*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1225*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1226*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1227*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1228*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1229*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1230*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1231*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1232*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1233*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1234*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1235*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1236*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1237*/ 31, 31, 31, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1238*/ 31, 31, 31, 32, 31, 31, 30, 30, 29, 30, 30, 30,
            /*1239*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1240*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1241*/ 30, 32, 31, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1242*/ 31, 31, 31, 32, 31, 31, 30, 30, 29, 30, 29, 30,
            /*1243*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1244*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1245*/ 30, 31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1246*/ 31, 31, 31, 32, 31, 31, 30, 30, 29, 30, 29, 30,
            /*1247*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1248*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1249*/ 30, 31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1250*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1251*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1252*/ 30, 31, 32, 31, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1253*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1254*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1255*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1256*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1257*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1258*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1259*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1260*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1261*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 29, 30, 30,
            /*1262*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1263*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1264*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1265*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 29, 30, 30,
            /*1266*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1267*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1268*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1269*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1270*/ 31, 31, 31, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1271*/ 31, 31, 31, 32, 31, 31, 30, 30, 29, 30, 30, 30,
            /*1272*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1273*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1274*/ 30, 31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1275*/ 31, 31, 31, 32, 31, 31, 30, 30, 29, 30, 29, 30,
            /*1276*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1277*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1278*/ 30, 31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1279*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1280*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1281*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1282*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1283*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1284*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1285*/ 30, 31, 31, 32, 31, 31, 30, 30, 29, 30, 30, 30,
            /*1286*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1287*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1288*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1289*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1290*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1291*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1292*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1293*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1294*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 30, 29, 30,
            /*1295*/ 31, 31, 31, 31, 32, 31, 30, 30, 29, 30, 29, 30,
            /*1296*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1297*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1298*/ 30, 31, 32, 31, 31, 31, 31, 30, 29, 29, 30, 30,
            /*1299*/ 31, 31, 31, 31, 32, 30, 31, 30, 29, 30, 29, 30,
            /*1300*/ 31, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1301*/ 30, 31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30,
            /*1302*/ 30, 31, 32, 31, 31, 31, 31, 29, 30, 29, 30, 30,
            /*1303*/ 30, 31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30,
        )
        supportedYears = monthLength.size / 12
        months = IntArray(monthLength.size)
        var jd = 0
        for (m in monthLength.indices) {
            months[m] = jd
            jd += monthLength[m]
        }
        jdSupportEnd = jd + jdSupportStart
    }

    internal fun toJdn(year: Int, month: Int, day: Int): Long {
        val yearIndex = year - supportedYearsStart
        return if (yearIndex < 0 || yearIndex >= supportedYears) -1 else months[yearIndex * 12 + month - 1] + day + jdSupportStart - 1
    }

    internal fun fromJdn(jd: Long): DateTriplet? {
        if (jd < jdSupportStart || jd >= jdSupportEnd) return null
        val days = (jd - jdSupportStart).toInt()
        var index = days / 32 // It is just the upper bound of months lengths
        while (index + 1 < months.size && months[index + 1] <= days) ++index
        val yearIndex = index / 12
        val month = index % 12
        val day = days - months[index]
        return DateTriplet(
            year = yearIndex + supportedYearsStart,
            month = month + 1,
            dayOfMonth = day + 1
        )
    }
}
