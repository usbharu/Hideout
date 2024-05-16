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

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.owl.common.property.ObjectPropertyValue
import dev.usbharu.owl.common.property.PropertyValue
import dev.usbharu.owl.common.property.StringPropertyValue
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import org.springframework.stereotype.Component

data class ReceiveFollowTask(
    val actor: String,
    val follow: Follow,
    val targetActor: String,
) : Task()

@Component
data object ReceiveFollowTaskDef : TaskDefinition<ReceiveFollowTask> {
    override val type: Class<ReceiveFollowTask>
        get() = ReceiveFollowTask::class.java

    override fun serialize(task: ReceiveFollowTask): Map<String, PropertyValue<*>> {
        return mapOf(
            "actor" to StringPropertyValue(task.actor),
            "follow" to ObjectPropertyValue(task.follow),
            "targetActor" to StringPropertyValue(task.targetActor)
        )
    }

    override fun deserialize(value: Map<String, PropertyValue<*>>): ReceiveFollowTask {
        return ReceiveFollowTask(
            value.getValue("actor").value as String,
            value.getValue("follow").value as Follow,
            value.getValue("targetActor").value as String,
        )
    }
}
