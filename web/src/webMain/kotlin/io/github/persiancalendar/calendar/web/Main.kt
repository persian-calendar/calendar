package io.github.persiancalendar.calendar.web

import io.github.persiancalendar.calendar.AbstractDate
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.IslamicDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.calendar.web.dom.Date
import io.github.persiancalendar.calendar.web.dom.DomDivElement
import io.github.persiancalendar.calendar.web.dom.DomInputElement
import io.github.persiancalendar.calendar.web.dom.DomSelectElement
import io.github.persiancalendar.calendar.web.dom.document

private class CalendarSpec(
    val id: String,
    val label: String,
)

private val calendars = listOf(
    CalendarSpec("civil", "میلادی"),
    CalendarSpec("persian", "شمسی"),
    CalendarSpec("islamic", "قمری"),
)

private fun dateOf(id: String, year: Int, month: Int, day: Int): AbstractDate = when (id) {
    "civil" -> CivilDate(year, month, day)
    "persian" -> PersianDate(year, month, day)
    "islamic" -> IslamicDate(year, month, day)
    else -> error("Unknown calendar: $id")
}

private fun dateOf(id: String, jdn: Long): AbstractDate = when (id) {
    "civil" -> CivilDate(jdn)
    "persian" -> PersianDate(jdn)
    "islamic" -> IslamicDate(jdn)
    else -> error("Unknown calendar: $id")
}

// Month length is computed from the calendar itself by diffing the JDN of the
// first day of this month and the next month, so it stays consistent with the
// library's own (table + fallback) logic.
private fun daysInMonth(id: String, year: Int, month: Int): Int {
    val current = dateOf(id, year, month, 1).toJdn()
    val nextYear = if (month == 12) year + 1 else year
    val nextMonth = if (month == 12) 1 else month + 1
    return (dateOf(id, nextYear, nextMonth, 1).toJdn() - current).toInt()
}

// Accept both Latin and Persian/Arabic-Indic digits in the year field.
private fun normalizeDigits(input: String): String = buildString {
    input.forEach { ch ->
        append(
            when (ch) {
                '۰' -> '0'; '۱' -> '1'; '۲' -> '2'; '۳' -> '3'; '۴' -> '4'
                '۵' -> '5'; '۶' -> '6'; '۷' -> '7'; '۸' -> '8'; '۹' -> '9'
                '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                else -> ch
            }
        )
    }
}

private fun toPersianDigits(input: String): String = buildString {
    input.forEach { ch ->
        append(
            when (ch) {
                '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'
                '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'
                else -> ch
            }
        )
    }
}

private val civilMonths = listOf(
    "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
    "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر",
)

private val persianMonths = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

// Old-era (Borji) months used by the Persian calendar before year 1304.
private val borjiMonths = listOf(
    "حمل", "ثور", "جوزا", "سرطان", "اسد", "سنبله",
    "میزان", "عقرب", "قوس", "جدی", "دلو", "حوت",
)

private val islamicMonths = listOf(
    "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
    "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه",
)

// JDN 0 was a Monday, so `jdn % 7` maps 0..6 to Monday..Sunday.
private val weekdays = listOf(
    "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه", "یکشنبه",
)

private fun weekdayOf(jdn: Long): String = weekdays[jdn.mod(7L).toInt()]

private fun monthNames(id: String, year: Int?): List<String> = when (id) {
    "civil" -> civilMonths
    "persian" -> if (year != null && year < 1304) borjiMonths else persianMonths
    "islamic" -> islamicMonths
    else -> error("Unknown calendar: $id")
}

private fun monthLabel(name: String, month: Int): String =
    "$name (${toPersianDigits(month.toString())})"

