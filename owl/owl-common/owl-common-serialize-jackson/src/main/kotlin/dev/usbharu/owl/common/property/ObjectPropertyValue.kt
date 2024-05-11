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

import com.fasterxml.jackson.databind.ObjectMapper

class ObjectPropertyValue(override val value: Any) : PropertyValue<Any>() {
    override val type: PropertyType
        get() = PropertyType.string
}

class ObjectPropertySerializer(private val objectMapper: ObjectMapper) : PropertySerializer<Any> {
    override fun isSupported(propertyValue: PropertyValue<*>): Boolean {
        println(propertyValue::class.java)
        return propertyValue is ObjectPropertyValue
    }

    override fun isSupported(string: String): Boolean {
        return string.startsWith("jackson:")
    }

    override fun serialize(propertyValue: PropertyValue<*>): String {
        return "jackson:" + propertyValue.value!!::class.qualifiedName + ":" + objectMapper.writeValueAsString(
            propertyValue.value
        )
    }

    override fun deserialize(string: String): PropertyValue<Any> {
//todo jacksonに読み込ませるStringがjackson:classname:jsonになっているのでjsonだけを読み込ませる
        return ObjectPropertyValue(
            objectMapper.readValue(
                string,
                Class.forName(string.substringAfter("jackson:").substringBefore(":"))
            )
        )

    }
}