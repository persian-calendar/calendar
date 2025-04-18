package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QamariTests {

    @Test
    fun `Conforms with Qamari tests`() = checkFile("/qamari/consolidated.txt")

    @Test
    fun `Conforms with predictions`() = checkFile("/qamari-perdictions.txt")

    fun checkFile(fileName: String) {
        assertAll(
            IranianIslamicDateConverter::class.java
                .getResourceAsStream(fileName)
                ?.readBytes()
                .let {
                    if (it == null) {
                        System.err.println("$fileName couldn't be found, skip")
                        ByteArray(0)
                    } else it
                }
                .decodeToString()
                .split("\n")
                .map { it.split("#")[0] }
                .filter { it.isNotBlank() }
                .map { line ->
                    val (qamariYear, qamariMonth, year, month, day) = (
                            line
                                .trimStart('*').trim()
                                .replace(Regex("[ /-]"), ",")
                                .split(',')
                                .map { it.toIntOrNull() ?: fail(line) })
                    {
                        assertEquals(
                            listOf(year, month, day),
                            CivilDate(IslamicDate(qamariYear, qamariMonth, 1)).let { date ->
                                listOf(date.year, date.month, date.dayOfMonth)
                            },
                            fileName + ": " + line.split(" ")[0]
                        )
                    }
                })
    }

//    @Test
//    fun helper1() {
//        val baseDate = IslamicDate(1264, 1, 1)
//        (0..<12 * 5).forEach { months ->
//            val nextMonth = baseDate.monthStartOfMonthsDistance(months + 1)
//            val thisMonth = baseDate.monthStartOfMonthsDistance(months)
//            if (thisMonth.month == 1) print("/*${thisMonth.year}*/ ")
//            print(nextMonth.toJdn() - thisMonth.toJdn())
//            if (thisMonth.month == 12) println(", ") else print(", ")
//        }
//    }

//    @Test
//    fun helper2() {
//        var lastMonth = 0L
//        var counter = 0
//        IranianIslamicDateConverter::class.java
//            .getResourceAsStream("/qamari/consolidated.txt")
//            ?.readBytes()!!
//            .decodeToString()
//            .split("\n")
//            .take(12 * 5)
//            .forEach {
//                val (y, m, d) = it.split(" ")[1].split("-").map { it.toInt(10) }
//                val thisMonth = CivilDate(y, m, d).toJdn()
//                if (lastMonth != 0L) print("${thisMonth - lastMonth}, ")
//                lastMonth = thisMonth
//                if (((counter++) - 1) % 12 == 11) println()
//            }
//    }
}
