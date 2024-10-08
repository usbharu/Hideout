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
 * [Set]でカスタマイズできる[PropertySerializerFactory]
 *
 * @property propertySerializers [PropertySerializer]の[Set]
 */
open class CustomPropertySerializerFactory(private val propertySerializers: Set<PropertySerializer<*>>) :
    PropertySerializerFactory {
    override fun <T> factory(propertyValue: PropertyValue<T>): PropertySerializer<T> =
        propertySerializers.firstOrNull { it.isSupported(propertyValue) } as PropertySerializer<T>?
            ?: throw IllegalArgumentException("PropertySerializer not found: $propertyValue")

    override fun factory(string: String): PropertySerializer<*> = propertySerializers.first { it.isSupported(string) }
}
