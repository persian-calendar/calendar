package io.github.persiancalendar.calendar

import org.junit.jupiter.api.assertAll
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BooksTests {

    @Test
    fun `Verify with Sarlati`() {
        val tests = BooksTests::class.java
            .getResourceAsStream("/Sarlati.txt")
            ?.readBytes()!!
            .decodeToString()
        val qamari = mutableListOf<Triple<Int, Int, Int>>()
        assertAll(
            tests
                .split("\n")
                .drop(1)
                .mapNotNull {
                    if (it == "|---|---|---|---|---|---|---|") {
//                        println()
                        return@mapNotNull null
                    }
                    if (it.startsWith("#")) return@mapNotNull null
                    val parts = it.split("|")

                    val persianDate = run {
                        val persianParts = parts[2].trim().split(" ")
                        assertEquals(3, persianParts.size, it)
                        val persianYear = persianParts[2].toInt()
                        val persianMonth = (if (persianYear < 1304) borjiMonths else persianMonths)
                            .indexOf(persianParts[1]) + 1
                        assertNotEquals(0, persianMonth, it)
                        PersianDate(persianYear, persianMonth, persianParts[0].toInt())
                    }
                    val islamicDate = run {
                        val islamicParts = parts[4].trim().split(" ")
                        assertEquals(3, islamicParts.size, it)
                        val islamicMonth = islamicMonths.indexOf(islamicParts[1]) + 1
                        if (parts[5].isNotBlank()) qamari.add(
                            Triple(
                                islamicParts[2].toInt(),
                                islamicMonth,
                                parts[5].trim().toInt()
                            )
                        )
                        assertNotEquals(0, islamicMonth, it)

                        val month = islamicMonths.indexOf(islamicParts[1]) + 1
                        //print(islamicParts[2] + "/$month/" + islamicParts[0] + " ")

                        IslamicDate(islamicParts[2].toInt(), month, islamicParts[0].toInt())
                    }
                    val gregorianDate = run {
                        val gregorianParts = parts[6].trim().split(" ")
                        assertEquals(3, gregorianParts.size, it)
                        val gregorianMonth = gregorianMonths.indexOf(gregorianParts[1]) + 1
                        assertNotEquals(0, gregorianMonth, it)

//                        println(
//                            gregorianParts[2] +
//                                    "-${gregorianMonth.toString().padStart(2, '0')}-" +
//                                    gregorianParts[0].padStart(2, '0')
//                        )

                        CivilDate(
                            gregorianParts[2].toInt(),
                            gregorianMonth,
                            gregorianParts[0].toInt()
                        )
                    };

                    {
                        val message = "$persianDate-$gregorianDate\n_${it}ـ"

                        assertEquals(
                            parts[1].trim(),
                            gregorianDate.weekDay,
                            message
                        )
                        when (persianDate) {
                            PersianDate(1285, 10, 1) -> Unit

                            else -> assertEquals(
                                parts[1].trim(),
                                persianDate.weekDay,
                                message
                            )
                        }
                        assertTrue(
                            abs(IslamicDate(persianDate).toJdn() - islamicDate.toJdn()) < 3,
                            message
                        )
                        when (persianDate) {
                            PersianDate(1285, 10, 1) -> Unit

                            else -> assertEquals(
                                persianDate.toJdn(),
                                gregorianDate.toJdn(),
                                message
                            )
                        }
//                        assertEquals(
//                            islamicDate.toJdn(),
//                            gregorianDate.toJdn(),
//                            "$persianDate-$gregorianDate\n_${it}ـ"
//                        )
                    }
                })

//        qamari.sortBy { it.first }
//        print(qamari.groupBy { it.first }.map { g ->
//            "/*${g.key}*/ 0b" + g.value.distinctBy { it.second }.sortedBy { it.second }.map {
//                if (it.third == 30) 1 else 0
//            }.joinToString("_") + ",\n"
//        }.joinToString(""))
    }

    private val weekDays = listOf(
        "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه",
    )

    private val AbstractDate.weekDay
        get(): String = weekDays[(toJdn() % 7).toInt()]

    private val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد",
        "شهریور", "مهر", "آبان", "آذر", "دی",
        "بهمن", "اسفند"
    )
    private val borjiMonths = listOf(
        "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
        "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت"
    )
    private val gregorianMonths = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )
    private val islamicMonths = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادى‌الاولى", "جمادی‌الثانیه",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )
}
