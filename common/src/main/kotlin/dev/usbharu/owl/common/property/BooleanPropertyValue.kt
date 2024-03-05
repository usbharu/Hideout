package dev.usbharu.owl.common.property

class BooleanPropertyValue(override val value: Boolean) : PropertyValue<Boolean>() {
    override val type: PropertyType
        get() = PropertyType.binary
}

class BooleanPropertySerializer : PropertySerializer<Boolean> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean {
        return propertyValue.value is Boolean
    }

    override fun isSupported(string: String): Boolean {
        return string.startsWith("bool:")
    }

    override fun serialize(propertyValue: PropertyValue<*>): String {
        return "bool:" + propertyValue.value.toString()
    }

    override fun deserialize(string: String): PropertyValue<Boolean> {
        return BooleanPropertyValue(string.replace("bool:", "").toBoolean())
    }
}