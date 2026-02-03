package io.github.persiancalendar.calendar

import io.github.persiancalendar.calendar.util.persianLeapYear
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AllPersianCalendarLeapYears : FunSpec({

    test("Conforms with calendrical leap years") {
        val leapYears = readResource("leaps.txt")
            .lineSequence()
            .filterNot { it.startsWith("#") || it.isBlank() }
            .map { it.toInt() }
            .toSet()

        (1..10_000).forEach { year ->
            withClue("Year $year") {
                print(" $year")
                persianLeapYear(year, 52.5) shouldBe (year in leapYears)
            }
        }
    }
})
