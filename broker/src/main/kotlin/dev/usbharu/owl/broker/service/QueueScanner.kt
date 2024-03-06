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

package dev.usbharu.owl.broker.service

import dev.usbharu.owl.broker.domain.model.queuedtask.QueuedTask
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.koin.core.annotation.Singleton
import java.time.Instant

interface QueueScanner {
    fun startScan(): Flow<QueuedTask>
}

@Singleton
class QueueScannerImpl(private val queueStore: QueueStore) : QueueScanner {
    override fun startScan(): Flow<QueuedTask> {
        return flow {
            while (currentCoroutineContext().isActive) {
                emitAll(scanQueue())
                delay(1000)
            }
        }
    }

    private fun scanQueue(): Flow<QueuedTask> {
        return queueStore.findByQueuedAtBeforeAndAssignedConsumerIsNull(Instant.now().minusSeconds(10))
    }

}