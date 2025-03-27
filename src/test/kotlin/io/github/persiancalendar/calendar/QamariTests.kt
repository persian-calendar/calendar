package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals

class QamariTests {

    @Test
    fun `Conforms with calendar center data`() = assertAll(prepareRunners("calendar-center.txt"))

    @Test
    fun `Conforms with ettelaat data`() = assertAll(prepareRunners("ettelaat.txt"))

    private fun prepareRunners(testFileName: String): List<() -> Unit> {
        return IranianIslamicDateConverter::class.java
            .getResourceAsStream("/qamari/$testFileName")
            ?.readBytes()
            .let {
                if (it == null) {
                    System.err.println("$testFileName couldn't be found, skip")
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
                            .map { it.toInt() })
                {
                    assertEquals(
                        listOf(year, month, day),
                        CivilDate(IslamicDate(qamariYear, qamariMonth, 1)).let { date ->
                            listOf(date.year, date.month, date.dayOfMonth)
                        },
                        line.split(" ")[0]
                    )
                }
            }
    }
}
