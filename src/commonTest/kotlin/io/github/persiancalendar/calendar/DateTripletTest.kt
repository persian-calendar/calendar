package io.github.persiancalendar.calendar

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.assertEquals

class DateTripletTest : FunSpec({

    test("simple smoke test") {
        DateTriplet(-231, 23, -42).also { (y, m, d) ->
            -231 shouldBe y
            23 shouldBe m
            -42 shouldBe d
        }
    }

    fun randomByte(): Int =
        Random.nextInt(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt() + 1)

    fun randomShort(): Int =
        Random.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt() + 1)

    test("random generated test") {
        repeat(1000) {
            val val1 = randomShort()
            val val2 = randomByte()
            val val3 = randomByte()
            val date = DateTriplet(val1, val2, val3)
            val (y, m, d) = date

            val1 shouldBe y
            val2 shouldBe m
            val3 shouldBe d
        }
    }
})