fun main() {
    val calendarSelect = document.getElementById<DomSelectElement>("calendar") ?: error("calendar")
    val yearInput = document.getElementById<DomInputElement>("year") ?: error("year")
    val monthSelect = document.getElementById<DomSelectElement>("month") ?: error("month")
    val daySelect = document.getElementById<DomSelectElement>("day") ?: error("day")
    val output = document.getElementById<DomDivElement>("output") ?: error("output")

    calendars.forEach { spec ->
        val option = document.createElement("option")
        option.textContent = spec.label
        option.setAttribute("value", spec.id)
        calendarSelect.appendChild(option)
    }
    calendarSelect.value = "persian"

    var todayJdn = 0L

    fun currentSpec(): CalendarSpec = calendars.first { it.id == calendarSelect.value }

    fun populateMonths() {
        val year = normalizeDigits(yearInput.value).toIntOrNull()
        val names = monthNames(currentSpec().id, year)
        val previous = monthSelect.value.toIntOrNull()
        monthSelect.innerHTML = ""
        repeat(12) { month ->
            val option = document.createElement("option")
            option.textContent = monthLabel(names[month], month + 1)
            option.setAttribute("value", (month + 1).toString())
            monthSelect.appendChild(option)
        }
        monthSelect.value = previous?.toString() ?: "1"
    }

    fun populateDays() {
        val year = normalizeDigits(yearInput.value).toIntOrNull()
        val month = monthSelect.value.toIntOrNull()
        if (year == null || month == null) {
            daySelect.innerHTML = ""
            return
        }
        val previous = daySelect.value.toIntOrNull()
        val maxDay = daysInMonth(currentSpec().id, year, month)
        daySelect.innerHTML = ""
        repeat(maxDay) { day ->
            val option = document.createElement("option")
            option.textContent = toPersianDigits((day + 1).toString())
            option.setAttribute("value", (day + 1).toString())
            daySelect.appendChild(option)
        }
        daySelect.value = if (previous != null && previous in 1..maxDay) previous.toString() else "1"
    }

    fun refresh(animate: Boolean = false) {
        val year = normalizeDigits(yearInput.value).toIntOrNull().takeIf { it in -10000..10000 }
        val month = monthSelect.value.toIntOrNull().takeIf { it in 1..12 }
        val day = daySelect.value.toIntOrNull().takeIf { it in 1..32 }
        if (year == null || month == null || day == null) {
            output.setAttribute("class", "")
            output.innerHTML = "<p>لطفاً یک سال معتبر وارد کنید.</p>"
            return
        }
        val jdn = dateOf(currentSpec().id, year, month, day).toJdn()

        val others = calendars.filter { it.id != currentSpec().id }
        val weekday = weekdayOf(jdn)
        val rows = others.joinToString("") { target ->
            val date = dateOf(target.id, jdn)
            val names = monthNames(target.id, date.year)
            val dayText = toPersianDigits(date.dayOfMonth.toString())
            val monthText = "${names[date.month - 1]} (${toPersianDigits(date.month.toString())})"
            val dateText = "${toPersianDigits(date.year.toString())}/" +
                "${toPersianDigits(date.month.toString())}/" +
                toPersianDigits(date.dayOfMonth.toString())
            "<li>" +
                "<div class=\"day\">$dayText</div>" +
                "<div class=\"month\">$monthText</div>" +
                "<div class=\"date\">$dateText</div>" +
                "</li>"
        }

        val delta = jdn - todayJdn
        val diffText = when {
            delta == 0L -> "امروز"
            delta > 0L -> "${toPersianDigits(delta.toString())} روز بعد از امروز"
            else -> "${toPersianDigits((-delta).toString())} روز قبل از امروز"
        }
        output.setAttribute("class", if (animate) "animating" else "")
        output.innerHTML = "<p class=\"weekday\">$weekday</p><p class=\"diff\">$diffText</p><ul>$rows</ul>"
    }

    // Show today's date (in the selected calendar) on load.
    fun setToday() {
        val now = Date()
        todayJdn = CivilDate(now.getFullYear(), now.getMonth() + 1, now.getDate()).toJdn()
        val today = dateOf(currentSpec().id, todayJdn)
        yearInput.value = toPersianDigits(today.year.toString())
        populateMonths()
        monthSelect.value = today.month.toString()
        populateDays()
        daySelect.value = today.dayOfMonth.toString()
    }

    // When the input calendar changes, keep the same instant: convert the
    // currently entered date into the newly selected calendar.
    var activeCalendarId = calendarSelect.value
    calendarSelect.addEventListener("change", {
        val year = normalizeDigits(yearInput.value).toIntOrNull()
        val month = monthSelect.value.toIntOrNull()
        val day = daySelect.value.toIntOrNull()
        if (year != null && month != null && day != null) {
            val jdn = dateOf(activeCalendarId, year, month, day).toJdn()
            val converted = dateOf(calendarSelect.value, jdn)
            yearInput.value = toPersianDigits(converted.year.toString())
            populateMonths()
            monthSelect.value = converted.month.toString()
            populateDays()
            daySelect.value = converted.dayOfMonth.toString()
        } else {
            populateMonths()
            populateDays()
        }
        activeCalendarId = calendarSelect.value
        refresh(animate = true)
    })
    yearInput.addEventListener("input", {
        populateMonths()
        populateDays()
        refresh()
    })
    monthSelect.addEventListener("change", {
        populateDays()
        refresh()
    })
    daySelect.addEventListener("change", { refresh() })

    setToday()
    refresh()
}
