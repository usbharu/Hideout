package dev.usbharu.owl.common.property

class DoublePropertyValue(override val value: Double) : PropertyValue<Double>() {
    override val type: PropertyType
        get() = PropertyType.number
}

class DoublePropertySerializer : PropertySerializer<Double> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean {
        return propertyValue.value is Double
    }

    override fun isSupported(string: String): Boolean {
        return string.startsWith("double:")
    }

    override fun serialize(propertyValue: PropertyValue<*>): String {
        return "double:" + propertyValue.value.toString()
    }

    override fun deserialize(string: String): PropertyValue<Double> {
        return DoublePropertyValue(string.replace("double:", "").toDouble())
    }
}