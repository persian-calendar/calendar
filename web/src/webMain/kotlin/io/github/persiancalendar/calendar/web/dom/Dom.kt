package io.github.persiancalendar.calendar.web.dom

// Minimal DOM bindings shared by the JS and WasmJS targets so the demo needs no
// third-party dependencies.

external interface DomEvent

external interface DomNode {
    fun appendChild(newChild: DomNode): DomNode
}

external interface DomElement : DomNode {
    var innerHTML: String
    var textContent: String
    fun setAttribute(name: String, value: String)
    fun addEventListener(type: String, listener: () -> Unit)
}

external interface DomSelectElement : DomElement {
    var value: String
}

external interface DomInputElement : DomElement {
    var value: String
}

external interface DomDivElement : DomElement

external interface DomDocument {
    fun <T : DomElement> getElementById(elementId: String): T?
    fun createElement(tagName: String): DomElement
}

external val document: DomDocument

external class Date {
    constructor()
    fun getFullYear(): Int
    fun getMonth(): Int
    fun getDate(): Int
}
