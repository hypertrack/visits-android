package com.hypertrack.android

import org.junit.Test

import org.junit.Assert.*

class ExtensionsKtTest {

    @Test
    fun `it should concatenate all the keys and values into one string`() {
        val payload = mapOf<String, Any>(
            "myKey" to "myValue",
            "anotherKey" to "anotherValue"
        )
        val got = payload.toNote()
        assertEquals("myKey: myValue\nanotherKey: anotherValue", got)
    }

    @Test
    fun `it should exclude entries from note if the key starts with ht_`() {
        val payload = mapOf<String, Any>(
            "myKey" to "myValue",
            "anotherKey" to "anotherValue",
            "ht_invisible_key" to "invisibleValue"
        )
        val got = payload.toNote()
        assertEquals("myKey: myValue\nanotherKey: anotherValue", got)    }
}