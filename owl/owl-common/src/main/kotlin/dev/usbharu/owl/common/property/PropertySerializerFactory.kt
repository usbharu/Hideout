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
 * [PropertyValue]のシリアライザーのファクトリ
 *
 */
interface PropertySerializerFactory {
    /**
     * [PropertyValue]からシリアライザーを作成します
     *
     * @param T [PropertyValue]の型
     * @param propertyValue シリアライザーを作成する[PropertyValue]
     * @return 作成されたシリアライザー
     */
    fun <T> factory(propertyValue: PropertyValue<T>): PropertySerializer<T>

    /**
     * シリアライズ済みの[PropertyValue]からシリアライザーを作成します
     *
     * @param string シリアライズ済みの[PropertyValue]
     * @return 作成されたシリアライザー
     */
    fun factory(string: String): PropertySerializer<*>
}