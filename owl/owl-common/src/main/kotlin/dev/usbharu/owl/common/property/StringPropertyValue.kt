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
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean {
        return propertyValue.value is String
    }

    override fun isSupported(string: String): Boolean {
        return string.startsWith("str:")
    }

    override fun serialize(propertyValue: PropertyValue<*>): String {
        return "str:" + propertyValue.value
    }

    override fun deserialize(string: String): PropertyValue<String> {
        return StringPropertyValue(string.replace("str:", ""))
    }
}