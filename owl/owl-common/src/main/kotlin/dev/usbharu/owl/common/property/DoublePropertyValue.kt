package dev.usbharu.owl.common.property

/**
 * Double型のプロパティ
 *
 * @property value プロパティ
 */
class DoublePropertyValue(override val value: Double) : PropertyValue<Double>() {
    override val type: PropertyType
        get() = PropertyType.number
}

/**
 * [DoublePropertyValue]のシリアライザー
 *
 */
class DoublePropertySerializer : PropertySerializer<Double> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean = propertyValue.value is Double

    override fun isSupported(string: String): Boolean = string.startsWith("double:")

    override fun serialize(propertyValue: PropertyValue<*>): String = "double:" + propertyValue.value.toString()

    override fun deserialize(string: String): PropertyValue<Double> =
        DoublePropertyValue(string.replace("double:", "").toDouble())
}
