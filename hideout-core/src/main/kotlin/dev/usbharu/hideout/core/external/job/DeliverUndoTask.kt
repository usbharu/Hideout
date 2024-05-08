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

package dev.usbharu.hideout.core.external.job

import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.owl.common.property.PropertyValue
import dev.usbharu.owl.common.task.PropertyDefinition
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import org.springframework.stereotype.Component

data class DeliverUndoTask(
    val undo: Undo,
    val inbox: String,
    val signer: Long,
) : Task()

@Component
data object DeliverUndoTaskDef : TaskDefinition<DeliverUndoTask> {
    override val name: String
        get() = TODO("Not yet implemented")
    override val priority: Int
        get() = TODO("Not yet implemented")
    override val maxRetry: Int
        get() = TODO("Not yet implemented")
    override val retryPolicy: String
        get() = TODO("Not yet implemented")
    override val timeoutMilli: Long
        get() = TODO("Not yet implemented")
    override val propertyDefinition: PropertyDefinition
        get() = TODO("Not yet implemented")
    override val type: Class<DeliverUndoTask>
        get() = TODO("Not yet implemented")

    override fun deserialize(value: Map<String, PropertyValue<*>>): DeliverUndoTask {
        TODO("Not yet implemented")
    }

    override fun serialize(task: DeliverUndoTask): Map<String, PropertyValue<*>> {
        TODO("Not yet implemented")
    }
}
