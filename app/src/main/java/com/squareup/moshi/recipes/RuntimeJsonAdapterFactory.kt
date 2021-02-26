@file:Suppress("UNCHECKED_CAST")

package com.squareup.moshi.recipes

import com.squareup.moshi.*
import java.io.IOException
import java.lang.reflect.Type
import java.util.*

class RuntimeJsonAdapterFactory(
        private val baseType: Class<*>,
        private val labelKey: String
) : JsonAdapter.Factory {
    private val subtypeToLabel: MutableMap<Class<*>, String> = LinkedHashMap()

    fun registerSubtype(subtype: Class<*>?, label: String?): RuntimeJsonAdapterFactory {
        if (subtype == null) {
            throw NullPointerException("subtype == null")
        }
        if (label == null) {
            throw NullPointerException("label == null")
        }
        require(baseType.isAssignableFrom(subtype)) { "$subtype must be a $baseType" }
        subtypeToLabel[subtype] = label
        return this
    }

    override fun create(type: Type, annotations: Set<Annotation?>, moshi: Moshi): JsonAdapter<*>? {
        if (annotations.isNotEmpty() || Types.getRawType(type) != baseType) {
            return null
        }
        val subtypeToLabel: Map<Class<*>, String> = LinkedHashMap(
                subtypeToLabel
        )
        val size = subtypeToLabel.size
        val labelToDelegate: MutableMap<String, JsonAdapter<*>> = LinkedHashMap(size)
        val subtypeToDelegate: MutableMap<Class<*>, JsonAdapter<*>> = LinkedHashMap(size)
        for ((key, value) in subtypeToLabel) {
            val delegate: JsonAdapter<*> = moshi.adapter<Any>(key, annotations)
            labelToDelegate[value] = delegate
            subtypeToDelegate[key] = delegate
        }
        val toJsonDelegate = moshi.adapter<Map<String, Any?>?>(
                Types.newParameterizedType(
                        MutableMap::class.java, String::class.java, Any::class.java
                )
        )
        return RuntimeJsonAdapter(
                labelKey, labelToDelegate, subtypeToDelegate, subtypeToLabel,
                toJsonDelegate
        )
    }

    private class RuntimeJsonAdapter internal constructor(
            private val labelKey: String,
            private val labelToDelegate: Map<String, JsonAdapter<*>>,
            private val subtypeToDelegate: Map<Class<*>, JsonAdapter<*>>,
            private val subtypeToLabel: Map<Class<*>, String>,
            private val toJsonDelegate: JsonAdapter<Map<String, Any?>?>
    ) : JsonAdapter<Any?>() {
        @Throws(IOException::class)
        override fun fromJson(reader: JsonReader): Any? {
            val raw = reader.readJsonValue()
            if (raw !is Map<*, *>) {
                throw JsonDataException(
                        "Value must be a JSON object but had a value of " + raw + " of type " + raw!!.javaClass
                )
            }
            val value// This is a JSON object.
                    = raw as MutableMap<String, Any>
            val label = value.remove(labelKey)
                    ?: throw JsonDataException("Missing label for $labelKey")
            if (label !is String) {
                throw JsonDataException(
                        "Label for "
                                + labelKey
                                + " must be a string but had a value of "
                                + label
                                + " of type "
                                + label.javaClass
                )
            }
            val delegate = labelToDelegate[label]
                    ?: throw JsonDataException("Type not registered for label: $label")
            return delegate.fromJsonValue(value)
        }

        @Throws(IOException::class)
        override fun toJson(writer: JsonWriter, value: Any?) {
            val subtype: Class<*> = value!!.javaClass
            val delegate// The delegate is a JsonAdapter<subtype>.
                    = subtypeToDelegate[subtype] as JsonAdapter<Any>?
                    ?: throw JsonDataException("Type not registered: $subtype")
            val jsonValue// This is a JSON object.
                    = delegate.toJsonValue(value) as MutableMap<String, Any?>?
            val existingLabel = jsonValue!!.put(labelKey, subtypeToLabel[subtype])
            if (existingLabel != null) {
                throw JsonDataException(
                        "Label field $labelKey already defined as $existingLabel"
                )
            }
            toJsonDelegate.toJson(writer, jsonValue)
        }
    }
}