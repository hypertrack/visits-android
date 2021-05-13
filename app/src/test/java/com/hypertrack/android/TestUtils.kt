package com.hypertrack.android

import androidx.lifecycle.LiveData
import junit.framework.TestCase.assertNull

fun <T> LiveData<T>.observeAndGetValue(): T {
    observeForever {}
    return value!!
}

fun <T> LiveData<T>.observeAndAssertNull() {
    observeForever {}
    return assertNull(value)
}