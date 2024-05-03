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

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.*

interface QueuedTaskRepository {
    suspend fun save(queuedTask: QueuedTask):QueuedTask

    /**
     * トランザクションの代わり
     */
    suspend fun findByTaskIdAndAssignedConsumerIsNullAndUpdate(id:UUID,update:QueuedTask):QueuedTask

    fun findByTaskNameInAndIsActiveIsTrueAndOrderByPriority(tasks: List<String>, limit: Int): Flow<QueuedTask>

    fun findByQueuedAtBeforeAndIsActiveIsTrue(instant: Instant): Flow<QueuedTask>
}