package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.islamic.IranianIslamicDateConverter
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.CsvSources
import kotlin.test.assertEquals

class QamariTests {

    private val calendarCenterGroundTruth = IranianIslamicDateConverter::class.java
        .getResourceAsStream("/qamari/calendar-center.txt")
        ?.readBytes()!!
        .decodeToString()
        .split("\n")
        .map { it.split("#")[0] }
        .filter { it.isNotBlank() }
        .map { it.trimStart('*').trim() }
        .map { it.replace(Regex("[ /-]"), ",") }
        .map { line -> line.split(',').map { it.toInt() } }

    @Test
    fun `Conforms with calendar center data`() {
        calendarCenterGroundTruth.forEach { (qamariYear, qamariMonth, year, month, day) ->
            assertEquals(
                IslamicDate(qamariYear, qamariMonth, 1).toJdn(),
                CivilDate(year, month, day).toJdn(),
            )
        }
    }
}
