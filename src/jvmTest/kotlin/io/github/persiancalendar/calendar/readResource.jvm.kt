package io.github.persiancalendar.calendar

actual fun readResource(name: String): String {
    return object {}.javaClass.classLoader.getResource(name)!!.readText()
}
