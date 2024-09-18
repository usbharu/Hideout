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

/**
 * Integer型のプロパティ
 *
 * @property value プロパティ
 */
class IntegerPropertyValue(override val value: Int) : PropertyValue<Int>() {
    override val type: PropertyType
        get() = PropertyType.number
}

/**
 * [IntegerPropertyValue]のシリアライザー
 *
 */
class IntegerPropertySerializer : PropertySerializer<Int> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean = propertyValue.value is Int

    override fun isSupported(string: String): Boolean = string.startsWith("int32:")

    override fun serialize(propertyValue: PropertyValue<*>): String = "int32:" + propertyValue.value.toString()

    override fun deserialize(string: String): PropertyValue<Int> =
        IntegerPropertyValue(string.replace("int32:", "").toInt())
}
