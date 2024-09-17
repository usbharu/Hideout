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
 * [PropertyValue]をシリアライズ・デシリアライズします
 *
 * @param T [PropertyValue]の型
 */
interface PropertySerializer<T> {
    /**
     * [PropertyValue]をサポートしているかを確認します
     *
     * @param propertyValue 確認する[PropertyValue]
     * @return サポートしている場合true
     */
    fun isSupported(propertyValue: PropertyValue<*>): Boolean

    /**
     * シリアライズ済みの[PropertyValue]から[PropertyValue]をサポートしているかを確認します
     *
     * @param string 確認するシリアライズ済みの[PropertyValue]
     * @return サポートしている場合true
     */
    fun isSupported(string: String): Boolean

    /**
     * [PropertyValue]をシリアライズします
     *
     * @param propertyValue シリアライズする[PropertyValue]
     * @return シリアライズ済みの[PropertyValue]
     */
    fun serialize(propertyValue: PropertyValue<*>): String

    /**
     * デシリアライズします
     *
     * @param string シリアライズ済みの[PropertyValue]
     * @return デシリアライズされた[PropertyValue]
     */
    fun deserialize(string: String): PropertyValue<T>
}
