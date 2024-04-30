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

package dev.usbharu.owl.common.task

import dev.usbharu.owl.common.property.PropertyValue

/**
 * タスク定義
 *
 * @param T タスク
 */
interface TaskDefinition<T : Task> {
    /**
     * タスク名
     */
    val name: String

    /**
     * 優先度
     */
    val priority: Int

    /**
     * 最大リトライ数
     */
    val maxRetry: Int

    /**
     * リトライポリシー名
     *
     * ポリシーの解決は各Brokerに依存しています
     */
    val retryPolicy: String

    /**
     * タスク実行時のタイムアウト(ミリ秒)
     */
    val timeoutMilli: Long

    /**
     * プロパティ定義
     */
    val propertyDefinition: PropertyDefinition

    /**
     * [Task]の[Class]
     */
    val type: Class<T>

    /**
     * タスクをシリアライズします.
     * プロパティのシリアライズと混同しないようにしてください。
     * @param task シリアライズするタスク
     * @return シリアライズされたタスク
     */
    fun serialize(task: T): Map<String, PropertyValue<*>>

    /**
     * タスクをデシリアライズします。
     * プロパティのデシリアライズと混同しないようにしてください
     * @param value デシリアライズするタスク
     * @return デシリアライズされたタスク
     */
    fun deserialize(value: Map<String, PropertyValue<*>>): T
}