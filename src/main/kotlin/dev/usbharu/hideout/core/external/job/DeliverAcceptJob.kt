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
import dev.usbharu.hideout.activitypub.domain.model.Accept
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.stereotype.Component

data class DeliverAcceptJobParam(
    val accept: Accept,
    val inbox: String,
    val signer: Long
)

@Component
class DeliverAcceptJob(private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverAcceptJobParam, DeliverAcceptJob>("DeliverAcceptJob") {

    val accept = string("accept")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverAcceptJobParam): ScheduleContext<DeliverAcceptJob>.(DeliverAcceptJob) -> Unit = {
        props[accept] = objectMapper.writeValueAsString(value.accept)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverAcceptJob>): DeliverAcceptJobParam {
        return DeliverAcceptJobParam(
            objectMapper.readValue(props[accept]),
            props[inbox],
            props[signer]
        )
    }
}
