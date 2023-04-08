package dev.usbharu.hideout.ap

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

open class Object : JsonLd {
    @JsonSerialize(using = TypeSerializer::class)
    private var type: List<String> = emptyList()
    var name: String? = null

    protected constructor()
    constructor(type: List<String>, name: String) : super() {
        this.type = type
        this.name = name
    }

    companion object {
        @JvmStatic
        protected fun add(list:List<String>,type:String):List<String> {
            val toMutableList = list.toMutableList()
            toMutableList.add(type)
            return toMutableList.distinct()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Object) return false

        if (type != other.type) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }


}

public class TypeSerializer : JsonSerializer<List<String>>() {
    override fun serialize(value: List<String>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        println(value)
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
