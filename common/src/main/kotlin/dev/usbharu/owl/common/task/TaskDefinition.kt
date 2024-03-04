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
import dev.usbharu.owl.common.retry.RetryPolicy

interface TaskDefinition<T : Task> {
    val name: String
    val priority: Int
    val maxRetry: Int
    val retryPolicy:RetryPolicy
    val timeoutMilli: Long
    val propertyDefinition: PropertyDefinition

    fun serialize(task: T): Map<String, PropertyValue>
    fun deserialize(value: Map<String, PropertyValue>): T
}