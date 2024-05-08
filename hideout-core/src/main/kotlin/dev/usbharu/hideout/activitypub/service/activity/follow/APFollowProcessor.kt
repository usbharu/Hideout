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

package dev.usbharu.hideout.activitypub.service.activity.follow

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.external.job.ReceiveFollowTask
import dev.usbharu.owl.producer.api.OwlProducer
import org.springframework.stereotype.Service

@Service
class APFollowProcessor(
    transaction: Transaction,
    private val objectMapper: ObjectMapper,
    private val owlProducer: OwlProducer,
) :
    AbstractActivityPubProcessor<Follow>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Follow>) {
        logger.info("FOLLOW from: {} to {}", activity.activity.actor, activity.activity.apObject)

        // inboxをジョブキューに乗せているので既に不要だが、フォロー承認制アカウントを実装する際に必要なので残す
        val jobProps = ReceiveFollowTask(
            activity.activity.actor,
            activity.activity,
            activity.activity.apObject
        )
        owlProducer.publishTask(jobProps)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Follow

    override fun type(): Class<Follow> = Follow::class.java
}
