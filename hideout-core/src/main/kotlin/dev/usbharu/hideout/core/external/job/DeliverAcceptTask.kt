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

import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.owl.common.property.*
import dev.usbharu.owl.common.task.PropertyDefinition
import dev.usbharu.owl.common.task.Task
import dev.usbharu.owl.common.task.TaskDefinition
import org.springframework.stereotype.Component

data class DeliverAcceptTask(
    val accept: Accept,
    val inbox: String,
    val signer: Long,
) : Task()

@Component
data object DeliverAcceptTaskDef : TaskDefinition<DeliverAcceptTask> {
    override val name: String
        get() = "DeliverAccept"
    override val priority: Int
        get() = 10
    override val maxRetry: Int
        get() = 5
    override val retryPolicy: String
        get() = ""
    override val timeoutMilli: Long
        get() = 1000
    override val propertyDefinition: PropertyDefinition
        get() = PropertyDefinition(
            mapOf(
                "accept" to PropertyType.binary,
                "inbox" to PropertyType.string,
                "signer" to PropertyType.number,
            )
        )
    override val type: Class<DeliverAcceptTask>
        get() = DeliverAcceptTask::class.java

    override fun serialize(task: DeliverAcceptTask): Map<String, PropertyValue<*>> {
        return mapOf(
            "accept" to ObjectPropertyValue(task.accept),
            "inbox" to StringPropertyValue(task.inbox),
            "signer" to LongPropertyValue(task.signer)
        )
    }

    override fun deserialize(value: Map<String, PropertyValue<*>>): DeliverAcceptTask {
        return DeliverAcceptTask(
            value.getValue("accept").value as Accept,
            value.getValue("inbox").value as String,
            value.getValue("signer").value as Long,
        )
    }
}
