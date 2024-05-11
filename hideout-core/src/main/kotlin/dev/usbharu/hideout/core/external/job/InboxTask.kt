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

import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.httpsignature.common.HttpRequest
import dev.usbharu.owl.common.property.ObjectPropertyValue
import dev.usbharu.owl.common.property.PropertyValue
import dev.usbharu.owl.common.property.StringPropertyValue
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import org.springframework.stereotype.Component

data class InboxTask(
    val json: String,
    val type: ActivityType,
    val httpRequest: HttpRequest,
    val headers: Map<String, List<String>>,
) : Task()

@Component
data object InboxTaskDef : TaskDefinition<InboxTask> {
    override val type: Class<InboxTask>
        get() = InboxTask::class.java

    override fun serialize(task: InboxTask): Map<String, PropertyValue<*>> {
        return mapOf(
            "json" to StringPropertyValue(task.json),
            "type" to ObjectPropertyValue(task.type),
            "httpRequest" to ObjectPropertyValue(task.httpRequest),
            "headers" to ObjectPropertyValue(task.headers),
        )
    }

    override fun deserialize(value: Map<String, PropertyValue<*>>): InboxTask {
        return InboxTask(
            value.getValue("json").value as String,
            value.getValue("type").value as ActivityType,
            value.getValue("httpRequest").value as HttpRequest,
            value.getValue("headers").value as Map<String, List<String>>,
        )
    }
}
