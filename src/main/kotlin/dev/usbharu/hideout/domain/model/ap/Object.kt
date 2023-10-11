package dev.usbharu.hideout.domain.model.ap

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

open class Object : JsonLd {
    @JsonSerialize(using = TypeSerializer::class)
    var type: List<String> = emptyList()
    var name: String? = null
    var actor: String? = null
    var id: String? = null

    protected constructor()
    constructor(type: List<String>, name: String? = null, actor: String? = null, id: String? = null) : super() {
        this.type = type
        this.name = name
        this.actor = actor
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Object) return false
        if (!super.equals(other)) return false

        if (type != other.type) return false
        if (name != other.name) return false
        if (actor != other.actor) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (actor?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Object(type=$type, name=$name, actor=$actor, id=$id) ${super.toString()}"

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
