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
 * [PropertySerializer]のユーティリティークラス
 */
object PropertySerializeUtils {
    /**
     * Stringと[PropertyValue]の[Map]から[PropertyValue]をシリアライズし、StringとStringの[Map]として返します
     *
     * @param serializerFactory シリアライズに使用する[PropertySerializerFactory]
     * @param properties シリアライズする[Map]
     * @return Stringとシリアライズ済みの[PropertyValue]の[Map]
     */
    fun serialize(
        serializerFactory: PropertySerializerFactory,
        properties: Map<String, PropertyValue<*>>,
    ): Map<String, String> {
        return properties.map {
            try {
                it.key to serializerFactory.factory(it.value).serialize(it.value)
            } catch (e: Exception) {
                throw PropertySerializeException("Failed to serialize property in ${serializerFactory.javaClass}", e)
            }
        }.toMap()
    }

    /**
     * Stringとシリアライズ済みの[PropertyValue]の[Map]からシリアライズ済みの[PropertyValue]をデシリアライズし、Stringと[PropertyValue]の[Map]として返します
     *
     * @param serializerFactory デシリアライズに使用する[PropertySerializerFactory]
     * @param properties デシリアライズする[Map]
     * @return Stringと[PropertyValue]の[Map]
     */
    fun deserialize(
        serializerFactory: PropertySerializerFactory,
        properties: Map<String, String>
    ): Map<String, PropertyValue<*>> =
        properties.map { it.key to serializerFactory.factory(it.value).deserialize(it.value) }.toMap()
}