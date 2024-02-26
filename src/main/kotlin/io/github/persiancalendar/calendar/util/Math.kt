package io.github.persiancalendar.calendar.util

import kotlin.math.PI
import kotlin.math.sin

internal fun Double.toRadians(): Double = this * PI / 180.0

internal fun sinOfDegree(degree: Double): Double = sin(degree.toRadians())
