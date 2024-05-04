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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.owl.common.task.Task
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverDeleteJobParam(
    val delete: Delete,
    val inbox: String,
    val signer: Long,
) : Task()

@Component
class DeliverDeleteJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverDeleteJobParam, DeliverDeleteJob>("DeliverDeleteJob") {

    val delete = string("delete")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverDeleteJobParam): ScheduleContext<DeliverDeleteJob>.(DeliverDeleteJob) -> Unit = {
        props[delete] = objectMapper.writeValueAsString(value.delete)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverDeleteJob>): DeliverDeleteJobParam = DeliverDeleteJobParam(
        objectMapper.readValue(props[delete]),
        props[inbox],
        props[signer]
    )
}
