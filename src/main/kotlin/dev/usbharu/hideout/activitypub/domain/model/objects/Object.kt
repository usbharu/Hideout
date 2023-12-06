package dev.usbharu.hideout.activitypub.domain.model.objects

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dev.usbharu.hideout.activitypub.domain.model.JsonLd

open class Object : JsonLd {
    @JsonSerialize(using = TypeSerializer::class)
    var type: List<String> = emptyList()
        set(value) {
            field = value.filter { it.isNotBlank() }
        }

    protected constructor()
    constructor(type: List<String>) : super() {
        this.type = type.filter { it.isNotBlank() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Object

        return type == other.type
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = "Object(type=$type) ${super.toString()}"

    companion object {
        @JvmStatic
        protected fun add(list: List<String>, type: String): List<String> {
            val toMutableList = list.toMutableList()
            toMutableList.add(type)
            return toMutableList.distinct()
        }
    }
}

class TypeSerializer : JsonSerializer<List<String>>() {
    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value?.size == 1) {
            gen?.writeString(value[0])
        } else {
            gen?.writeStartArray()
            value?.forEach {
                gen?.writeString(it)
            }
            gen?.writeEndArray()
        }
    }
}
