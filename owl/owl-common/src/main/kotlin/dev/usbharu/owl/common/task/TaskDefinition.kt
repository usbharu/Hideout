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

import dev.usbharu.owl.common.allFields
import dev.usbharu.owl.common.property.*

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
        get() = type.simpleName

    /**
     * 優先度
     */
    val priority: Int
        get() = 0

    /**
     * 最大リトライ数
     */
    val maxRetry: Int
        get() = 5

    /**
     * リトライポリシー名
     *
     * ポリシーの解決は各Brokerに依存しています
     */
    val retryPolicy: String
        get() = ""

    /**
     * タスク実行時のタイムアウト(ミリ秒)
     */
    val timeoutMilli: Long
        get() = 1000

    /**
     * プロパティ定義
     */
    val propertyDefinition: PropertyDefinition
        get() {
            val mapValues = type.allFields.associate { it.name to it.type }.mapValues {
                when {
                    it.value === Int::class.java -> PropertyType.number
                    it.value === String::class.java -> PropertyType.string
                    it.value === Long::class.java -> PropertyType.number
                    it.value === Double::class.java -> PropertyType.number
                    it.value === Float::class.java -> PropertyType.number
                    else -> PropertyType.binary
                }
            }
            return PropertyDefinition(mapValues)
        }

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
    fun serialize(task: T): Map<String, PropertyValue<*>> {
        return type.allFields.associateBy { it.name }.mapValues {
            when {
                it.value.type === Int::class.java -> IntegerPropertyValue(it.value.getInt(task))
                it.value.type === String::class.java -> StringPropertyValue(it.value.get(task) as String)
                it.value.type === Long::class.java -> LongPropertyValue(it.value.getLong(task))
                it.value.type === Double::class.java -> DoublePropertyValue(it.value.getDouble(task))
                it.value.type === Float::class.java -> FloatPropertyValue(it.value.getFloat(task))
                it.value.type === Boolean::class.java -> BooleanPropertyValue(it.value.getBoolean(task))
                else -> throw IllegalArgumentException("Unsupported type ${it.value} in ${task.javaClass.name}")
            }
        }
    }

    /**
     * タスクをデシリアライズします。
     * プロパティのデシリアライズと混同しないようにしてください
     * @param value デシリアライズするタスク
     * @return デシリアライズされたタスク
     */
    fun deserialize(value: Map<String, PropertyValue<*>>): T {
        val task = try {
            type.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Unable to deserialize value $value for type ${type.name}", e)
        }

        type.allFields.associateBy { it.name }.mapValues {
            when {
                it.value.type === Int::class.java -> it.value.setInt(task, value.getValue(it.key).value as Int)
                it.value.type === Double::class.java -> it.value.setDouble(task, value.getValue(it.key).value as Double)
                it.value.type === Float::class.java -> it.value.setFloat(task, value.getValue(it.key).value as Float)
                it.value.type === String::class.java -> it.value.set(task, value.getValue(it.key).value as String)
                it.value.type === Long::class.java -> it.value.setLong(task, value.getValue(it.key).value as Long)
                else -> throw IllegalArgumentException("Unsupported type ${it.value} in ${task.javaClass.name}")
            }
        }

        return task
    }
}
