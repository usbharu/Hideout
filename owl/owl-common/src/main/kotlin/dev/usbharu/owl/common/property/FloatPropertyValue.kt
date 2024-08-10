/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.owl.common.property

class FloatPropertyValue(override val value: Float) : PropertyValue<Float>() {
    override val type: PropertyType
        get() = PropertyType.number
}

/**
 * [FloatPropertyValue]のシリアライザー
 *
 */
class FloatPropertySerializer : PropertySerializer<Float> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean {
        return propertyValue.value is Float
    }

    override fun isSupported(string: String): Boolean {
        return string.startsWith("float:")
    }

    override fun serialize(propertyValue: PropertyValue<*>): String {
        return "float:" + propertyValue.value.toString()
    }

    override fun deserialize(string: String): PropertyValue<Float> {
        return FloatPropertyValue(string.replace("float:", "").toFloat())
    }
}