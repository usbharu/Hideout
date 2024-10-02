package dev.usbharu.owl.common.property

/**
 * Boolean型のプロパティ
 *
 * @property value プロパティ
 */
class BooleanPropertyValue(override val value: Boolean) : PropertyValue<Boolean>() {
    override val type: PropertyType
        get() = PropertyType.binary
}

/**
 * [BooleanPropertyValue]のシリアライザー
 *
 */
class BooleanPropertySerializer : PropertySerializer<Boolean> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean = propertyValue.value is Boolean

    override fun isSupported(string: String): Boolean = string.startsWith("bool:")

    override fun serialize(propertyValue: PropertyValue<*>): String = "bool:" + propertyValue.value.toString()

    override fun deserialize(string: String): PropertyValue<Boolean> =
        BooleanPropertyValue(string.replace("bool:", "").toBoolean())
}
