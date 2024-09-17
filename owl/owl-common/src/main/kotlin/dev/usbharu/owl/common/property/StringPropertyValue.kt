package dev.usbharu.owl.common.property

/**
 * String型のプロパティ
 *
 * @property value プロパティ
 */
class StringPropertyValue(override val value: String) : PropertyValue<String>() {
    override val type: PropertyType
        get() = PropertyType.string
}

/**
 * [StringPropertyValue]のシリアライザー
 *
 */
class StringPropertyValueSerializer : PropertySerializer<String> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean = propertyValue.value is String

    override fun isSupported(string: String): Boolean = string.startsWith("str:")

    override fun serialize(propertyValue: PropertyValue<*>): String = "str:" + propertyValue.value

    override fun deserialize(string: String): PropertyValue<String> = StringPropertyValue(string.replace("str:", ""))
}
