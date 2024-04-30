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

import dev.usbharu.owl.broker.domain.model.task.Task
import dev.usbharu.owl.broker.domain.model.task.TaskRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.koin.core.annotation.Singleton
import org.slf4j.LoggerFactory
import java.time.Instant

interface TaskScanner {

    fun startScan(): Flow<Task>
}

@Singleton
class TaskScannerImpl(private val taskRepository: TaskRepository) :
    TaskScanner {

    override fun startScan(): Flow<Task> = flow {
        while (currentCoroutineContext().isActive) {
            emitAll(scanTask())
            delay(500)
        }
    }

    private fun scanTask(): Flow<Task> {
        return taskRepository.findByNextRetryBeforeAndCompletedAtIsNull(Instant.now())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskScannerImpl::class.java)
    }
}