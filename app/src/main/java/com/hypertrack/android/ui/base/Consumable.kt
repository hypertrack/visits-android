package com.hypertrack.android.ui.base

class Consumable<T>(
    val payload: T,
    private var consumed: Boolean = false
) {
    fun consume(c: (payload: T) -> Unit) {
        if (!consumed) {
            consumed = true
            c.invoke(payload)
        }
    }

    fun <R> map(mapper: (T) -> R): Consumable<R> {
        return Consumable(mapper.invoke(payload), consumed)
    }
}

fun Exception.toConsumable(): Consumable<Exception> {
    return Consumable(this)
}

fun String.toConsumable(): Consumable<String> {
    return Consumable(this)
}