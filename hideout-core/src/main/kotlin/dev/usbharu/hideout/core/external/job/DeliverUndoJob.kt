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
import dev.usbharu.hideout.activitypub.domain.model.Undo
import dev.usbharu.owl.common.task.Task
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverUndoJobParam(
    val undo: Undo,
    val inbox: String,
    val signer: Long,
) : Task()

@Component
class DeliverUndoJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverUndoJobParam, DeliverUndoJob>("DeliverUndoJob") {

    val undo = string("undo")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverUndoJobParam): ScheduleContext<DeliverUndoJob>.(DeliverUndoJob) -> Unit = {
        props[undo] = objectMapper.writeValueAsString(value.undo)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverUndoJob>): DeliverUndoJobParam {
        return DeliverUndoJobParam(
            objectMapper.readValue(props[undo]),
            props[inbox],
            props[signer]
        )
    }
}
