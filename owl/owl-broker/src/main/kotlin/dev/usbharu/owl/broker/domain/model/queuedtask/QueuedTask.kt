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

package dev.usbharu.owl.broker.domain.model.queuedtask

import dev.usbharu.owl.broker.domain.model.task.Task
import java.time.Instant
import java.util.*

/**
 * @param attempt キューされた時点での試行回数より1多い
 * @param isActive trueならアサイン可能 falseならアサイン済みかタイムアウト等で無効
 */
data class QueuedTask(
    val attempt: Int,
    val queuedAt: Instant,
    val task: Task,
    val priority: Int,
    val isActive: Boolean,
    val timeoutAt: Instant?,
    val assignedConsumer: UUID?,
    val assignedAt: Instant?
)
