package io.github.persiancalendar.calendar

import org.junit.jupiter.api.assertAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

class BooksTests {

    @Test
    fun `Verify with Sarlati`() {
        BooksTests::class.java
            .getResourceAsStream("/Sarlati.txt")
            ?.readBytes()!!
            .decodeToString()
            .split("\n")
            .drop(1)
            .filter { it != "|---|---|---|---|---|---|---|" }
            // Not all are verified
            .take(400)
            .map {
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
                val gregorianDate = run {
                    val gregorianParts = parts[6].trim().split(" ")
                    assertEquals(3, gregorianParts.size, it)
                    val gregorianMonth = gregorianMonths.indexOf(gregorianParts[1]) + 1
                    assertNotEquals(0, gregorianMonth, it)
                    CivilDate(
                        gregorianParts[2].toInt(),
                        gregorianMonth,
                        gregorianParts[0].toInt()
                    )
                }
                val islamicDate = run {
                    val islamicParts = parts[4].trim().split(" ")
                    assertEquals(3, islamicParts.size, it)
                    val islamicMonth = gregorianMonths.indexOf(gregorianMonths[1]) + 1
                    assertNotEquals(0, islamicMonth, it)
                    IslamicDate(
                        islamicParts[2].toInt(),
                        islamicMonths.indexOf(islamicParts[1]) + 1,
                        islamicParts[0].toInt()
                    )
                }

                assertEquals(
                    persianDate.toJdn(),
                    gregorianDate.toJdn(),
                    "$persianDate-$gregorianDate\n_${it}ـ"
                )
//                assertEquals(
//                    islamicDate.toJdn(),
//                    gregorianDate.toJdn(),
//                    "$persianDate-$gregorianDate\n_${it}ـ"
//                )
            }
    }

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
