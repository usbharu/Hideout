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
import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Reject
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * ブロックアクティビティ配送のジョブパラメーター
 *
 * @property signer ブロック操作を行ったユーザーid
 * @property block 配送するブロックアクティビティ
 * @property reject 配送するフォロー解除アクティビティ
 * @property inbox 配送先url
 */
data class DeliverBlockJobParam(
    val signer: Long,
    val block: Block,
    val reject: Reject,
    val inbox: String
)

/**
 * ブロックアクティビティ配送のジョブ
 */
@Component
class DeliverBlockJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverBlockJobParam, DeliverBlockJob>("DeliverBlockJob") {

    val block = string("block")
    val reject = string("reject")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverBlockJobParam): ScheduleContext<DeliverBlockJob>.(DeliverBlockJob) -> Unit = {
        props[block] = objectMapper.writeValueAsString(value.block)
        props[reject] = objectMapper.writeValueAsString(value.reject)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverBlockJob>): DeliverBlockJobParam = DeliverBlockJobParam(
        signer = props[signer],
        block = objectMapper.readValue(props[block]),
        reject = objectMapper.readValue(props[reject]),
        inbox = props[inbox]
    )
}
