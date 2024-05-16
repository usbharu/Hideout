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

import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition

abstract class AbstractTaskRunner<T : Task, D : TaskDefinition<T>>(private val taskDefinition: D) : TaskRunner {
    override val name: String
        get() = taskDefinition.name

    override suspend fun run(taskRequest: TaskRequest): TaskResult {
        val deserialize = taskDefinition.deserialize(taskRequest.properties)
        return typedRun(deserialize, taskRequest)
    }

    abstract suspend fun typedRun(typedParam: T, taskRequest: TaskRequest): TaskResult

}