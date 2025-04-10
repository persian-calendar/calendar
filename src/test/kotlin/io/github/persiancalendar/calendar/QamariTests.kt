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
                            fileName + ": " + line.split(" ")[0]
                        )
                    }
                })
    }
}
