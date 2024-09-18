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

package dev.usbharu.owl.consumer

import dev.usbharu.owl.common.property.PropertyValue
import java.time.Instant
import java.util.*

/**
 * タスクをConsumerに要求します
 *
 * @property name タスク名
 * @property id タスクID
 * @property attempt 試行回数
 * @property queuedAt タスクがキューに入れられた時間
 * @property properties タスクに渡されたパラメータ
 */
data class TaskRequest(
    val name: String,
    val id: UUID,
    val attempt: Int,
    val queuedAt: Instant,
    val properties: Map<String, PropertyValue<*>>
)
