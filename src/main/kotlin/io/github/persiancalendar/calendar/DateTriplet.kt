package io.github.persiancalendar.calendar

// Putting numbers bigger than [-32768-32767] causes undefined behavior
@JvmInline
value class DateTriplet private constructor(private val packedValue: Long) {
    constructor(year: Int, month: Int, dayOfMonth: Int) : this(
        ((year.toLong() and 0xFFFF) shl 32) or
                ((month.toLong() and 0xFFFF) shl 16) or
                (dayOfMonth.toLong() and 0xFFFF)
    )

    val year: Int get() = ((packedValue shr 32) and 0xFFFF).toInt()
    val month: Int get() = ((packedValue shr 16) and 0xFFFF).toInt()
    val dayOfMonth: Int get() = (packedValue and 0xFFFF).toInt()

    operator fun component1(): Int = year
    operator fun component2(): Int = month
    operator fun component3(): Int = dayOfMonth

    override fun toString(): String = "(year=$year, month=$month, dayOfMonth=$dayOfMonth)"
}
