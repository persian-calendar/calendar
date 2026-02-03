package io.github.persiancalendar.calendar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CalendarCenterConversions : FunSpec({
    // https://calendar.ut.ac.ir/Fa/Software/CalConv.asp
    test("Matches with Calendar Center converter Hijri months") {
        readResource("CalendarCenterData.txt")
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .forEach { line ->
                val p = line.split(",").map { it.trim().toInt() }

                val pJdn = PersianDate(p[2], p[1], p[0]).toJdn()
                val iJdn = IslamicDate(p[5], p[4], p[3]).toJdn()
                val cJdn = CivilDate(p[8], p[7], p[6]).toJdn()

                setOf(pJdn, iJdn, cJdn).size shouldBe 1
            }
    }
})
