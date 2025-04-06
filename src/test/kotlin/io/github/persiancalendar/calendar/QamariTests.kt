package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QamariTests {

    @Test
    fun `Conforms with Qamari tests`() {
        assertAll(
            IranianIslamicDateConverter::class.java
                .getResourceAsStream("/qamari/consolidated.txt")
            ?.readBytes()
            .let {
                if (it == null) {
                    System.err.println("consolidated.txt couldn't be found, skip")
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
                        line.split(" ")[0]
                    )
                }
            })
    }
}
